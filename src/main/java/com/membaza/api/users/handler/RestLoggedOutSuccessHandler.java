package com.membaza.api.users.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Component("restLogoutSuccessHandler")
public final class RestLoggedOutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication
    ) throws IOException, ServletException {

        final HttpSession session = request.getSession();
        if (session != null) {
            session.removeAttribute("user");
        }

        response.sendRedirect("/logout.html?logSucc=true");
    }

}
