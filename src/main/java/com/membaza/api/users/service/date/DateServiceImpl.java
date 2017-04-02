package com.membaza.api.users.service.date;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Service
public final class DateServiceImpl implements DateService {

    private final Clock clock;

    public DateServiceImpl() {
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
