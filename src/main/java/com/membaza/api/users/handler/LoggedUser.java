package com.membaza.api.users.handler;

import com.membaza.api.users.security.ActiveUserStore;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.util.List;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public final class LoggedUser implements HttpSessionBindingListener {

    private final ActiveUserStore activeUserStore;

    @Getter @Setter
    private String username;

    public LoggedUser(String username, ActiveUserStore activeUserStore) {
        this.username        = username;
        this.activeUserStore = activeUserStore;
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        final List<String> users = activeUserStore.getUsers();
        final LoggedUser user = (LoggedUser) event.getValue();

        if (!users.contains(user.getUsername())) {
            users.add(user.getUsername());
        }
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        final List<String> users = activeUserStore.getUsers();
        final LoggedUser user = (LoggedUser) event.getValue();

        if (users.contains(user.getUsername())) {
            users.remove(user.getUsername());
        }
    }
}