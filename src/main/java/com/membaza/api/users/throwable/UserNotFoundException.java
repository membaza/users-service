package com.membaza.api.users.throwable;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@ResponseStatus(value = NOT_FOUND, reason = "Specified user not found.")
public final class UserNotFoundException extends RuntimeException {}