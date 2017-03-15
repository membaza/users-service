package com.membaza.api.users.service;

import com.membaza.api.users.persistence.model.Privilege;
import com.membaza.api.users.persistence.model.Role;
import com.membaza.api.users.persistence.model.User;
import com.membaza.api.users.persistence.repository.UserRepository;
import com.membaza.api.users.service.login.LoginAttemptService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Transactional
@Service("userDetailsService")
public final class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;

    public UserDetailsServiceImpl(
            final UserRepository userRepository,
            final LoginAttemptService loginAttemptService,
            final HttpServletRequest request) {

        this.userRepository      = requireNonNull(userRepository);
        this.loginAttemptService = requireNonNull(loginAttemptService);
        this.request             = requireNonNull(request);
    }

    @Override
    public UserDetails loadUserByUsername(final String email)
    throws UsernameNotFoundException {

        final String ip = getClientIp();
        if (loginAttemptService.isBlocked(ip)) {
            throw new RuntimeException("blocked");
        }

        try {
            final User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new UsernameNotFoundException(
                    "No user found with username: " + email
                );
            }

            return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                getAuthorities(user.getRoles())
            );

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<? extends GrantedAuthority>
    getAuthorities(final Collection<Role> roles) {

        return getGrantedAuthorities(getPrivileges(roles));
    }

    private List<String> getPrivileges(final Collection<Role> roles) {
        final List<String> privileges = new ArrayList<>();
        final List<Privilege> collection = new ArrayList<>();

        for (final Role role : roles) {
            collection.addAll(role.getPrivileges());
        }

        for (final Privilege item : collection) {
            privileges.add(item.getName());
        }

        return privileges;
    }

    private List<GrantedAuthority> getGrantedAuthorities(final List<String> privileges) {
        final List<GrantedAuthority> authorities = new ArrayList<>();

        for (final String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }

        return authorities;
    }

    private String getClientIp() {
        final String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0];
    }
}
