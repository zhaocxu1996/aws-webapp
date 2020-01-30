package neu.edu.csye6225.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaocxu
 */
@Configuration
public class WebConfig {

    @Bean("basicFilter")
    public Filter httpBasicAuthenticationFilter() {
        return new HttpBasicAuthenticationFilter();
    }

    @Bean
    public FilterRegistrationBean httpBasicAuthenticationFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new DelegatingFilterProxy("basicFilter"));
        List<String> patterns = new ArrayList<>();
        patterns.add("/v1/user/self");
//        patterns.add("/v1/bill");
        patterns.add("/v1/bills");
        patterns.add("/v1/bill/*");
        registration.setUrlPatterns(patterns);
        registration.setName("basicFilter");
        registration.setOrder(1);
        return registration;
    }
}
