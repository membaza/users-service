package com.membaza.api.users.throwable;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public final class ReCaptchaInvalidException extends RuntimeException {

    private static final long serialVersionUID = 5861310537366287163L;

    public ReCaptchaInvalidException() {
        super();
    }

    public ReCaptchaInvalidException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ReCaptchaInvalidException(final String message) {
        super(message);
    }

    public ReCaptchaInvalidException(final Throwable cause) {
        super(cause);
    }

}