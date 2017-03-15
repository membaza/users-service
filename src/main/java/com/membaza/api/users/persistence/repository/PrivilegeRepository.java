package com.membaza.api.users.persistence.repository;

import com.membaza.api.users.persistence.model.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {

    Privilege findByName(String name);

    @Override
    void delete(Privilege privilege);

}
