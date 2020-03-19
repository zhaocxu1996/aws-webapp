package neu.edu.csye6225.controller;

import com.google.gson.JsonObject;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import neu.edu.csye6225.entity.User;
import neu.edu.csye6225.service.IUserService;
import neu.edu.csye6225.utils.NIST;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

/**
 * @author zhaocxu
 */
@RestController
public class UserController {

    Logger logger = LoggerFactory.getLogger(UserController.class);
    StatsDClient statsd = new NonBlockingStatsDClient("csye6225", "localhost", 8125);

    @Autowired
    IUserService iUserService;

    @GetMapping(value = "/v1/user/self", produces = "application/json")
    public String getUserInfo(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("user.GET");

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("user.GET-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }
        stopWatch.stop();

        stopWatch.start("sql");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        String password = uAndp[1];
        User user = iUserService.findUserByEmail(username);
        stopWatch.stop();
        statsd.recordExecutionTime("user.GET-sql",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("user.GET-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }

        JsonObject data = new JsonObject();
        data.addProperty("id", user.getId());
        data.addProperty("first_name", user.getFirst_name());
        data.addProperty("last_name", user.getLast_name());
        data.addProperty("email_address", user.getEmail_address());
        data.addProperty("account_created", user.getAccount_created());
        data.addProperty("account_updated", user.getAccount_updated());
        response.setStatus(HttpServletResponse.SC_OK);
        logger.info("User found.");
        stopWatch.stop();
        statsd.recordExecutionTime("user.GET-api",stopWatch.getTotalTimeMillis());
        return data.toString();
    }

    @PutMapping(value = "/v1/user/self", produces = "application/json")
    public String updateUserInfo(HttpServletRequest request, HttpServletResponse response, @RequestBody User user) throws UnsupportedEncodingException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("user.PUT");

        // As a user, I want to update my account information. I should only be allowed to update following fields.
        // Attempt to update any other field should return 400 Bad Request HTTP response code.
        if (user.getId() != null || user.getAccount_updated() != null ||
                user.getAccount_created() != null || user.getEmail_address() != null || !NIST.nist(user)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Invalid user content.");
            stopWatch.stop();
            statsd.recordExecutionTime("user.PUT-api",stopWatch.getTotalTimeMillis());
            return "Invalid user content.";
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("user.PUT-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }
        stopWatch.stop();

        stopWatch.start("sql");
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        String password = uAndp[1];
        User tokenUser = iUserService.findUserByEmail(username);
        stopWatch.stop();
        statsd.recordExecutionTime("user.PUT-sql-1",stopWatch.getLastTaskTimeMillis());

        if (tokenUser == null || !BCrypt.checkpw(password, tokenUser.getPassword())) {
            stopWatch.start("api");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            logger.error("Invalid token.");
            stopWatch.stop();
            statsd.recordExecutionTime("user.PUT-api",stopWatch.getTotalTimeMillis());
            return "Invalid token.";
        }

        stopWatch.start("sql");
        if (user.getFirst_name() != null) {
            tokenUser.setFirst_name(user.getFirst_name());
        }
        if (user.getLast_name() != null) {
            tokenUser.setLast_name(user.getLast_name());
        }
        if (user.getPassword() != null) {
            tokenUser.setPassword(user.getPassword());
        }
        iUserService.update(tokenUser);
        stopWatch.stop();
        statsd.recordExecutionTime("user.PUT-sql-2",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        // Password field should never be returned in the response payload
        tokenUser.setPassword(null);
        JsonObject data = new JsonObject();
        data.addProperty("id", tokenUser.getId());
        data.addProperty("first_name", tokenUser.getFirst_name());
        data.addProperty("last_name", tokenUser.getLast_name());
        data.addProperty("email_address", tokenUser.getEmail_address());
        data.addProperty("account_created", tokenUser.getAccount_created());
        data.addProperty("account_updated", tokenUser.getAccount_updated());
        response.setStatus(HttpServletResponse.SC_OK);
        logger.info("User updated.");
        stopWatch.stop();
        statsd.recordExecutionTime("user.PUT-api",stopWatch.getTotalTimeMillis());
        return data.toString();
    }

    @PostMapping(value = "/v1/user", produces = "application/json")
    public String createUser(HttpServletResponse response, @RequestBody User user) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("api");
        statsd.increment("user.POST");

        // if any one of these information is missing, return 400
        // Application must return 400 Bad Reqest HTTP response code when a user account with the email address already exists.
        if (user.getEmail_address() == null || user.getPassword() == null ||
                user.getFirst_name() == null || user.getLast_name() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Invalid input.");
            stopWatch.stop();
            statsd.recordExecutionTime("user.POST-api",stopWatch.getTotalTimeMillis());
            return "You must input email, password, first name and last name.";
        }
        if (!NIST.nist(user)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Invalid password.");
            stopWatch.stop();
            statsd.recordExecutionTime("user.POST-api",stopWatch.getTotalTimeMillis());
            return "Password is too short or too simple.";
        }
        stopWatch.stop();

        stopWatch.start("sql");
        User exist = iUserService.findUserByEmail(user.getEmail_address());
        if (exist != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Email already exist.");
            stopWatch.stop();
            statsd.recordExecutionTime("user.POST-sql-1",stopWatch.getLastTaskTimeMillis());
            return "Email already exist.";
        }
        stopWatch.stop();

        stopWatch.start("api");
        if (user.getId() != null || user.getAccount_updated() != null || user.getAccount_created() != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            logger.error("Illegal input.");
            stopWatch.stop();
            statsd.recordExecutionTime("user.POST-api",stopWatch.getLastTaskTimeMillis());
            return "Illegal input.";
        }
        stopWatch.stop();

        stopWatch.start("sql");
        // Any value provided for these fields must be ignored.
        if (user.getAccount_created()!=null) {
            user.setAccount_created(null);
        }
        if (user.getAccount_updated()!=null) {
            user.setAccount_updated(null);
        }
        User returnedUser = iUserService.createUser(user);
        stopWatch.stop();
        statsd.recordExecutionTime("user.POST-sql-2",stopWatch.getLastTaskTimeMillis());

        stopWatch.start("api");
        JsonObject data = new JsonObject();
        data.addProperty("id", returnedUser.getId());
        data.addProperty("first_name", returnedUser.getFirst_name());
        data.addProperty("last_name", returnedUser.getLast_name());
        data.addProperty("email_address", returnedUser.getEmail_address());
        data.addProperty("account_created", returnedUser.getAccount_created());
        data.addProperty("account_updated", returnedUser.getAccount_updated());
        response.setStatus(HttpServletResponse.SC_CREATED);
        logger.info("User created.");
        stopWatch.stop();
        statsd.recordExecutionTime("user.POST-api",stopWatch.getTotalTimeMillis());
        return data.toString();
    }

}
