package com.membaza.api.users.service.user;

import com.membaza.api.users.persistence.model.PasswordResetToken;
import com.membaza.api.users.persistence.model.User;
import com.membaza.api.users.persistence.repository.PasswordResetTokenRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Calendar;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Service
@Transactional
public final class UserSecurityServiceImpl implements UserSecurityService {

    private final PasswordResetTokenRepository repository;

    public UserSecurityServiceImpl(PasswordResetTokenRepository repository) {
        this.repository = requireNonNull(repository);
    }

    @Override
    public String validatePasswordResetToken(long id, String token) {
        final PasswordResetToken passToken = repository.findByToken(token);

        if ((passToken == null) || (passToken.getUser().getId() != id)) {
            return "invalidToken";
        }

        final Calendar cal = Calendar.getInstance();
        if ((passToken.getExpiryDate()
            .getTime() - cal.getTime()
            .getTime()) <= 0) {
            return "expired";
        }

        final User user = passToken.getUser();
        final Authentication auth = new UsernamePasswordAuthenticationToken(
            user, null,
            singletonList(new SimpleGrantedAuthority("CHANGE_PASSWORD_PRIVILEGE"))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        return null;
    }
}
