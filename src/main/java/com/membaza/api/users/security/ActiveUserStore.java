package com.membaza.api.users.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Component
@Getter @Setter
public final class ActiveUserStore {

    public List<String> users;

    public ActiveUserStore() {
        users = new ArrayList<>();
    }

}