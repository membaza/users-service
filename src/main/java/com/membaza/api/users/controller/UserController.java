package com.membaza.api.users.controller;

import com.membaza.api.users.controller.dto.EmailDto;
import com.membaza.api.users.controller.dto.PasswordDto;
import com.membaza.api.users.controller.dto.VerifyDto;
import com.membaza.api.users.persistence.User;
import com.membaza.api.users.persistence.UserDeletion;
import com.membaza.api.users.security.JwtAuthentication;
import com.membaza.api.users.service.date.DateService;
import com.membaza.api.users.service.email.EmailService;
import com.membaza.api.users.service.random.RandomService;
import com.membaza.api.users.throwable.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.membaza.api.users.util.PredicateUtil.either;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(UserController.class);

    private final Environment env;
    private final MongoTemplate mongo;
    private final DateService dates;
    private final RandomService random;
    private final EmailService email;

    public UserController(Environment env,
                          MongoTemplate mongo,
                          DateService dates,
                          RandomService random,
                          EmailService email) {

        this.env    = requireNonNull(env);
        this.mongo  = requireNonNull(mongo);
        this.dates  = requireNonNull(dates);
        this.random = requireNonNull(random);
        this.email  = requireNonNull(email);
    }

    ////////////////////////////////////////////////////////////////////////////
    //                              Delete User                               //
    ////////////////////////////////////////////////////////////////////////////

    @DeleteMapping("/{userId}")
    ResponseEntity<Void> delete(@PathVariable String userId,
                                @RequestParam(required = false) String lang,
                                Authentication auth) {

        final JwtAuthentication jwtAuth = (JwtAuthentication) auth;

        if (can(auth, "DELETE_USER")) {
            if (!mongo.remove(query(where("id").is(userId)), User.class)
                    .isUpdateOfExisting()) {
                throw new UserNotFoundException();
            }

            return new ResponseEntity<>(NO_CONTENT);
        } else if (jwtAuth != null
               &&  userId.equals(jwtAuth.getPrincipal().getId())) {

            // Create a deletion token in the database.
            final UserDeletion token = new UserDeletion();
            token.setInitiated(dates.now());
            token.setCode(random.nextString(40));
            mongo.insert(token);

            // Send the code to the user as an email.
            final String url = env.getProperty("service.email.siteurl");
            final Map<String, String> args = new HashMap<>();
            args.put("sitename", env.getProperty("service.email.sitename"));
            args.put("urls.delete.verify", url + "/unregister/success");
            args.put("urls.delete.cancel", url + "/unregister/cancel");
            args.put("userId", userId);
            args.put("code", token.getCode());

            final User user = mongo.findOne(query(where("id")
                 .is(jwtAuth.getPrincipal().getId())), User.class);

            try {
                email.send(
                    user.getFirstname() + " " + user.getLastname(),
                    user.getEmail(),
                    "delete_confirm",
                    lang, args
                );
            } catch (final Throwable ex) {
                LOGGER.error("Failed to send email to " + user.getEmail());
                // Delete the user again since the user didn't get the email.
                mongo.remove(token);
                throw ex;
            }

            return new ResponseEntity<>(ACCEPTED);
        } else {
            throw new InsufficientAuthenticationException(
                "Deleting accounts requires either the account to be your " +
                "own or the 'DELETE_USER' privilege."
            );
        }
    }

    @DeleteMapping("/{userId}/verify")
    void deleteVerify(@PathVariable String userId,
                      @RequestBody(required = false) VerifyDto verification) {

    }

    @DeleteMapping("/{userId}/cancel")
    void deleteCancel(@PathVariable String userId,
                      @RequestBody(required = false) VerifyDto verification) {

    }

    ////////////////////////////////////////////////////////////////////////////
    //                             Change Email                               //
    ////////////////////////////////////////////////////////////////////////////

    @PutMapping("/{userId}/mail")
    void changeEmail(@PathVariable String userId,
                     @RequestBody EmailDto email) {

    }

    @PutMapping("/{userId}/mail/verify")
    void changeEmailVerify(@PathVariable String userId,
                           @RequestBody VerifyDto verification) {

    }

    @PutMapping("/{userId}/mail/cancel")
    void changeEmailCancel(@PathVariable String userId,
                           @RequestBody VerifyDto verification) {

    }

    ////////////////////////////////////////////////////////////////////////////
    //                           Change Password                              //
    ////////////////////////////////////////////////////////////////////////////

    @PutMapping("/{userId}/password")
    void changePassword(@PathVariable String userId,
                        @RequestBody PasswordDto email) {

    }

    @PutMapping("/{userId}/password/verify")
    void changePasswordVerify(@PathVariable String userId,
                              @RequestBody PasswordDto verification) {

    }

    @PutMapping("/{userId}/password/cancel")
    void changePasswordCancel(@PathVariable String userId,
                              @RequestBody PasswordDto verification) {

    }

    @PutMapping("/{userId}/password/reset")
    void changePassword(@PathVariable String userId) {

    }

    ////////////////////////////////////////////////////////////////////////////
    //                                Get Users                               //
    ////////////////////////////////////////////////////////////////////////////

    @GetMapping
    List<User> getUsers() {
        return mongo.findAll(User.class);
    }

    @GetMapping("/{userId}")
    User getUser(@PathVariable String userId) {
        return mongo.findOne(query(where("id").is(userId)), User.class);
    }

    ////////////////////////////////////////////////////////////////////////////
    //                             Utility Methods                            //
    ////////////////////////////////////////////////////////////////////////////

    private boolean can(Authentication auth, String action) {
        final JwtAuthentication jwtAuth = (JwtAuthentication) auth;
        final String privilege = action + "_PRIVILEGE";
        return jwtAuth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(either("ADMIN_ROLE"::equals, privilege::equals));
    }
}
