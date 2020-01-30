package neu.edu.csye6225.config;

import neu.edu.csye6225.entity.User;
import neu.edu.csye6225.service.IUserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author zhaocxu
 */
public class HttpBasicAuthenticationFilter implements Filter {

    @Autowired
    private IUserService iUserService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // get an authorization header which consists of 'Basic token'
        String authorization = request.getHeader("Authorization");
        if (!isAuthorized(authorization)) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter out = response.getWriter();
            out.write("Invalid token.");
            return;
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean isAuthorized(String authorization) throws UnsupportedEncodingException {
        if (authorization == null || authorization.isEmpty() ||
                !authorization.contains("Basic ") || !authorization.startsWith("Basic ")) {
            return false;
        }
        // token is the encoded username of base64
        String token = authorization.substring(6);
        Base64.Decoder decoder = Base64.getDecoder();
        String usernameAndPassword = new String(decoder.decode(token), "UTF-8");;
        // there is a problem about context of spring which will cause null pointer,
        // so try to get an instance of IUserService
        String uAndp[] = usernameAndPassword.split("[:]");
        String username = uAndp[0];
        String password = uAndp[1];
        User user = iUserService.findUserByEmail(username);
        if (user != null) {
            boolean matched = BCrypt.checkpw(password, user.getPassword());
            if (matched) {
                return true;
            }
        }
        return false;
    }

}
