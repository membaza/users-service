package com.membaza.api.users.service.date;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface DateService {

    Instant instantNow();

    Date now();

    Clock clock();

}
