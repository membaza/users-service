package com.membaza.api.users.controller;

import com.membaza.api.users.component.DateComponent;
import com.membaza.api.users.component.RandomComponent;
import com.membaza.api.users.controller.dto.RegisterDto;
import com.membaza.api.users.controller.dto.VerifyDto;
import com.membaza.api.users.persistence.model.Role;
import com.membaza.api.users.persistence.model.User;
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

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.HttpStatus.CONFLICT;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@RestController
@RequestMapping("/users")
public class RegisterController {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(RegisterController.class);

    private final DateComponent dates;
    private final RandomComponent random;
    private final PasswordEncoder passEncoder;
    private final Environment env;
    private final MongoTemplate mongo;

    public RegisterController(DateComponent dates,
                              RandomComponent random,
                              PasswordEncoder passEncoder,
                              Environment env,
                              MongoTemplate mongo) {

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
        mongo.insert(user); // Exception is handled below.

        // TODO: Send out confirmation email
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
    }

    ////////////////////////////////////////////////////////////////////////////
    //                          Exception Handlers                            //
    ////////////////////////////////////////////////////////////////////////////

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(value = CONFLICT, reason = "An user with that email already exists")
    public void duplicateKeyException() {}

    private Set<Role> defaultRoles() {
        final Set<String> names = CommaSeparated.toSet(env.getProperty("service.roles.default"));
        final Query rolesQuery = new Query(where("name").in(names));
        return new HashSet<>(mongo.find(rolesQuery, Role.class));
    }

    private Set<String> defaultPrivileges() {
        return CommaSeparated.toSet(env.getProperty("service.privileges.default"));
    }
}
