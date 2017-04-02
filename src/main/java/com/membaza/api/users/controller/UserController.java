package com.membaza.api.users.controller;

import com.membaza.api.users.controller.dto.EmailDto;
import com.membaza.api.users.controller.dto.PasswordDto;
import com.membaza.api.users.controller.dto.VerifyDto;
import com.membaza.api.users.persistence.User;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final Environment env;
    private final MongoTemplate mongo;

    public UserController(Environment env, MongoTemplate mongo) {
        this.env   = requireNonNull(env);
        this.mongo = requireNonNull(mongo);
    }

    ////////////////////////////////////////////////////////////////////////////
    //                              Delete User                               //
    ////////////////////////////////////////////////////////////////////////////

    @DeleteMapping("/{userId}")
    void delete(@PathVariable String userId) {

    }

    @DeleteMapping("/{userId}/verify")
    void deleteVerify(@PathVariable String userId,
                      @RequestBody VerifyDto verification) {

    }

    @DeleteMapping("/{userId}/cancel")
    void deleteCancel(@PathVariable String userId,
                      @RequestBody VerifyDto verification) {

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
    //                                Debugging                               //
    ////////////////////////////////////////////////////////////////////////////

    @GetMapping
    List<User> getUsers() {
        return mongo.findAll(User.class);
    }
}
