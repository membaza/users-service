package com.membaza.api.users.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String EMAIL_PATTERN =
        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
            "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);

    @Override
    public void initialize(final ValidEmail validEmail) {}

    @Override
    public boolean isValid(final String username, final ConstraintValidatorContext context) {
        return validateEmail(username);
    }

    private boolean validateEmail(final String email) {
        final Matcher matcher = PATTERN.matcher(email);
        return matcher.matches();
    }
}