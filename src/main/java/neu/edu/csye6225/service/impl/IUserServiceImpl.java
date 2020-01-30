package neu.edu.csye6225.service.impl;

import neu.edu.csye6225.dao.UserDao;
import neu.edu.csye6225.entity.User;
import neu.edu.csye6225.service.IUserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author zhaocxu
 */
@Service
public class IUserServiceImpl implements IUserService {

    @Autowired
    UserDao userDao;

    @Override
    public User createUser(User user) {
        // account_created field for the user should be set to current time when user creation is successful.
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        user.setAccount_created(date);
        user.setAccount_updated(date);
        // encode password
        String encodedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(encodedPassword);
        userDao.save(user);
        // Password field should never be returned in the response payload
        user.setPassword(null);
        return user;
    }

    @Override
    public User findUserByEmail(String email) {
        List<User> users = userDao.findAll();
        for (User user : users) {
            if (user.getEmail_address().equals(email)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void update(User newUser) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        newUser.setAccount_updated(date);
        // encode password
        String encodedPassword = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
        newUser.setPassword(encodedPassword);
        userDao.save(newUser);
    }

}
