package neu.edu.csye6225.utils;

import neu.edu.csye6225.entity.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhaocxu
 */
public class NIST {
    /**
     * As a user, I expect application to enforce strong password as recommended by NIST.
     * @param user
     * @return whether the password that user input is valid or not
     */
    public static boolean nist(User user) {
        String password = user.getPassword();
        // contains characters from other input contents
        if ((user.getEmail_address()!=null&&password.contains(user.getEmail_address())) ||
                (user.getFirst_name()!=null&&password.contains(user.getFirst_name())) ||
                (user.getLast_name()!=null&&password.contains(user.getLast_name()))) {
            return false;
        }
        // contains both upper and lower case characters and number, and length at least 8
        // refer https://ask.csdn.net/questions/673231
        String pattern = "^(?:(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])).{8,}$";
        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(password);
        boolean flag = matcher.matches();
        return flag;
    }
}
