package com.membaza.api.users.controller;

import com.membaza.api.users.controller.dto.EmailDto;
import com.membaza.api.users.controller.dto.PasswordDto;
import com.membaza.api.users.controller.dto.VerifyDto;
import org.springframework.web.bind.annotation.*;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@RestController
@RequestMapping("/users")
public class UserController {



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

    @PutMapping("/{userId}/email")
    void changeEmail(@PathVariable String userId,
                     @RequestBody EmailDto email) {

    }

    @PutMapping("/{userId}/email/verify")
    void changeEmailVerify(@PathVariable String userId,
                           @RequestBody VerifyDto verification) {

    }

    @PutMapping("/{userId}/email/cancel")
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
}
