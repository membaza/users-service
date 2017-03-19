package com.membaza.api.users.service.purge;

import com.membaza.api.users.component.DateComponent;
import com.membaza.api.users.persistence.model.User;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

import static java.time.temporal.ChronoUnit.WEEKS;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Service
@EnableScheduling
public final class TokensPurgeService {

    private final DateComponent dates;
    private final MongoTemplate mongo;
    private final Environment env;

    public TokensPurgeService(
            final DateComponent dates,
            final MongoTemplate mongo,
            final Environment env) {

        this.dates = requireNonNull(dates);
        this.mongo = requireNonNull(mongo);
        this.env   = requireNonNull(env);
    }

    @Scheduled(cron = "0 0 1 * * *") // Run every hour...
    public void purgeExpired() {
        if (!Boolean.parseBoolean(env.getProperty("service.purge.enabled"))) {
            return;
        }

        final Date oneWeekAgo = Date.from(dates.instantNow().minus(1, WEEKS));

        // Remove users that hasn't confirmed their registration in one week.
        mongo.remove(query(
            where("confirmed").is(false)
                .and("dateRegistered").lt(oneWeekAgo)
        ), User.class);

        // Remove tokens older than one week.
        purgeExpired("emailChanges",    oneWeekAgo);
        purgeExpired("passwordChanges", oneWeekAgo);
        purgeExpired("userDeletions",   oneWeekAgo);
    }

    private void purgeExpired(String key, Date initiatedAfter) {
        mongo.updateMulti(new Query(), new Update().pull(
            key,
            query(where("initiated").lt(initiatedAfter))
        ), User.class);
    }
}