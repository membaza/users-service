package com.membaza.api.users.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Getter @Setter
public final class GenericResponse {

    private String message;
    private String error;

    public GenericResponse(final String message) {
        this.message = message;
    }

    public GenericResponse(final String message, final String error) {
        this.message = message;
        this.error   = error;
    }

    public GenericResponse(final List<FieldError> fieldErrors,
                           final List<ObjectError> globalErrors) {

        final ObjectMapper mapper = new ObjectMapper();
        try {
            this.message = mapper.writeValueAsString(fieldErrors);
            this.error   = mapper.writeValueAsString(globalErrors);
        } catch (final JsonProcessingException ex) {
            this.message = "";
            this.error   = "";
        }
    }
}