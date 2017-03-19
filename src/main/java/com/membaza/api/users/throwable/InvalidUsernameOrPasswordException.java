package com.membaza.api.users.throwable;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public final class InvalidUsernameOrPasswordException extends RuntimeException {

    public InvalidUsernameOrPasswordException(String message) {
        super(message);
    }

    public InvalidUsernameOrPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
