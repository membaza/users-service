package com.membaza.api.users.util;

import java.util.function.Predicate;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public final class PredicateUtil {

    public static <T> Predicate<T> either(Predicate<T> first, Predicate<T> second) {
        return first.or(second);
    }

    private PredicateUtil() {}
}
