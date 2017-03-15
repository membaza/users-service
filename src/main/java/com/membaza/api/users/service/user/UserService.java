package com.membaza.api.users.service.user;

import com.membaza.api.users.dto.UserDto;
import com.membaza.api.users.persistence.model.PasswordResetToken;
import com.membaza.api.users.persistence.model.User;
import com.membaza.api.users.persistence.model.VerificationToken;
import com.membaza.api.users.throwable.UserAlreadyExistException;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
public interface UserService {

    User registerNewUserAccount(UserDto repository) throws UserAlreadyExistException;

    User getUser(String verificationToken);

    void saveRegisteredUser(User user);

    void deleteUser(User user);

    void createVerificationTokenForUser(User user, String token);

    VerificationToken getVerificationToken(String VerificationToken);

    VerificationToken generateNewVerificationToken(String token);

    void createPasswordResetTokenForUser(User user, String token);

    User findUserByEmail(String email);

    PasswordResetToken getPasswordResetToken(String token);

    User getUserByPasswordResetToken(String token);

    User getUserByID(long id);

    void changeUserPassword(User user, String password);

    boolean checkIfValidOldPassword(User user, String password);

    String validateVerificationToken(String token);

    String generateQRUrl(User user) throws UnsupportedEncodingException;

    User updateUser2FA(boolean use2FA);

    List<String> getUsersFromSessionRegistry();

}
