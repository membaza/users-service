package com.membaza.api.users.service.login;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
public interface LoginAttemptService {

    void loginSucceeded(String key);

    void loginFailed(String key);

    boolean isBlocked(String key);

}
