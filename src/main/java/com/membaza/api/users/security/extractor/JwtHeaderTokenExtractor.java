package com.membaza.api.users.security.extractor;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Service
public final class JwtHeaderTokenExtractor
implements TokenExtractor {

    public static String HEADER_PREFIX = "Bearer ";

    @Override
    public String extract(String header) {
        if (header == null || "".equals(header)) {
            throw new AuthenticationServiceException(
                "Authorization header cannot be blank!"
            );
        }

        if (header.length() < HEADER_PREFIX.length()) {
            throw new AuthenticationServiceException(
                "Invalid authorization header size."
            );
        }

        return header.substring(HEADER_PREFIX.length(), header.length());
    }
}