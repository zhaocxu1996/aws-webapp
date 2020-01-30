package neu.edu.csye6225.service;

import neu.edu.csye6225.entity.User;


/**
 * @author zhaocxu
 */
public interface IUserService {

    /**
     * Create a user
     * @param user
     * @return a user with create&update time but with out password
     */
    User createUser(User user);

    /**
     * Find the user by user's email
     * @param email
     * @return target user/null
     */
    User findUserByEmail(String email);

    /**
     * Update a user
     * @param newUser
     */
    void update(User newUser);
}
