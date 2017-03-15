package com.membaza.api.users.persistence.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Collection;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Data @Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToMany(mappedBy = "roles")
    private Collection<User> users;

    @ManyToMany
    @JoinTable(name = "roles_privileges",
               joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
               inverseJoinColumns = @JoinColumn(name = "privilege_id", referencedColumnName = "id")
    ) private Collection<Privilege> privileges;

    private String name;

    public Role() {}

    public Role(final String name) {
        this.name = name;
    }
}
