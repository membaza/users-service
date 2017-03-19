package com.membaza.api.users.component;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Component
public final class RandomComponentImpl implements RandomComponent {

    private final SecureRandom random;

    public RandomComponentImpl() {
        random = new SecureRandom();
        random.setSeed(System.currentTimeMillis());
    }

    RandomComponentImpl(SecureRandom random) {
        this.random = requireNonNull(random);
    }

    @Override
    public long nextLong() {
        return random.nextLong();
    }

    @Override
    public String nextString(int length) {
        final StringBuilder str = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            str.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return str.toString();
    }

    private final static String ALPHABET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
}