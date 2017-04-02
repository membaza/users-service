package com.membaza.api.users.service.email;

import com.membaza.api.users.service.text.TextService;
import lombok.Data;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Service
public final class EmailServiceImpl implements EmailService {

    private static final String EMAIL_FILE = "/mail/*.html";

    private final Environment env;
    private final TextService text;
    private final RestTemplate mailgun;

    EmailServiceImpl(Environment env,
                     TextService text) {

        this.env     = requireNonNull(env);
        this.text    = requireNonNull(text);
        this.mailgun = new RestTemplate();

        this.mailgun.setMessageConverters(asList(
            new FormHttpMessageConverter(),
            new StringHttpMessageConverter()
        ));

        this.mailgun.getInterceptors().add(new BasicAuthorizationInterceptor(
            "api", env.getProperty("mailgun.apiKey")
        ));
    }

    @Override
    public void send(String to,
                     String toEmail,
                     String template,
                     String language,
                     Map<String, String> args) {

        final String subject = text.get(template.replace('_', '.') + ".subject", language);
        final String body    = body(template, language, args);
        send(subject, to, toEmail, body);
    }

    private String body(String template, String language, Map<String, String> args) {
        final String templateFile = EMAIL_FILE.replace("*", template);

        try {
            return Files.lines(Paths.get(
                new PathMatchingResourcePatternResolver()
                    .getResource(templateFile)
                    .getURI()
            )).map(line -> text.format(line, language, args))
                .collect(joining("\n"));
        } catch (final IOException ex) {
            throw new IllegalArgumentException(
                "Could not find email template '" + templateFile + "'."
            );
        }
    }

    private void send(String subject, String to, String toEmail, String body) {
        final String url = "https://api.mailgun.net/v3/" +
            env.getProperty("mailgun.domain") + "/messages";

        final Map<String, String> args = new HashMap<>();
        args.put("subject", subject);
        args.put("from",  env.getProperty("service.email.sitename") +
                  " <" +  env.getProperty("service.email.sender") + ">");
        args.put("to", to + " <" + toEmail + ">");
        args.put("html", body);

        final ResponseEntity<MailGunResponse> response =
            mailgun.postForEntity(url,args, MailGunResponse.class);



        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(
                "Error delivering mail. Message: " +
                response.getBody().getMessage()
            );
        }
    }

    @Data
    private final static class MailGunResponse {
        private String message;
        private String id;
    }
}
