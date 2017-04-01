package com.membaza.api.users.throwable;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.CONFLICT;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@ResponseStatus(value = CONFLICT, reason = "A user with that mail already exists.")
public final class UserAlreadyExistException extends RuntimeException {}