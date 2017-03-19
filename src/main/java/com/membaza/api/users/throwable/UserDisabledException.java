package com.membaza.api.users.throwable;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@ResponseStatus(value = FORBIDDEN, reason = "The specified user has been disabled.")
public final class UserDisabledException extends RuntimeException {}
