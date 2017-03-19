package com.membaza.api.users.service.jwt;

import lombok.Getter;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public final class JwtToken {

    @Getter
    private final String token;

    public JwtToken(String json) {
        this.token = requireNonNull(json);
    }
}
