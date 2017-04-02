package com.membaza.api.users.controller;

import com.membaza.api.users.service.date.DateService;
import com.membaza.api.users.service.random.RandomService;
import com.membaza.api.users.controller.dto.RegisterDto;
import com.membaza.api.users.controller.dto.VerifyDto;
import com.membaza.api.users.persistence.Role;
import com.membaza.api.users.persistence.User;
import com.membaza.api.users.service.email.EmailService;
import com.membaza.api.users.service.text.TextService;
import com.membaza.api.users.throwable.InvalidVerificationCodeException;
import com.membaza.api.users.util.CommaSeparated;
import com.mongodb.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.HttpStatus.CONFLICT;

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
    private final TextService text;

    public RegisterController(DateService dates,
                              RandomService random,
                              PasswordEncoder passEncoder,
                              Environment env,
                              MongoTemplate mongo,
                              EmailService email,
                              TextService text) {

        this.dates       = requireNonNull(dates);
        this.random      = requireNonNull(random);
        this.passEncoder = requireNonNull(passEncoder);
        this.env         = requireNonNull(env);
        this.mongo       = requireNonNull(mongo);
        this.email       = requireNonNull(email);
        this.text        = requireNonNull(text);
    }

    @PostMapping
    void register(@RequestBody RegisterDto register,
                  @RequestParam(required = false) String lang) {

        final String language = getLanguage(lang);
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

        final String code = random.nextString(40);
        user.setConfirmed(false);
        user.setUserCreationCode(code);
        mongo.insert(user); // Exception is handled below.

        LOGGER.info("User '" + register.getEmail() + "' registered.");

        final String url = env.getProperty("service.email.siteurl");
        final Map<String, String> emailArgs = new HashMap<>();
        emailArgs.put("firstname", register.getFirstname());
        emailArgs.put("lastname", register.getLastname());
        emailArgs.put("sitename", env.getProperty("service.email.sitename"));
        emailArgs.put("urls.register.verify", url + "/register/success");
        emailArgs.put("urls.register.cancel", url + "/register/cancel");
        emailArgs.put("userId", requireNonNull(user.getId()));
        emailArgs.put("code", code);

        try {
            email.send(
                register.getFirstname() + " " + register.getLastname(),
                register.getEmail(),
                "register_confirm",
                language,
                emailArgs
            );
        } catch (final Throwable ex) {
            LOGGER.error("Failed to send email to " + register.getEmail());
            // Delete the user again since the user didn't get the email.
            mongo.remove(user);
            throw ex;
        }
    }

    @PostMapping("/{userId}/verify")
    void registerVerify(@PathVariable String userId,
                        @RequestBody VerifyDto verification) {

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

        LOGGER.info("User '" + userId + "' verified registration.");
    }

    @PostMapping("/{userId}/cancel")
    void registerCancel(@PathVariable String userId,
                        @RequestBody VerifyDto verification) {

        if (!mongo.remove(
            query(where("id").is(userId)
                      .and("userCreationCode").is(verification.getCode())
            ), User.class
        ).isUpdateOfExisting()) {
            throw new InvalidVerificationCodeException();
        }

        LOGGER.info("User '" + userId + "' cancelled registration.");
    }

    ////////////////////////////////////////////////////////////////////////////
    //                          Exception Handlers                            //
    ////////////////////////////////////////////////////////////////////////////

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(value = CONFLICT, reason = "An user with that mail already exists")
    public void duplicateKeyException() {}

    private Set<Role> defaultRoles() {
        final Set<String> names = CommaSeparated.toSet(env.getProperty("service.roles.default"));
        final Query rolesQuery = new Query(where("name").in(names));
        return new HashSet<>(mongo.find(rolesQuery, Role.class));
    }

    private Set<String> defaultPrivileges() {
        return CommaSeparated.toSet(env.getProperty("service.privileges.default"));
    }

    private String getLanguage(String lang) {
        if (lang == null) {
            return "en";
        } else {
            if (text.has(lang)) {
                return lang;
            } else {
                throw new IllegalArgumentException(
                    "'" + lang + "' is not a valid language."
                );
            }
        }
    }
}
