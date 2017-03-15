package com.membaza.api.users.event;

import com.membaza.api.users.persistence.model.User;
import com.membaza.api.users.service.user.UserService;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Component
public final class RegistrationCompleteListener
implements ApplicationListener<RegistrationCompleteEvent> {

    private final UserService service;
    private final MessageSource messages;
    private final JavaMailSender mailSender;
    private final Environment env;

    public RegistrationCompleteListener(
            final UserService service,
            final MessageSource messages,
            final JavaMailSender mailSender,
            final Environment env) {

        this.service    = requireNonNull(service);
        this.messages   = requireNonNull(messages);
        this.mailSender = requireNonNull(mailSender);
        this.env        = requireNonNull(env);
    }

    @Override
    public void onApplicationEvent(final RegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(final RegistrationCompleteEvent event) {
        final User user    = event.getUser();
        final String token = UUID.randomUUID().toString();
        service.createVerificationTokenForUser(user, token);

        final SimpleMailMessage email = constructEmailMessage(event, user, token);
        mailSender.send(email);
    }

    private SimpleMailMessage constructEmailMessage(
            final RegistrationCompleteEvent event,
            final User user,
            final String token) {

        final String recipientAddress = user.getEmail();
        final String subject = "Registration Confirmation";
        final String confirmationUrl = event.getAppUrl() + "/registrationConfirm.html?token=" + token;
        final String message = messages.getMessage("message.regSucc", null, event.getLocale());
        final SimpleMailMessage email = new SimpleMailMessage();

        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + " \r\n" + confirmationUrl);
        email.setFrom(env.getProperty("support.email"));

        return email;
    }
}