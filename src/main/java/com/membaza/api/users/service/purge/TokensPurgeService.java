package com.membaza.api.users.service.purge;

import com.membaza.api.users.service.date.DateService;
import com.membaza.api.users.persistence.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER =
        LoggerFactory.getLogger(TokensPurgeService.class);

    private final DateService dates;
    private final MongoTemplate mongo;
    private final Environment env;

    public TokensPurgeService(
            final DateService dates,
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
        final int removed = mongo.remove(query(
            where("confirmed").is(false)
                .and("dateRegistered").lt(oneWeekAgo)
        ), User.class).getN();

        if (removed > 0) {
            LOGGER.info("Removed " + removed + " unverified accounts.");
        }

        // Remove tokens older than one week.
        purgeExpired("emailChanges",    oneWeekAgo);
        purgeExpired("passwordChanges", oneWeekAgo);
        purgeExpired("userDeletions",   oneWeekAgo);
    }

    private void purgeExpired(String key, Date initiatedAfter) {
        final int purged = mongo.updateMulti(new Query(), new Update().pull(
            key,
            query(where("initiated").lt(initiatedAfter))
        ), User.class).getN();

        if (purged > 0) {
            LOGGER.info("Purged " + purged + " expired " + key + " tokens.");
        }
    }
}