package com.membaza.api.users.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Configuration
public class EmailConfig {

    private static final String EMAIL_TEMPLATE_ENCODING = "UTF-8";
    private static final String
        JAVA_MAIL_FILE = "classpath:mail/mail.properties",
        HOST           = "mail.server.host",
        PORT           = "mail.server.port",
        PROTOCOL       = "mail.server.protocol",
        USERNAME       = "mail.server.username",
        PASSWORD       = "mail.server.password";

    private final ApplicationContext ctx;
    private final Environment env;

    public EmailConfig(ApplicationContext applicationContext,
                       Environment environment) {

        this.ctx = requireNonNull(applicationContext);
        this.env = requireNonNull(environment);
    }

    @Bean
    public JavaMailSender mailSender() throws IOException {

        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Basic mail sender configuration, based on mail.properties
        mailSender.setHost(env.getProperty(HOST));
        mailSender.setPort(Integer.parseInt(env.getProperty(PORT)));
        mailSender.setProtocol(env.getProperty(PROTOCOL));
        mailSender.setUsername(env.getProperty(USERNAME));
        mailSender.setPassword(env.getProperty(PASSWORD));

        // JavaMail-specific mail sender configuration, based on javamail.properties
        final Properties javaMailProperties = new Properties();
        javaMailProperties.load(ctx.getResource(JAVA_MAIL_FILE).getInputStream());
        mailSender.setJavaMailProperties(javaMailProperties);

        return mailSender;

    }

    @Bean
    public ResourceBundleMessageSource emailMessageSource() {
        final ResourceBundleMessageSource messageSource =
            new ResourceBundleMessageSource();
        messageSource.setBasename("mail/MailMessages");
        return messageSource;
    }

    @Bean
    public TemplateEngine emailTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlTemplateResolver());
        templateEngine.setTemplateEngineMessageSource(emailMessageSource());
        return templateEngine;
    }

    private ITemplateResolver htmlTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(2);
        templateResolver.setResolvablePatterns(Collections.singleton("html/*"));
        templateResolver.setPrefix("/mail/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("html");
        templateResolver.setCharacterEncoding(EMAIL_TEMPLATE_ENCODING);
        templateResolver.setCacheable(false);
        return templateResolver;
    }
}
