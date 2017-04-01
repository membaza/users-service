package com.membaza.api.users.service.email;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.Locale;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Service
public interface EmailService {

    @FunctionalInterface
    interface ContextConsumer {
        void accept(Context ctx);
    }

    void send(String template,
              String subject,
              String recipientName,
              String recipientEmail,
              Locale locale,
              ContextConsumer... setters);

}
