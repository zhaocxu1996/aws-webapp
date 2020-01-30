package neu.edu.csye6225;

import neu.edu.csye6225.entity.User;
import neu.edu.csye6225.service.IUserService;
import neu.edu.csye6225.utils.NIST;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class UserTest {

    @Autowired
    IUserService iUserService;

    /**
     * miss parameter, return 400 bad request
     */
    @Test(dataProvider = "dataProvider")
    public void testCreateUser1(User user) {
        Assert.assertFalse(user.getPassword()==null);
    }

    /**
     * invalid parameter, return 400 bad request
     */
    @Test(dataProvider = "dataProvider")
    public void testCreateUser2(User user) {
        Assert.assertFalse(!NIST.nist(user));
    }

    /**
     * right parameter, return 201 user created
     */
    @Test(dataProvider = "dataProvider")
    public void testCreateUser3(User user) {
        User returnedUser = iUserService.createUser(user);
        Assert.assertEquals(returnedUser.getEmail_address(), user.getEmail_address());
    }

    @DataProvider(name = "dataProvider")
    public Object[][] dataProvider(Method method) {
        String methodName = method.getName();
        Object[][] args;
        switch (methodName) {
            case "testCreateUser1":
                User cuser1 = new User();
                cuser1.setEmail_address("test@example.com");
                args = new Object[][] {{cuser1}};
                break;
            case "testCreateUser2":
                User cuser2 = new User();
                cuser2.setEmail_address("test@example.com");
                cuser2.setPassword("12345678");
                cuser2.setFirst_name("fname");
                cuser2.setLast_name("lname");
                args = new Object[][] {{cuser2}};
                break;
            case "testCreateUser3":
                User cuser3 = new User();
                cuser3.setEmail_address("test@example.com");
                cuser3.setPassword("Abc123456");
                cuser3.setFirst_name("fname");
                cuser3.setLast_name("lname");
                args = new Object[][] {{cuser3}};
                break;
            default:
                args = new Object[][]{};
        }
        return args;
    }
}