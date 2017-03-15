package com.membaza.api.users.service.captcha;

import com.membaza.api.users.throwable.ReCaptchaInvalidException;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
public interface CaptchaService {

    void processResponse(final String response)
    throws ReCaptchaInvalidException;

    String getReCaptchaSite();

    String getReCaptchaSecret();

}