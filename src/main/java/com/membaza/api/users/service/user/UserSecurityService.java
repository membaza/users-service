package com.membaza.api.users.service.user;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface UserSecurityService {

    String validatePasswordResetToken(long id, String token);

}