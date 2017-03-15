package com.membaza.api.users.service.captcha;

import com.membaza.api.users.throwable.ReCaptchaInvalidException;
import com.membaza.api.users.throwable.ReCaptchaUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.regex.Pattern;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Service("captchaService")
public final class CaptchaServiceImpl implements CaptchaService {

    private static final Pattern RESPONSE_PATTERN =
        Pattern.compile("[A-Za-z0-9_-]+");

    private final static Logger LOGGER =
        LoggerFactory.getLogger(CaptchaService.class);

    private @Autowired HttpServletRequest request;
    private @Autowired CaptchaSettings captchaSettings;
    private @Autowired ReCaptchaAttemptService reCaptchaAttemptService;
    private @Autowired RestOperations restTemplate;

    @Override
    public void processResponse(final String response) {
        LOGGER.debug("Attempting to validate response {}", response);

        if (reCaptchaAttemptService.isBlocked(getClientIP())) {
            throw new ReCaptchaInvalidException(
                "Client exceeded maximum number of failed attempts"
            );
        }

        if (!responseSanityCheck(response)) {
            throw new ReCaptchaInvalidException(
                "Response contains invalid characters"
            );
        }

        final URI verifyUri = URI.create(String.format(
            "https://www.google.com/recaptcha/api/siteverify?secret=%s" +
            "&response=%s&remoteip=%s",
            getReCaptchaSecret(), response, getClientIP()
        ));

        try {
            final GoogleResponse googleResponse =
                restTemplate.getForObject(verifyUri, GoogleResponse.class);

            LOGGER.debug("Google's response: %s", googleResponse.toString());

            if (!googleResponse.isSuccess()) {
                if (googleResponse.hasClientError()) {
                    reCaptchaAttemptService.reCaptchaFailed(getClientIP());
                }

                throw new ReCaptchaInvalidException(
                    "reCaptcha was not successfully validated"
                );
            }
        } catch (final RestClientException ex) {
            throw new ReCaptchaUnavailableException(
                "Registration unavailable at this time. Please try again " +
                "later.", ex);
        }
        reCaptchaAttemptService.reCaptchaSucceeded(getClientIP());
    }

    private boolean responseSanityCheck(final String response) {
        return StringUtils.hasLength(response)
            && RESPONSE_PATTERN.matcher(response).matches();
    }

    @Override
    public String getReCaptchaSite() {
        return captchaSettings.getSite();
    }

    @Override
    public String getReCaptchaSecret() {
        return captchaSettings.getSecret();
    }

    private String getClientIP() {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
