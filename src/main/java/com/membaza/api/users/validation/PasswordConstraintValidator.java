package com.membaza.api.users.validation;

import com.google.common.base.Joiner;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Collections.singletonList;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public final class PasswordConstraintValidator
    implements ConstraintValidator<ValidPassword, String> {

    @Override
    public void initialize(final ValidPassword arg0) {

    }

    @Override
    public boolean isValid(final String password, final ConstraintValidatorContext context) {
        final PasswordValidator validator = new PasswordValidator(singletonList(
            new LengthRule(8, 30)
            // More rules can be added to increase security
        ));

        final RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            Joiner.on("\n").join(validator.getMessages(result))
        ).addConstraintViolation();

        return false;
    }
}