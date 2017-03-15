package com.membaza.api.users.dto;

import com.membaza.api.users.validation.PasswordMatches;
import com.membaza.api.users.validation.ValidEmail;
import com.membaza.api.users.validation.ValidPassword;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Data
@PasswordMatches
public final class UserDto {

    @NotNull
    @Size(min = 1)
    private String firstName;

    @NotNull
    @Size(min = 1)
    private String lastName;

    @ValidPassword
    private String password;

    @NotNull
    @Size(min = 1)
    private String matchingPassword;

    @ValidEmail
    @NotNull
    @Size(min = 1)
    private String email;

    private boolean isUsing2FA;

    private Integer role;

}