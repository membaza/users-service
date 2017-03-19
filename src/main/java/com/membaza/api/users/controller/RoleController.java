package com.membaza.api.users.controller;

import org.springframework.web.bind.annotation.*;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@RestController
@RequestMapping("/users")
public class RoleController {

    @PostMapping("/users/{userId}/roles/{role}")
    void assignRole(@PathVariable String userId,
                    @PathVariable String role) {

    }

    @DeleteMapping("/users/{userId}/roles/{role}")
    void revokeRole(@PathVariable String userId,
                    @PathVariable String role) {

    }

}
