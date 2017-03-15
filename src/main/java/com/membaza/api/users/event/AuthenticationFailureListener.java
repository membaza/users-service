package com.membaza.api.users.event;

import com.membaza.api.users.service.login.LoginAttemptServiceImpl;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Component
public final class AuthenticationFailureListener
implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final LoginAttemptServiceImpl loginAttemptService;

    public AuthenticationFailureListener(LoginAttemptServiceImpl service) {
        this.loginAttemptService = requireNonNull(service);
    }

    @Override
    public void onApplicationEvent(final AuthenticationFailureBadCredentialsEvent e) {
        final WebAuthenticationDetails auth = (WebAuthenticationDetails)
            e.getAuthentication().getDetails();

        if (auth != null) {
            loginAttemptService.loginFailed(auth.getRemoteAddress());
        }
    }
}