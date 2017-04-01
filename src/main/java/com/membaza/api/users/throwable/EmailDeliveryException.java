package com.membaza.api.users.throwable;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@ResponseStatus(value = INTERNAL_SERVER_ERROR, reason = "Error sending email.")
public final class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(Throwable cause) {
        super(cause);
    }
}
