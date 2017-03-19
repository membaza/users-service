package com.membaza.api.users.security.jwt.extractor;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface TokenExtractor {
    String extract(String payload);
}