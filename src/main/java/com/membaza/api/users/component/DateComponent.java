package com.membaza.api.users.component;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
public interface DateComponent {

    Instant instantNow();

    Date now();

    Clock clock();

}
