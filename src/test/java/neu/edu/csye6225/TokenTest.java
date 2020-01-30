package neu.edu.csye6225;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class TokenTest {

    @Test
    void test1() throws UnsupportedEncodingException {
        String username = "liu.chang@husky.neu.edu";
        String password = "Abc123456";
        String token = username + password;
        Base64.Encoder encoder = Base64.getEncoder();
        String encoded = encoder.encodeToString(token.getBytes("UTF-8"));
        System.out.println(encoded);
        Base64.Decoder decoder = Base64.getDecoder();
        String decoded = new String(decoder.decode(encoded), "UTF-8");
        System.out.println(decoded);
    }
}
