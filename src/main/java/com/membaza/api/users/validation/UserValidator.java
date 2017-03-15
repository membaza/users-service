package com.membaza.api.users.validation;

import com.membaza.api.users.dto.UserDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
public final class UserValidator implements Validator {

    @Override
    public boolean supports(final Class<?> clazz) {
        return UserDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(final Object obj, final Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "message.firstName", "Firstname is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "message.lastName", "Lastname is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "message.password", "Lastname is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "message.username", "Username is required.");
    }
}