package com.membaza.api.users.persistence.repository;

import com.membaza.api.users.persistence.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface UserRepository extends MongoRepository<User, String> {

    User findByEmail(String email);

}