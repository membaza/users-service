package com.membaza.api.users.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public final class ConfirmationValidator
    implements ConstraintValidator<ValidConfirmation, String> {

    @Override
    public void initialize(final ValidConfirmation arg0) {}

    @Override
    public boolean isValid(final String confirmCode,
                           final ConstraintValidatorContext context) {

        return confirmCode != null
            && confirmCode.length() == 40;
    }
}