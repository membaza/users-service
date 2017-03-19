package com.membaza.api.users.component;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Component
public final class DateComponentImpl implements DateComponent {

    private final Clock clock;

    public DateComponentImpl() {
        this.clock = Clock.system(ZoneId.of("UTC"));
    }

    @Override
    public Instant instantNow() {
        return Instant.now(clock);
    }

    @Override
    public Date now() {
        return Date.from(instantNow());
    }

    @Override
    public Clock clock() {
        return clock;
    }
}
