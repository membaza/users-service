package com.membaza.api.users.controller;

import com.membaza.api.users.persistence.Role;
import com.membaza.api.users.persistence.User;
import com.membaza.api.users.security.JwtAuthentication;
import com.membaza.api.users.throwable.OperationNotAllowedException;
import com.membaza.api.users.throwable.RoleNotFoundException;
import com.membaza.api.users.throwable.UserNotFoundException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import static com.membaza.api.users.util.PredicateUtil.either;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@RestController
@RequestMapping("/users")
public class RoleController {

    private final MongoTemplate mongo;

    public RoleController(MongoTemplate mongo) {
        this.mongo = requireNonNull(mongo);
    }

    @PostMapping("/users/{userId}/roles/{role}")
    void assignRole(@PathVariable String userId,
                    @PathVariable String role,
                    Authentication auth) {

        if (can(auth, "ASSIGN", role)) {
            final Role found = findRoleByName(role);
            if (found == null) throw new RoleNotFoundException();

            if (!mongo.updateFirst(
                    query(where("id").is(userId)),
                    new Update().addToSet("roles", found),
                    User.class
                ).isUpdateOfExisting()) {

                throw new UserNotFoundException();
            }
        } else {
            throw new OperationNotAllowedException();
        }
    }

    @DeleteMapping("/users/{userId}/roles/{role}")
    void revokeRole(@PathVariable String userId,
                    @PathVariable String role,
                    Authentication auth) {

        if (can(auth, "REVOKE", role)) {
            final Role found = findRoleByName(role);
            if (found == null) throw new RoleNotFoundException();

            if (!mongo.updateFirst(
                query(where("id").is(userId)),
                new Update().pull("roles", found),
                User.class
            ).isUpdateOfExisting()) {
                throw new UserNotFoundException();
            }
        } else {
            throw new OperationNotAllowedException();
        }
    }
    
    private boolean can(Authentication auth, String action, String role) {
        final JwtAuthentication jwtAuth = (JwtAuthentication) auth;
        final String privilege = action + "_" + role + "_PRIVILEGE";
        return jwtAuth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(either("ADMIN_ROLE"::equals, privilege::equals));
    }

    private Role findRoleByName(String name) {
        return mongo.findOne(query(where("name").is(name + "_ROLE")), Role.class);
    }
}
