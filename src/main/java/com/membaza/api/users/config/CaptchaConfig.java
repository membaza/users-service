package com.membaza.api.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Configuration
@ComponentScan(basePackages = {"com.membaza.api.users.service.captcha"})
public class CaptchaConfig {

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        final SimpleClientHttpRequestFactory factory =
            new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(3_000);
        factory.setReadTimeout(7_000);

        return factory;
    }

    @Bean
    public RestOperations restTemplate() {
        return new RestTemplate(this.clientHttpRequestFactory());
    }
}