package com.membaza.api.users.config;

import com.membaza.api.users.validation.ConfirmationValidator;
import com.membaza.api.users.validation.EmailValidator;
import com.membaza.api.users.validation.PasswordConstraintValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Configuration
public class ValidatorConfig {

    @Bean
    public EmailValidator usernameValidator() {
        return new EmailValidator();
    }

    @Bean
    public PasswordConstraintValidator passwordValidator() {
        return new PasswordConstraintValidator();
    }

    @Bean
    public ConfirmationValidator confirmationValidator() {
        return new ConfirmationValidator();
    }
}