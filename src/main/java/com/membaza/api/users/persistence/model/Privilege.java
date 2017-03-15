package com.membaza.api.users.persistence.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Collection;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Data
@Entity
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "privileges")
    private Collection<Role> roles;

    public Privilege() {}

    public Privilege(final String name) {
        this.name = name;
    }

}
