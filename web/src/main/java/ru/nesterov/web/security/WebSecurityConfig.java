package ru.nesterov.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {
    private final SecretTokenFilter secretTokenFilter;
    private final UsernameHeaderFilter usernameHeaderFilter;

    public WebSecurityConfig(SecretTokenFilter secretTokenFilter, UsernameHeaderFilter usernameHeaderFilter) {
        this.secretTokenFilter = secretTokenFilter;
        this.usernameHeaderFilter = usernameHeaderFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                .requestMatchers("/user/createUser", "/user/getUserByUsername").permitAll()
                                .anyRequest()
                                .hasAnyRole("USER", "ADMIN")
                )
                .addFilterBefore(secretTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(usernameHeaderFilter, SecretTokenFilter.class)
                .build();
    }
}
