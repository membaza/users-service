package com.membaza.api.users.validation;

import com.membaza.api.users.dto.UserDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public final class PasswordMatchesValidator
implements ConstraintValidator<PasswordMatches, UserDto> {

    @Override
    public void initialize(final PasswordMatches constraintAnnotation) {}

    @Override
    public boolean isValid(
            final UserDto user,
            final ConstraintValidatorContext context) {

        return user.getPassword().equals(user.getMatchingPassword());
    }
}
