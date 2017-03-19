package com.membaza.api.users.throwable;

import com.membaza.api.users.persistence.model.User;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public final class UserNotConfirmedException extends RuntimeException {

    public UserNotConfirmedException(User user) {
        super("User '" + user.getEmail() + "' has not yet confirmed registration.");
    }

}
