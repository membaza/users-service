package com.membaza.api.users.controller;

import com.membaza.api.users.controller.dto.RegisterDto;
import com.membaza.api.users.controller.dto.VerifyDto;
import com.membaza.api.users.persistence.Role;
import com.membaza.api.users.persistence.User;
import com.membaza.api.users.security.JwtAuthentication;
import com.membaza.api.users.service.date.DateService;
import com.membaza.api.users.service.email.EmailService;
import com.membaza.api.users.service.random.RandomService;
import com.membaza.api.users.throwable.InvalidVerificationCodeException;
import com.membaza.api.users.throwable.UserNotFoundException;
import com.membaza.api.users.util.CommaSeparated;
import com.mongodb.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.membaza.api.users.util.PredicateUtil.either;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.HttpStatus.*;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@RestController
@RequestMapping("/users")
public class RegisterController {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(RegisterController.class);

    private final DateService dates;
    private final RandomService random;
    private final PasswordEncoder passEncoder;
    private final Environment env;
    private final MongoTemplate mongo;
    private final EmailService email;

    public RegisterController(DateService dates,
                              RandomService random,
                              PasswordEncoder passEncoder,
                              Environment env,
                              MongoTemplate mongo,
                              EmailService email) {

        this.dates       = requireNonNull(dates);
        this.random      = requireNonNull(random);
        this.passEncoder = requireNonNull(passEncoder);
        this.env         = requireNonNull(env);
        this.mongo       = requireNonNull(mongo);
        this.email       = requireNonNull(email);
    }

    @PostMapping
    ResponseEntity<Void> register(@RequestBody RegisterDto register,
                                  @RequestParam(required = false) String lang,
                                  Authentication auth) {

        final User user = new User();

        user.setFirstname(register.getFirstname());
        user.setLastname(register.getLastname());
        user.setEmail(register.getEmail());
        user.setPassword(passEncoder.encode(register.getPassword()));
        user.setDateRegistered(dates.now());
        user.setEnabled(true);
        user.setRoles(defaultRoles());
        user.setPrivileges(defaultPrivileges());

        // If the user is logged in with CREATE_USER privilege, then we can set
        // confirmed to true immediately. Otherwise, the registration will have
        // to be confirmed by mail.
        if (can(auth, "CREATE_USER")) {
            user.setConfirmed(true);
        } else {
            user.setConfirmed(false);
            user.setUserCreationCode(random.nextString(40));
        }

        mongo.insert(user); // Exception is handled below.
        LOGGER.info("User '" + register.getEmail() + "' registered.");

        // Send confirmation email.
        final HttpStatus status;
        if (user.isConfirmed()) {
            status = CREATED;

        } else {
            final String url = env.getProperty("service.email.siteurl");
            final Map<String, String> args = new HashMap<>();
            args.put("firstname", register.getFirstname());
            args.put("lastname", register.getLastname());
            args.put("sitename", env.getProperty("service.email.sitename"));
            args.put("urls.register.verify", url + "/register/success");
            args.put("urls.register.cancel", url + "/register/cancel");
            args.put("userId", requireNonNull(user.getId()));
            args.put("code", user.getUserCreationCode());

            try {
                email.send(
                    register.getFirstname() + " " + register.getLastname(),
                    register.getEmail(),
                    "register_confirm",
                    lang,
                    args
                );
            } catch (final Throwable ex) {
                LOGGER.error("Failed to send email to " + register.getEmail());
                // Delete the user again since the user didn't get the email.
                mongo.remove(user);
                throw ex;
            }

            status = ACCEPTED;
        }

        return new ResponseEntity<>(status);
    }

    @PostMapping("/{userId}/verify")
    ResponseEntity<Void> registerVerify(@PathVariable String userId,
                                        @RequestBody(required = false) VerifyDto verification,
                                        Authentication auth) {

        // If no verification code is specified:
        if (verification == null) {
            if (can(auth, "VERIFY_CREATE_USER")) {
                if (!mongo.updateFirst(
                    query(where("id").is(userId)),
                    new Update()
                        .set("confirmed", true)
                        .unset("userCreationCode"),
                    User.class
                ).isUpdateOfExisting()) {
                    throw new UserNotFoundException();
                }
            } else {
                throw new InsufficientAuthenticationException(
                    "Either a verification code or the role " +
                    "'VERIFY_CREATE_USER' is required for this action."
                );
            }
        } else {
            if (!mongo.updateFirst(
                query(where("id").is(userId)
                          .and("userCreationCode").is(verification.getCode())
                ), new Update()
                    .set("confirmed", true)
                    .unset("userCreationCode"),
                User.class
            ).isUpdateOfExisting()) {
                throw new InvalidVerificationCodeException();
            }
        }

        // TODO: Send out email to user

        LOGGER.info("User '" + userId + "' verified registration.");
        return new ResponseEntity<>(OK);
    }

    @PostMapping("/{userId}/cancel")
    ResponseEntity<Void> registerCancel(@PathVariable String userId,
                                        @RequestBody(required = false) VerifyDto verification,
                                        Authentication auth) {

        // If no verification code is specified:
        if (verification == null) {
            if (can(auth, "CANCEL_CREATE_USER")) {
                if (!mongo.remove(
                    query(where("id").is(userId)
                        .and("confirmed").is(false)
                    ), User.class
                ).isUpdateOfExisting()) {
                    throw new UserNotFoundException();
                }
            } else {
                throw new InsufficientAuthenticationException(
                    "Either a verification code or the role " +
                    "'CANCEL_CREATE_USER' is required for this action."
                );
            }
        } else {
            if (!mongo.remove(
                query(where("id").is(userId)
                    .and("userCreationCode").is(verification.getCode())
                ), User.class
            ).isUpdateOfExisting()) {
                throw new InvalidVerificationCodeException();
            }
        }

        // TODO: Send out email to user

        LOGGER.info("User '" + userId + "' cancelled registration.");
        return new ResponseEntity<>(OK);
    }

    ////////////////////////////////////////////////////////////////////////////
    //                          Exception Handlers                            //
    ////////////////////////////////////////////////////////////////////////////

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(value = CONFLICT, reason = "An user with that mail already exists")
    public void duplicateKeyException() {}

    ////////////////////////////////////////////////////////////////////////////
    //                            Utility Methods                             //
    ////////////////////////////////////////////////////////////////////////////

    private Set<Role> defaultRoles() {
        final Set<String> names = CommaSeparated.toSet(env.getProperty("service.roles.default"));
        final Query rolesQuery = new Query(where("name").in(names));
        return new HashSet<>(mongo.find(rolesQuery, Role.class));
    }

    private Set<String> defaultPrivileges() {
        return CommaSeparated.toSet(env.getProperty("service.privileges.default"));
    }

    private boolean can(Authentication auth, String action) {
        final String privilege = action + "_PRIVILEGE";

        // If no authentication is specified, try the default role.
        if (auth == null) {
            return defaultPrivileges().contains(privilege)
            || defaultRoles().stream()
                .anyMatch(either(
                    r -> "ADMIN_ROLE".equals(r.getName()),
                    r -> r.getPrivileges().contains(privilege)
                ));
        } else {
            final JwtAuthentication jwtAuth = (JwtAuthentication) auth;

            return jwtAuth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(either("ADMIN_ROLE"::equals, privilege::equals));
        }
    }

    private String hateoasUrl(String path) {
        return env.getProperty("service.hateoas.baseurl") + "/users/" + path;
    }
}
