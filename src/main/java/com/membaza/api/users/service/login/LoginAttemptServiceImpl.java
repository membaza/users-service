package com.membaza.api.users.service.login;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Service
public final class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final int MAX_ATTEMPT = 10;

    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptServiceImpl() {
        attemptsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(new CacheLoader<String, Integer>() {

            @Override
            public Integer load(final String key) {
                return 0;
            }
        });
    }

    @Override
    public void loginSucceeded(final String key) {
        attemptsCache.invalidate(key);
    }

    @Override
    public void loginFailed(final String key) {
        int attempts;
        try {
            attempts = attemptsCache.get(key);
        } catch (final ExecutionException e) {
            attempts = 1;
        }
        attemptsCache.put(key, attempts);
    }

    @Override
    public boolean isBlocked(final String key) {
        try {
            return attemptsCache.get(key) >= MAX_ATTEMPT;
        } catch (final ExecutionException e) {
            return false;
        }
    }
}