package com.membaza.api.users.service.purge;

import com.membaza.api.users.persistence.repository.PasswordResetTokenRepository;
import com.membaza.api.users.persistence.repository.VerificationTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Date;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Service
@Transactional
public final class TokensPurgeService {

    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordTokenRepository;

    public TokensPurgeService(
            VerificationTokenRepository tokenRepository,
            PasswordResetTokenRepository passwordTokenRepository) {

        this.tokenRepository         = requireNonNull(tokenRepository);
        this.passwordTokenRepository = requireNonNull(passwordTokenRepository);
    }

    @Scheduled(cron = "${purge.cron.expression}")
    public void purgeExpired() {
        final Date now = Date.from(Instant.now());
        passwordTokenRepository.deleteAllExpiredSince(now);
        tokenRepository.deleteAllExpiredSince(now);
    }
}