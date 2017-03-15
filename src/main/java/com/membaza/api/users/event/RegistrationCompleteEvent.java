package com.membaza.api.users.event;

import com.membaza.api.users.persistence.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Getter
@SuppressWarnings("serial")
public final class RegistrationCompleteEvent extends ApplicationEvent {

    private final String appUrl;
    private final Locale locale;
    private final User user;

    public RegistrationCompleteEvent(
            final User user,
            final Locale locale,
            final String appUrl) {

        super(user);
        this.user   = user;
        this.locale = locale;
        this.appUrl = appUrl;
    }
}
