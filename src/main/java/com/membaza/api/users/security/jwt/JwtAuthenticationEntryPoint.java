package com.membaza.api.users.security.jwt;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Component
public final class JwtAuthenticationEntryPoint
implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex
    ) throws IOException, ServletException {
        response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized");
    }

}
