package com.membaza.api.users.persistence.repository;

import com.membaza.api.users.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    @Override
    void delete(User user);

}