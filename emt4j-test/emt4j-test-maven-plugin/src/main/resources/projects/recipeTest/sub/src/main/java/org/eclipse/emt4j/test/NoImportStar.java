import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

// In this test, we don't hope to see inport statements merged into java.util.*" after fixing this file
public class NoImportStar {
    public void usingJavaUtil() {
        List<String> list = new ArrayList<>();
        list = new LinkedList<>();
        Set<String> set = new HashSet<>();
        set = new TreeSet<>();
    }

    public void codeToFix() throws Exception{
        // these code will be autofixed, but this test case is not for testing this
        BASE64Decoder decoder = new BASE64Decoder();
        decoder.decodeBuffer("abcd");
        BASE64Encoder encoder = new BASE64Encoder();
        encoder.encode("abcd".getBytes());
    }
}
