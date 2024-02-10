package com.hudyma.CarRental2024.config;

import com.hudyma.CarRental2024.constants.UserAccessLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String [] FREE_ACCESS_URL_LIST = {
            "/cars",
            "/users",
            "/api/**",
            "/orders",
            "/"
    };

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers(FREE_ACCESS_URL_LIST)
                        .permitAll()
                        .requestMatchers("/user").hasRole(UserAccessLevel.USER.name())
                        .requestMatchers("/admin").hasRole(UserAccessLevel.ADMIN.name())
                        .requestMatchers("/manager").hasRole(UserAccessLevel.MANAGER.name())
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout.logoutUrl("auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler( (request, response, authentication) ->
                                SecurityContextHolder.clearContext()));
        return http.build();
    }
}
