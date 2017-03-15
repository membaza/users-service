package com.membaza.api.users.validation;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@SuppressWarnings("serial")
public final class EmailExistsException extends Throwable {

    public EmailExistsException(final String message) {
        super(message);
    }

}