package com.membaza.api.users.service.text;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
public interface TextService {

    Map<String, String> EMPTY = emptyMap();

    boolean has(String language);

    default String get(String key, String language) {
        return get(key, language, EMPTY);
    }

    String get(String key, String language, Map<String, String> args);

    default String format(String text, String language) {
        return format(text, language, EMPTY);
    }

    String format(String text, String language, Map<String, String> args);

}
