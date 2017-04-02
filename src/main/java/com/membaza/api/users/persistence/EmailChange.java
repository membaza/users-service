package com.membaza.api.users.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Data
public final class EmailChange {

    private @Id String id;
    private Date initiated;
    private String changeTo;

    @Indexed(unique=true)
    private String code;

}