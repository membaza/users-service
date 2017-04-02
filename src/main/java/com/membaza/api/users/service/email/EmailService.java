package com.membaza.api.users.service.email;

import java.util.Map;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface EmailService {

    void send(String to,
              String toEmail,
              String template,
              String language,
              Map<String, String> args);

}
