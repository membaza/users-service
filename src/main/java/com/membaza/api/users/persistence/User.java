package com.membaza.api.users.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Data
@Document(collection = "users")
public final class User {

    @Id
    private String id;

    private @NotNull String firstname;
    private @NotNull String lastname;

    @Indexed(unique=true)
    private @NotNull String email;
    private @NotNull String password;
    private @NotNull Set<Role> roles;
    private @NotNull Set<String> privileges;
    private @NotNull Date dateRegistered;
    private boolean confirmed;
    private boolean enabled;

    @Indexed(unique=true)
    private String userCreationCode;
    private Set<EmailChange> emailChanges;
    private Set<PasswordChange> passwordChanges;
    private Set<UserDeletion> userDeletions;

    @JsonIgnore
    public String getPassword(){
        return password;
    }

    @JsonIgnore
    public String getUserCreationCode(){
        return userCreationCode;
    }
}
