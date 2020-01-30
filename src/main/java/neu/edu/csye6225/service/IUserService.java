package neu.edu.csye6225.service;

import neu.edu.csye6225.entity.User;


/**
 * @author zhaocxu
 */
public interface IUserService {

    User createUser(User user);

    User findUserByEmail(String email);

    void update(User newUser);
}
