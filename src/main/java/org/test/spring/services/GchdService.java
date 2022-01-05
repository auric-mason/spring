package org.test.spring.services;

import com.sun.jna.Native;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class GchdService {
    public void beginRecording(String tempFileName, String converter) {
        //ok, so technically we can just use exec to do this, and still might...
        // but jna or jni is much cooler...
        //command
        // /home/wolfe/gchd/elgato-gchd/build/src/gchd -c rgb $tfile
        String argv[] = {tempFileName, "-c " + converter == null?"rgb" : converter, tempFileName};
//        URL gchd = GchdService.class.getResource("/gchd");
//        System.load(gchd.getFile());
        System.setProperty("java.library.path","/home/wolfe/gchd/elgato-gchd/build/src");
        System.load("/usr/lib/gchd");
        CGCHD ctest = (CGCHD) Native.loadLibrary("org.test.spring/gchd", CGCHD.class);
        System.out.println("status:" + ctest.main(2, argv));

    }
}
