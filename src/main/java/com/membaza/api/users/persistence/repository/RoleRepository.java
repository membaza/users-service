package com.membaza.api.users.persistence.repository;

import com.membaza.api.users.persistence.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface RoleRepository extends MongoRepository<Role, Long> {

    Role findByName(String name);

}
