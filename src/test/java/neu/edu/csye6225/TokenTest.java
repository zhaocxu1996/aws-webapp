package neu.edu.csye6225;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class TokenTest {

    @Test
    void test1() throws UnsupportedEncodingException {
        String password = "liu.chang@husky.neu.edu";
        Base64.Encoder encoder = Base64.getEncoder();
        String encoded = encoder.encodeToString(password.getBytes("UTF-8"));
        System.out.println(encoded);
        Base64.Decoder decoder = Base64.getDecoder();
        String decoded = new String(decoder.decode(encoded), "UTF-8");
        System.out.println(decoded);
    }
}
