package com.membaza.api.users.service;

import com.membaza.api.users.dto.UserDto;
import com.membaza.api.users.persistence.model.PasswordResetToken;
import com.membaza.api.users.persistence.model.User;
import com.membaza.api.users.persistence.model.VerificationToken;
import com.membaza.api.users.persistence.repository.PasswordResetTokenRepository;
import com.membaza.api.users.persistence.repository.RoleRepository;
import com.membaza.api.users.persistence.repository.UserRepository;
import com.membaza.api.users.persistence.repository.VerificationTokenRepository;
import com.membaza.api.users.throwable.UserAlreadyExistException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Service
@Transactional
public final class UserServiceImpl implements UserService {

    private static final String TOKEN_INVALID = "invalidToken";
    private static final String TOKEN_EXPIRED = "expired";
    private static final String TOKEN_VALID   = "valid";
    private static final String QR_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
    private static final String APP_NAME  = "UsersService";

    private final UserRepository repository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final SessionRegistry sessionRegistry;

    public UserServiceImpl(UserRepository repository,
                           VerificationTokenRepository tokenRepository,
                           PasswordResetTokenRepository passwordTokenRepository,
                           PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository,
                           SessionRegistry sessionRegistry) {

        this.repository              = repository;
        this.tokenRepository         = tokenRepository;
        this.passwordTokenRepository = passwordTokenRepository;
        this.passwordEncoder         = passwordEncoder;
        this.roleRepository          = roleRepository;
        this.sessionRegistry         = sessionRegistry;
    }

    @Override
    public User registerNewUserAccount(final UserDto userRepository) {
        if (emailExist(userRepository.getEmail())) {
            throw new UserAlreadyExistException(
                "There is an account with that email address: " +
                userRepository.getEmail()
            );
        }

        final User user = new User();
        user.setFirstName(userRepository.getFirstName());
        user.setLastName(userRepository.getLastName());
        user.setPassword(passwordEncoder.encode(userRepository.getPassword()));
        user.setEmail(userRepository.getEmail());
        user.setUsing2FA(userRepository.isUsing2FA());
        user.setRoles(singletonList(roleRepository.findByName("ROLE_USER")));
        return repository.save(user);
    }

    @Override
    public User getUser(final String verificationToken) {
        final VerificationToken token =
            tokenRepository.findByToken(verificationToken);

        if (token != null) {
            return token.getUser();
        }

        return null;
    }

    @Override
    public VerificationToken getVerificationToken(
            final String VerificationToken) {

        return tokenRepository.findByToken(VerificationToken);
    }

    @Override
    public void saveRegisteredUser(final User user) {
        repository.save(user);
    }

    @Override
    public void deleteUser(final User user) {
        final VerificationToken verificationToken =
            tokenRepository.findByUser(user);

        if (verificationToken != null) {
            tokenRepository.delete(verificationToken);
        }

        final PasswordResetToken passwordToken =
            passwordTokenRepository.findByUser(user);

        if (passwordToken != null) {
            passwordTokenRepository.delete(passwordToken);
        }

        repository.delete(user);
    }

    @Override
    public void createVerificationTokenForUser(
            final User user,
            final String token) {

        final VerificationToken myToken = new VerificationToken(token, user);
        tokenRepository.save(myToken);
    }

    @Override
    public VerificationToken generateNewVerificationToken(
            final String existingVerificationToken) {

        VerificationToken vToken =
            tokenRepository.findByToken(existingVerificationToken);

        vToken.updateToken(UUID.randomUUID().toString());
        vToken = tokenRepository.save(vToken);

        return vToken;
    }

    @Override
    public void createPasswordResetTokenForUser(
            final User user,
            final String token) {

        final PasswordResetToken myToken = new PasswordResetToken(token, user);
        passwordTokenRepository.save(myToken);
    }

    @Override
    public User findUserByEmail(final String email) {
        return repository.findByEmail(email);
    }

    @Override
    public PasswordResetToken getPasswordResetToken(final String token) {
        return passwordTokenRepository.findByToken(token);
    }

    @Override
    public User getUserByPasswordResetToken(final String token) {
        return passwordTokenRepository.findByToken(token).getUser();
    }

    @Override
    public User getUserByID(final long id) {
        return repository.findOne(id);
    }

    @Override
    public void changeUserPassword(final User user, final String password) {
        user.setPassword(passwordEncoder.encode(password));
        repository.save(user);
    }

    @Override
    public boolean checkIfValidOldPassword(
            final User user,
            final String oldPassword) {

        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    @Override
    public String validateVerificationToken(String token) {
        final VerificationToken verificationToken =
            tokenRepository.findByToken(token);

        if (verificationToken == null) {
            return TOKEN_INVALID;
        }

        final User user = verificationToken.getUser();
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate()
                .getTime() - cal.getTime().getTime()) <= 0) {
            tokenRepository.delete(verificationToken);
            return TOKEN_EXPIRED;
        }

        user.setEnabled(true);
        // tokenRepository.delete(verificationToken);
        repository.save(user);
        return TOKEN_VALID;
    }

    @Override
    public String generateQRUrl(User user) throws UnsupportedEncodingException {
        return QR_PREFIX + URLEncoder.encode(String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s",
            APP_NAME, user.getEmail(), user.getSecret(), APP_NAME
        ), "UTF-8");
    }

    @Override
    public User updateUser2FA(boolean use2FA) {
        final Authentication curAuth = SecurityContextHolder.getContext()
            .getAuthentication();

        User currentUser = (User) curAuth.getPrincipal();
        currentUser.setUsing2FA(use2FA);
        currentUser = repository.save(currentUser);
        final Authentication auth = new UsernamePasswordAuthenticationToken(
            currentUser, currentUser.getPassword(), curAuth.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        return currentUser;
    }

    @Override
    public List<String> getUsersFromSessionRegistry() {
        return sessionRegistry.getAllPrincipals().stream()
            .filter((u) -> !sessionRegistry.getAllSessions(u, false).isEmpty())
            .map(Object::toString)
            .collect(Collectors.toList());
    }

    private boolean emailExist(final String email) {
        return repository.findByEmail(email) != null;
    }
}