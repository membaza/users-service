package com.membaza.api.users.service.captcha;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "google.recaptcha.key")
public class CaptchaSettings {
    private String site;
    private String secret;
}