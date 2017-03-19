package com.membaza.api.users.security.jwt.extractor;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Component
public final class JwtHeaderTokenExtractor
implements TokenExtractor {

    public static String HEADER_PREFIX = "Bearer ";

    @Override
    public String extract(String header) {
        if (StringUtils.isBlank(header)) {
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
