package com.membaza.api.users.throwable;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@ResponseStatus(value = NOT_FOUND, reason = "Verification code is either invalid or expired")
public final class InvalidVerificationCodeException extends RuntimeException {}