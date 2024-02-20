package org.eclipse.emt4j.test;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// 1 fixed problem(should only be reported as 1 problem, although base64 occuer in 2 classes)
@SuppressWarnings("all")
public class SunMiscBase64 implements Problem {
    @Override
    public void produce() {
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            decoder.decodeBuffer("abcd");
            BASE64Encoder encoder = new BASE64Encoder();
            encoder.encode("abcd".getBytes());

            decoder.decodeBuffer(Files.newInputStream(Paths.get("abcd")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class SunMiscBase64Inner implements Problem {
        @Override
        public void produce() {
            try {
                BASE64Decoder decoder = new BASE64Decoder();
                decoder.decodeBuffer("abcd");
                BASE64Encoder encoder = new BASE64Encoder();
                encoder.encode("abcd".getBytes());

                decoder.decodeBuffer(Files.newInputStream(Paths.get("abcd")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
