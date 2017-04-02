package com.membaza.api.users.service.jwt;

import com.membaza.api.users.persistence.User;
import com.membaza.api.users.security.JwtToken;
import io.jsonwebtoken.Claims;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface JwtService {

    JwtToken createLoginToken(User user);

    Claims validate(JwtToken token);

}
