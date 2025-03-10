package kr.or.connect.reservation.config;

import kr.or.connect.reservation.config.authentication.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/detail","/review","/my-reservation", "/booking-login", "/booking", "/profile",
                        "/api/promotions",
                        "/api/products/**", "/api/users/**",
                        "/static/css/**", "/static/font/**", "/static/htmls/**", "/static/img/**",
                        "/static/img_map/**", "/static/js/**", "/views/**", "/*.ico");
    }
}
