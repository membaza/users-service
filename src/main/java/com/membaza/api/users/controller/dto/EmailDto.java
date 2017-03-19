package com.membaza.api.users.controller.dto;

import com.membaza.api.users.validation.ValidEmail;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Data
public final class EmailDto {

    @NotNull
    @Size(min = 1)
    @ValidEmail
    private String email;

}
