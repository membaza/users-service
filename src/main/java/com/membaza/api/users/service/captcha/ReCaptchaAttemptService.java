package com.membaza.api.users.service.captcha;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Service("reCaptchaAttemptService")
public final class ReCaptchaAttemptService {

    private static final int MAX_ATTEMPT = 4;

    private final LoadingCache<String, Integer> attemptsCache;

    public ReCaptchaAttemptService() {
        attemptsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(4, TimeUnit.HOURS)
            .build(new CacheLoader<String, Integer>() {
                @Override
                public Integer load(final String key) {
                    return 0;
                }
            });
    }

    public void reCaptchaSucceeded(final String key) {
        attemptsCache.invalidate(key);
    }

    public void reCaptchaFailed(final String key) {
        final int attempts = attemptsCache.getUnchecked(key) + 1;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(final String key) {
        return attemptsCache.getUnchecked(key) >= MAX_ATTEMPT;
    }
}