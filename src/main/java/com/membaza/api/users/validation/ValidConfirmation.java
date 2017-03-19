package com.membaza.api.users.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Target({ TYPE, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = ConfirmationValidator.class)
@Documented
@Inherited
public @interface ValidConfirmation {

    String message() default "Invalid Confirmation Code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}