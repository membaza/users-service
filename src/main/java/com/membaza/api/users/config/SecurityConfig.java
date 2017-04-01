package com.membaza.api.users.config;

import com.membaza.api.users.security.jwt.JwtAuthenticationProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpMethod.*;

/**
 * @author Emil Forslund
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    public SecurityConfig(AuthenticationEntryPoint authenticationEntryPoint,
                          JwtAuthenticationProvider jwtAuthenticationProvider) {

        this.authenticationEntryPoint   = requireNonNull(authenticationEntryPoint);
        this.jwtAuthenticationProvider  = requireNonNull(jwtAuthenticationProvider);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(jwtAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // We don't need CSRF for JWT based authentication
        http.csrf().disable()
            .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint)

            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            .authorizeRequests()
            .antMatchers(POST,
                "/users",
                "/users/{userId}/verify",
                "/users/{userId}/cancel",
                "/users/{userId}/reset",
                "/users/login",
                "/users/logout").permitAll()

            .antMatchers(PUT,
                 "/users/{userId}/mail/verify",
                 "/users/{userId}/mail/cancel",
                 "/users/{userId}/password/verify",
                 "/users/{userId}/password/cancel").permitAll()

            .antMatchers(DELETE,
                 "/users/{userId}/verify",
                 "/users/{userId}/cancel").permitAll()

            .and()
            .authorizeRequests()
            .antMatchers(POST,
                 "/users/refresh",
                 "/users/{userId}/roles/{role}").authenticated()

            .antMatchers(PUT,
                 "/users/{userId}/mail",
                 "/users/{userId}/password").authenticated()

            .antMatchers(DELETE,
                 "/users/{userId}",
                 "/users/{userId}/roles/{role}").authenticated();
    }
}
