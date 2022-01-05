package org.test.spring.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.test.spring.RecordingException;
import org.test.spring.dao.RecordingsRepository;
import org.test.spring.model.Recording;
import org.test.spring.model.TempFiles;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RecordingService extends AbstractService {

  @Autowired private GchdService gchdService;

  @Autowired private RecordingsRepository recordingRepository;

  @Autowired private FFMpegService ffMpegService;

  @Value("${recording.exec.concurrentRecordings}")
  private Integer executorThreads;

  private Map<Long, Recording> inProcessRecordings = new HashMap<>();

  private ThreadPoolExecutor executor = null;

  @PostConstruct
  public void RecordingServiceInit() {
    executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(executorThreads);
  }

  public enum Status {
    FINISHED,
    STARTED,
    PAUSED,
    CANCELED,
    FAILED
  }

  public Recording startRecording(Recording recording) {
    if (executor.getActiveCount() == executor.getMaximumPoolSize()) {
      recording.setStatus(Status.CANCELED);
      return recording;
      // throw new RecordingException("Unable to init recording, pool exhausted");
    }

    recording.setStatus(Status.STARTED);
    recording = recordingRepository.save(recording);
    try {
      recording.getTmpFiles().add(startRecording());
      executor.execute(new RecordingMonitor(recording));
      inProcessRecordings.put(recording.getId(), recording);
    } catch (IOException e) {
      log.error("Unable to create temp file", e);
    }

    return recording;
  }

  public String stopRecording(Long id) {
    Optional<Recording> rec = recordingRepository.findById(id);
    if (rec.isPresent()) {
      synchronized (id) {
        rec.get().setStatus(Status.CANCELED);
        recordingRepository.save(rec.get());
      }
    }
    return ""; // return the tmp file name
  }

  public String pauseRecording(Long id) {
    return pauseRecording(id, 0);
  }

  public String pauseRecording(Long id, Integer minsBack) {

    Recording rec = recordingRepository.findById(id).get();
    synchronized (id) {
      rec.setStatus(Status.PAUSED);
      recordingRepository.save(rec);
      addTempFileDuration(rec, minsBack * -1);
    }
    return ""; // return the tmp file name
  }

  public String resumeRecording(Long id) {
    if (executor.getActiveCount() == executor.getMaximumPoolSize()) {
      return "";
      // throw new RecordingException("Unable to init recording, pool exhausted");
    }
    Recording rec = recordingRepository.findById(id).get();

    synchronized (id) {
      rec.setStatus(Status.STARTED);
      rec = recordingRepository.save(rec);

      try {
        rec.setTmpFiles(inProcessRecordings.get(rec.getId()).getTmpFiles());
        rec.getTmpFiles().add(startRecording());
        executor.execute(new RecordingMonitor(rec));
        inProcessRecordings.put(rec.getId(), rec);
      } catch (IOException e) {
        log.error("Unable to create temp file", e);
      }
    }
    return ""; // return the tmp file name
  }

  public List<Recording> listRecentRecordings() {
    return StreamSupport.stream(recordingRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  private TempFiles startRecording() throws IOException {
    File tmpFile = null;
    Process recordProc = null;
    tmpFile = File.createTempFile("gchd_", ".ts");
    try {
      recordProc = gchdService.beginRecording("-c rgb", tmpFile.getAbsolutePath());
    } catch (RecordingException e) {
      log.error("Unable to start the recording", e);
      if (tmpFile != null && tmpFile.exists()) {
        tmpFile.delete();
      }
    }

    return new TempFiles(tmpFile, 0, recordProc);
  }

  private void addTempFileDuration(Recording recording, Integer duration) {
    List<TempFiles> tmpFiles = recording.getTmpFiles();
    TempFiles lastFile = tmpFiles.get(recording.getTmpFiles().size() - 1);
    setTempFileDuration(recording, lastFile.duration() + duration);
  }
  /***
   * sets the duration of the last instance of the temp files in the recording
   * @param recording
   * @param duration
   */
  private void setTempFileDuration(Recording recording, Integer duration) {
    List<TempFiles> tmpFiles = recording.getTmpFiles();
    TempFiles lastFile = tmpFiles.get(recording.getTmpFiles().size() - 1);
    TempFiles newTmpFile = new TempFiles(lastFile.tmpFile(), duration, lastFile.process());
    tmpFiles.remove(lastFile);
    tmpFiles.add(lastFile);
    recording.setTmpFiles(tmpFiles);
  }

  private void deleteTempFiles(Recording recording) {
    for (TempFiles t : recording.getTmpFiles()) {
      if (t.tmpFile().exists()) {
        int i = 0;
        while (!t.tmpFile().delete() && i < 10) {
          i++;
          try {
            Thread.sleep(10000);
          } catch (InterruptedException e) {
            log.error("Unable to delete file " + t.tmpFile().getAbsolutePath());
          }
        }
        if (i == 10) {
          log.error("Unable to delete file " + t.tmpFile().getAbsolutePath());
        }
      }
    }
  }
  /***
   * Monitors the recording initiated and handles changes to the status.
   */
  private class RecordingMonitor implements Runnable {
    private Recording recording;
    private Integer monitorDuration = 0;

    public RecordingMonitor(Recording recording) {
      this.recording = recording;
    }

    @Override
    public void run() {
      while (recording.getCompleted() < recording.getDuration()
          && recording.getStatus() == Status.STARTED) {

        try {
          Thread.sleep(60000); // always in 1 min increments
        } catch (InterruptedException e) {
          log.error("Error for " + recording.getId(), e);
          throw new RuntimeException();
        }

        synchronized (recording.getId()) {
          recording = recordingRepository.findById(recording.getId()).get();
          Integer completed = recording.getCompleted();
          recording.setCompleted(completed + 1);
          monitorDuration++;
          setTempFileDuration(recording, monitorDuration);

          if (!isRunning()) {
            recording.setStatus(Status.FAILED);
          }
          recordingRepository.save(recording);
          inProcessRecordings.put(recording.getId(), recording);
        }
      }

      switch (recording.getStatus()) {
        case STARTED:
          killRecordingProcess();
          try {
            ffMpegService.combineAndConvertFiles(recording.getName(), recording.getTmpFiles());
            deleteTempFiles(recording);
            synchronized (recording.getId()) {
              recording.setStatus(Status.FINISHED);
              recordingRepository.save(recording);
              inProcessRecordings.remove(recording.getId());
            }
          } catch (RecordingException e) {
            synchronized (recording.getId()) {
              recording.setStatus(Status.FAILED);
              recordingRepository.save(recording);
              inProcessRecordings.remove(recording.getId());
            }
            log.error("Unable to finish file", e);
          }
          break;
        case PAUSED:
          killRecordingProcess();
          inProcessRecordings.put(recording.getId(), recording);
          break;
        case CANCELED:
        case FAILED:
          killRecordingProcess();
          deleteTempFiles(recording);
          inProcessRecordings.remove(recording.getId());
          break;
        default:
          // do nothing since
      }
      log.info("Recording for " + recording + "\n completed");
    }

    private boolean isRunning() {
      return recording.getTmpFiles().stream().filter(t -> t.process().isAlive()).count() > 0;
    }

    private void killRecordingProcess() {
      for (TempFiles t : recording.getTmpFiles()) {
        if (t.process().isAlive()) {
          t.process().destroy();
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            log.error("Error for " + recording.getId(), e);
            throw new RuntimeException();
          }
          if (t.process().isAlive()) {
            t.process().destroyForcibly();
          }
        }
      }
    }
  }
}
