package com.membaza.api.users.throwable;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@ResponseStatus(value = UNAUTHORIZED, reason = "Not authorized to perform this operation.")
public final class OperationNotAllowedException extends RuntimeException {

}
