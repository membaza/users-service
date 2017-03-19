package com.membaza.api.users.throwable;

import com.membaza.api.users.persistence.model.User;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public final class UserDisabledException extends RuntimeException {

    public UserDisabledException(User user) {
        super("User account '" + user.getEmail() + "' has been disabled.");
    }

}
