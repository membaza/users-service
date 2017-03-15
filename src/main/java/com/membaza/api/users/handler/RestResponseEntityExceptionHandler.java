package com.membaza.api.users.handler;

import com.membaza.api.users.throwable.*;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@ControllerAdvice
public final class RestResponseEntityExceptionHandler
extends ResponseEntityExceptionHandler {

    private final MessageSource messages;

    public RestResponseEntityExceptionHandler(MessageSource messages) {
        this.messages = requireNonNull(messages);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(
            final BindException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request) {

        logger.error("400 Status Code", ex);

        final BindingResult res = ex.getBindingResult();
        final GenericResponse bodyOfResponse =
            new GenericResponse(res.getFieldErrors(), res.getGlobalErrors());

        return handleExceptionInternal(
            ex,
            bodyOfResponse,
            new HttpHeaders(),
            HttpStatus.BAD_REQUEST,
            request
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request) {

        logger.error("400 Status Code", ex);

        final BindingResult res = ex.getBindingResult();
        final GenericResponse bodyOfResponse =
            new GenericResponse(res.getFieldErrors(), res.getGlobalErrors());

        return handleExceptionInternal(
            ex,
            bodyOfResponse,
            new HttpHeaders(),
            HttpStatus.BAD_REQUEST,
            request
        );
    }

    @ExceptionHandler(InvalidOldPasswordException.class)
    public ResponseEntity<Object> handleInvalidOldPassword(
            final RuntimeException ex,
            final WebRequest request) {

        logger.error("400 Status Code", ex);

        final GenericResponse bodyOfResponse = new GenericResponse(
            messages.getMessage(
                "message.invalidOldPassword", null, request.getLocale()
            ), "InvalidOldPassword"
        );

        return handleExceptionInternal(
            ex,
            bodyOfResponse,
            new HttpHeaders(),
            HttpStatus.BAD_REQUEST,
            request
        );
    }

    @ExceptionHandler(ReCaptchaInvalidException.class)
    public ResponseEntity<Object> handleReCaptchaInvalid(
            final RuntimeException ex,
            final WebRequest request) {

        logger.error("400 Status Code", ex);

        final GenericResponse bodyOfResponse = new GenericResponse(
            messages.getMessage(
                "message.invalidReCaptcha",
                null, request.getLocale()
            ), "InvalidReCaptcha"
        );

        return handleExceptionInternal(
            ex,
            bodyOfResponse,
            new HttpHeaders(),
            HttpStatus.BAD_REQUEST,
            request
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFound(
            final RuntimeException ex,
            final WebRequest request) {

        logger.error("404 Status Code", ex);

        final GenericResponse bodyOfResponse = new GenericResponse(
            messages.getMessage(
                "message.userNotFound",
                null,
                request.getLocale()
            ), "UserNotFound"
        );

        return handleExceptionInternal(
            ex,
            bodyOfResponse,
            new HttpHeaders(),
            HttpStatus.NOT_FOUND,
            request
        );
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Object> handleUserAlreadyExist(
            final RuntimeException ex,
            final WebRequest request) {

        logger.error("409 Status Code", ex);

        final GenericResponse bodyOfResponse = new GenericResponse(
            messages.getMessage(
                "message.regError",
                null,
                request.getLocale()
            ), "UserAlreadyExist"
        );

        return handleExceptionInternal(
            ex,
            bodyOfResponse,
            new HttpHeaders(),
            HttpStatus.CONFLICT,
            request
        );
    }

    @ExceptionHandler(MailAuthenticationException.class)
    public ResponseEntity<Object> handleMail(
            final RuntimeException ex,
            final WebRequest request) {

        logger.error("500 Status Code", ex);

        final GenericResponse bodyOfResponse = new GenericResponse(
            messages.getMessage(
                "message.email.config.error",
                null,
                request.getLocale()
            ), "MailError"
        );

        return new ResponseEntity<>(
            bodyOfResponse,
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(ReCaptchaUnavailableException.class)
    public ResponseEntity<Object> handleReCaptchaUnavailable(
            final RuntimeException ex,
            final WebRequest request) {

        logger.error("500 Status Code", ex);

        final GenericResponse bodyOfResponse = new GenericResponse(
            messages.getMessage(
                "message.unavailableReCaptcha",
                null, request.getLocale()
            ), "InvalidReCaptcha"
        );

        return handleExceptionInternal(
            ex,
            bodyOfResponse,
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR,
            request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternal(
            final RuntimeException ex,
            final WebRequest request) {

        logger.error("500 Status Code", ex);

        final GenericResponse bodyOfResponse = new GenericResponse(
            messages.getMessage(
                "message.error",
                null,
                request.getLocale()
            ), "InternalError"
        );

        return new ResponseEntity<>(
            bodyOfResponse,
            new HttpHeaders(),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
