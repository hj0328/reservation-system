package kr.or.connect.reservation.config.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

//        if (session == null || session.getAttribute(UtilConstant.MEMBER_ID) == null) {
//            response.sendRedirect("/");
//            return false;
//        }

        return true;
    }
}
