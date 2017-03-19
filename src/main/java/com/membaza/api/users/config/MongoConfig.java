package com.membaza.api.users.config;

import com.mongodb.Mongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoTemplate mongoTemplate(Mongo mongo) throws Exception {
        return new MongoTemplate(mongo, "membaza-users");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}