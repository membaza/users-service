package com.membaza.api.users.controller;

import com.membaza.api.users.component.DateComponent;
import com.membaza.api.users.component.RandomComponent;
import com.membaza.api.users.controller.dto.RegisterDto;
import com.membaza.api.users.controller.dto.VerifyDto;
import com.membaza.api.users.persistence.model.Role;
import com.membaza.api.users.persistence.model.User;
import com.membaza.api.users.persistence.repository.UserRepository;
import com.membaza.api.users.throwable.UserAlreadyExistException;
import com.membaza.api.users.throwable.UserNotFoundException;
import com.membaza.api.users.util.CommaSeparated;
import com.mongodb.DuplicateKeyException;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@RestController
@RequestMapping("/users")
public class RegisterController {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(RegisterController.class);

    private final UserRepository users;
    private final DateComponent dates;
    private final RandomComponent random;
    private final PasswordEncoder passEncoder;
    private final Environment env;
    private final MongoTemplate mongo;

    public RegisterController(UserRepository users,
                              DateComponent dates,
                              RandomComponent random,
                              PasswordEncoder passEncoder,
                              Environment env,
                              MongoTemplate mongo) {

        this.users       = requireNonNull(users);
        this.dates       = requireNonNull(dates);
        this.random      = requireNonNull(random);
        this.passEncoder = requireNonNull(passEncoder);
        this.env         = requireNonNull(env);
        this.mongo       = requireNonNull(mongo);
    }

    @PostMapping
    void register(@RequestBody RegisterDto register) {

        final User user = new User();

        user.setEmail(register.getEmail());
        user.setPassword(passEncoder.encode(register.getPassword()));
        user.setDateRegistered(dates.now());
        user.setEnabled(true);
        user.setRoles(defaultRoles());
        user.setPrivileges(defaultPrivileges());

        // If the user is logged in with CREATE_USER privilege, then we can set
        // confirmed to true immediately. Otherwise, the registration will have
        // to be confirmed by email.

        user.setConfirmed(false);
        user.setUserCreationCode(random.nextString(40));

        try {
            users.save(user);
        } catch (final DuplicateKeyException ex) {
            LOGGER.warn("Attempted to register user with email '%s' that " +
                        "already exists.", register.getEmail());

            throw new UserAlreadyExistException(
                "User with email '" + register.getEmail() + "' already exists.",
                ex
            );
        }

        // TODO: Send out confirmation email
    }

    @PostMapping("/{userId}/verify")
    void registerVerify(@PathVariable String userId,
                        @RequestBody VerifyDto verification) {

        final User user = users.findOne(userId);
        if (user == null
        || !Objects.equals(user.getUserCreationCode(),
                           verification.getCode())) {

            throw new UserNotFoundException(
                "Verification code is either invalid or expired."
            );
        }

        user.setConfirmed(true);
        user.setUserCreationCode(null);

        users.save(user);
    }

    @PostMapping("/{userId}/cancel")
    void registerCancel(@PathVariable String userId,
                        @RequestBody VerifyDto verification) {

        final User user = users.findOne(userId);
        if (user == null
            || !Objects.equals(user.getUserCreationCode(),
                               verification.getCode())) {

            throw new UserNotFoundException(
                "Verification code is either invalid or expired."
            );
        }

        users.delete(user);
    }

    private Set<Role> defaultRoles() {
        final Set<String> names = CommaSeparated.toSet(env.getProperty("service.roles.default"));
        final Query rolesQuery = new Query(Criteria.where("name").in(names));
        return new HashSet<>(mongo.find(rolesQuery, Role.class));
    }

    private Set<String> defaultPrivileges() {
        return CommaSeparated.toSet(env.getProperty("service.privileges.default"));
    }

    private void assertClaim(Claims claims, String privilege) {
        @SuppressWarnings("unchecked")
        final Collection<String> privileges =
            (Collection<String>) claims.get("privileges");

        if (!privileges.contains(privilege)) {
            throw new IllegalArgumentException(
                "Specified verification code is not valid for this action."
            );
        }
    }
}
