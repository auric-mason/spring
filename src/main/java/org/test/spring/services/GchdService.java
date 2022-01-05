package org.test.spring.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.test.spring.RecordingException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class GchdService extends AbstractService {

  @Value("${recording.exec.cmd}")
  private String command;

  public Process beginRecording(String... arguments) throws RecordingException {
    // ok, so technically we can just use exec to do this, and still might...
    // but jna or jni is much cooler...
    // command
    // /home/wolfe/gchd/elgato-gchd/build/src/gchd -c rgb $tfile

    try {
      // TODO fix the stram to split the arguments
      Runtime rt = Runtime.getRuntime();
      Process proc =
          rt.exec(command + " " + Arrays.stream(arguments).collect(Collectors.joining(" ")));

      if (!proc.isAlive() && proc.exitValue() != 0) {
        String stdErr = StreamUtils.copyToString(proc.getErrorStream(), Charset.defaultCharset());
        throw new RecordingException("Recording exception " + stdErr);
      }
      return proc;
    } catch (IOException e) {
      throw new RecordingException("Unable to initiate command for recording", e);
    }

    //    `;/home/wolfe/gchd/elgato-gchd/build/src/gchd

    // String argv[] = {tempFileName, "-c " + converter == null?"rgb" : converter, tempFileName};
    //        URL gchd = GchdService.class.getResource("/gchd");
    //        System.load(gchd.getFile());
    //        System.setProperty("java.library.path","/home/wolfe/gchd/elgato-gchd/build/src");
    //        System.load("/usr/lib/gchd");
    //        CGCHD ctest = (CGCHD) Native.loadLibrary("org.test.spring/gchd", CGCHD.class);
    //        System.out.println("status:" + ctest.main(2, argv));

  }
}
