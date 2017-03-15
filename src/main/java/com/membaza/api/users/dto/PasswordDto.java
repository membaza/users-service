package com.membaza.api.users.dto;

import com.membaza.api.users.validation.ValidPassword;
import lombok.Data;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Data
public final class PasswordDto {

    @ValidPassword
    private String newPassword;
    private String oldPassword;

}