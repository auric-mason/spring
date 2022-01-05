package org.test.spring.services;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.test.spring.RecordingException;
import org.test.spring.dao.RecordingsRepository;
import org.test.spring.model.TempFiles;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FFMpegService {

  @Autowired private RecordingsRepository recordingsRepository;

  @Value("${ffmpeg.path}")
  private String ffMpegPath;

  @Value("${ffprobe.path}")
  private String ffProbe;

  public void combineAndConvertFiles(String outputFileName, List<TempFiles> files)
      throws RecordingException {
    // so first thing is to make sure they are the right length,

    FFmpeg ffmpeg = null;
    try {
      ffmpeg = new FFmpeg(ffMpegPath);
      FFprobe ffprobe = new FFprobe(ffProbe);

      FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
      // truncate the temp files
      if (files.size() > 1) {

        for (TempFiles file : files) {

          // FFmpegProbeResult probeResult = ffprobe.probe(file.tmpFile().getAbsolutePath());
          File tmpFile = File.createTempFile("trunc", "");

          FFmpegBuilder truncateBuilder =
              new FFmpegBuilder()
                  .setInput(file.tmpFile().getAbsolutePath())
                  .overrideOutputFiles(true)
                  .addOutput(tmpFile.getAbsolutePath())
                  .setDuration(file.duration(), TimeUnit.MINUTES)
                  .setVideoCodec("copy")
                  .setAudioCodec("copy")
                  .done();

          // Run a one-pass encode
          executor.createJob(truncateBuilder).run();
          file.tmpFile().delete();
          tmpFile.renameTo(file.tmpFile());
        }

        // then stitch them together

        FFmpegBuilder concatBuilder =
            new FFmpegBuilder()
                .overrideOutputFiles(true)
                .addExtraArgs("-vaapi_device /dev/dri/renderD128");

        for (TempFiles file : files) {
          concatBuilder = concatBuilder.addInput(file.tmpFile().getAbsolutePath());
        }

        concatBuilder =
            concatBuilder
                .addOutput(outputFileName)
                .setVideoCodec("libx265")
                .setAudioCodec("aac")
                .done();

        executor.createJob(concatBuilder).run();

      } else {
        FFmpegBuilder oneFileBuilder =
            new FFmpegBuilder()
                .overrideOutputFiles(true)
                .addExtraArgs("-vaapi_device /dev/dri/renderD128")
                .addInput(files.get(0).tmpFile().getAbsolutePath())
                .addOutput(outputFileName)
                .setVideoCodec("libx265")
                .setAudioCodec("aac")
                .done();

        executor.createJob(oneFileBuilder).run();
      }
    } catch (Throwable e) {
      throw new RecordingException("Conversion failed", e);
    }
  }
}
