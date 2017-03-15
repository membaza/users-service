package com.membaza.api.users.persistence.repository;

import com.membaza.api.users.persistence.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String name);

    @Override
    void delete(Role role);

}
