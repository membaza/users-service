package com.membaza.api.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import static java.util.Objects.requireNonNull;

/**
 * @author Emil Forslund
 * @since  1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final AuthenticationSuccessHandler myAuthenticationSuccessHandler;
    private final LogoutSuccessHandler myLogoutSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;
//    private final CustomWebAuthenticationDetailsSource authenticationDetailsSource;

    public SecurityConfig(final UserDetailsService userDetailsService,
                          final AuthenticationSuccessHandler myAuthenticationSuccessHandler,
                          final LogoutSuccessHandler myLogoutSuccessHandler,
                          final AuthenticationFailureHandler authenticationFailureHandler) {

        this.userDetailsService             = requireNonNull(userDetailsService);
        this.myAuthenticationSuccessHandler = requireNonNull(myAuthenticationSuccessHandler);
        this.myLogoutSuccessHandler         = requireNonNull(myLogoutSuccessHandler);
        this.authenticationFailureHandler   = requireNonNull(authenticationFailureHandler);
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
//        auth.authenticationProvider(authProvider());
    }

    @Override
    public void configure(final WebSecurity web) throws Exception {
        web.ignoring()
            .antMatchers("/resources/**");
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/login*","/login*", "/logout*", "/signin/**", "/signup/**",
                         "/user/register*", "/user/register/confirm*", "/expiredAccount*", "/registration*",
                         "/badUser*", "/user/register/resend*" ,"/user/password/forgot*", "/user/password/reset*",
                         "/user/password/change*", "/email/error*", "/resources/**","/user/register/success*","/qrcode*").permitAll()
            .antMatchers("/invalidSession*").anonymous()
            .antMatchers("/user/password/update*","/user/password/save*").hasAuthority("CHANGE_PASSWORD_PRIVILEGE")
            .anyRequest().hasAuthority("READ_PRIVILEGE")
            .and()
            .formLogin()
            .loginPage("/login")
            .defaultSuccessUrl("/homepage.html")
            .failureUrl("/login?error=true")
            .successHandler(myAuthenticationSuccessHandler)
            .failureHandler(authenticationFailureHandler)
//            .authenticationDetailsSource(authenticationDetailsSource)
            .permitAll()
            .and()
            .sessionManagement()
            .invalidSessionUrl("/invalidSession.html")
            .maximumSessions(1).sessionRegistry(sessionRegistry()).and()
            .sessionFixation().none()
            .and()
            .logout()
            .logoutSuccessHandler(myLogoutSuccessHandler)
            .invalidateHttpSession(false)
            .logoutSuccessUrl("/logout.html?logSucc=true")
            .deleteCookies("JSESSIONID")
            .permitAll();
    }

//    @Bean
//    public DaoAuthenticationProvider authProvider() {
//        final CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider();
//        authProvider.setUserDetailsService(userDetailsService);
//        authProvider.setPasswordEncoder(encoder());
//        return authProvider;
//    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(11);
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}
