package com.membaza.api.users.throwable;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@ResponseStatus(value = NOT_ACCEPTABLE, reason = "Invalid mail or password.")
public final class InvalidEmailOrPasswordException extends RuntimeException {}