package com.membaza.api.users.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public final class CommaSeparated {

    public static Stream<String> toStream(String str) {
        return Stream.of(str.split(","));
    }

    public static Set<String> toSet(String str) {
        return toStream(str).collect(Collectors.toSet());
    }

    private CommaSeparated() {}

}
