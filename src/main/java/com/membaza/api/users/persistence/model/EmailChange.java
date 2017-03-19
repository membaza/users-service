package com.membaza.api.users.persistence.model;

import lombok.Data;

import javax.persistence.Id;
import java.util.Date;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Data
public final class EmailChange {

    @Id
    private String id;
    private Date initiated;
    private String changeTo;

}