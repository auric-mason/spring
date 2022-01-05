package org.test.spring.services;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.test.spring.dao.RecordingsRepository;
import org.test.spring.model.Recordings;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RecordingService {

    @Autowired
    private GchdService gchdService;

    @Autowired
    private RecordingsRepository recordingRepository;

    public static enum Status {
        FINISHED,
        STARTED,
        PAUSED,
        CANCELED
    }

    public String startRecording(Recordings recording){
        recording.setCompleted(Status.STARTED.ordinal());
        recordingRepository.save(recording);
        try {
            File tmpFile = File.createTempFile("gchd_",".ts");
            gchdService.beginRecording(tmpFile.getAbsolutePath(), null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";//return the tmp file name
    }

    public String stopRecording(Long id) {
        Optional<Recordings> rec = recordingRepository.findById(id);
        if(rec.isPresent()) {
            rec.get().setCompleted(Status.CANCELED.ordinal());
            recordingRepository.save(rec.get());
        }
        return "";//return the tmp file name
    }
    public String pauseRecording(Long id) {

        Optional<Recordings> rec = recordingRepository.findById(id);
        if(rec.isPresent()) {
            rec.get().setCompleted(Status.PAUSED.ordinal());
            recordingRepository.save(rec.get());
        }
        return "";//return the tmp file name
    }
    public String resumeRecording(Long id) {
        Optional<Recordings> rec = recordingRepository.findById(id);
        if(rec.isPresent()) {
            rec.get().setCompleted(Status.STARTED.ordinal());
            recordingRepository.save(rec.get());
        }
        return "";//return the tmp file name
    }

    public List<Recordings> listRecentRecordings() {
        return StreamSupport.stream(recordingRepository.findAll().spliterator(),
                false).collect(Collectors.toList());
    }
}
