package com.membaza.api.users.service.email;

import com.membaza.api.users.throwable.EmailDeliveryException;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
public final class EmailServiceImpl implements EmailService {

    private final MessageSource msgSource;
    private final JavaMailSender sender;
    private final TemplateEngine engine;
    private final Environment env;

    public EmailServiceImpl(MessageSource msgSource,
                            JavaMailSender sender,
                            TemplateEngine engine,
                            Environment env) {

        this.msgSource = requireNonNull(msgSource);
        this.sender    = requireNonNull(sender);
        this.engine    = requireNonNull(engine);
        this.env       = requireNonNull(env);
    }

    @Override
    public void send(String template,
                     String subject,
                     String recipientName,
                     String recipientEmail,
                     Locale locale,
                     ContextConsumer... setters) {

        final Context ctx = new Context(locale);
        Stream.of(setters).forEach(setter -> setter.accept(ctx));
        msgSource.getMessage(template + ".subject", new Object[] {
            recipientName,
            recipientEmail
        }, locale);

        try {
            // Prepare message using a Spring helper
            final MimeMessage mimeMessage = sender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            message.setSubject(subject);
            message.setFrom(env.getProperty("service.email.sender"));
            message.setTo(recipientEmail);

            // Create the HTML body using Thymeleaf
            final String htmlContent = engine.process(template + ".html", ctx);
            message.setText(htmlContent, true);

            // Send mail
            sender.send(mimeMessage);
        } catch (final MessagingException ex) {
            throw new EmailDeliveryException(ex);
        }
    }
}
