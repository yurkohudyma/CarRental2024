package com.hudyma.CarRental2024.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static com.hudyma.CarRental2024.model.Permission.ADMIN_READ;
import static com.hudyma.CarRental2024.model.Permission.MANAGER_READ;
import static com.hudyma.CarRental2024.model.Role.ADMIN;
import static com.hudyma.CarRental2024.model.Role.MANAGER;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String[] FREE_ACCESS_URL_LIST = {
            "/cars",
            "/users",
            "/api/**",
            "/orders",
            "/edit",
            "/auth/**",
            "/",
            "/img/**",
            "/css/**"
    };

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                                req.requestMatchers(FREE_ACCESS_URL_LIST)
                                        .permitAll()
//                                      .requestMatchers("/user").hasRole(UserAccessLevel.USER.name())
                                        .requestMatchers("/admin").hasAnyRole(ADMIN.name(), MANAGER.name())
                                        .requestMatchers(GET, "/admin").hasAnyAuthority(
                                                ADMIN_READ.name(), MANAGER_READ.name())
                                        .requestMatchers("/mgr").hasAnyRole(ADMIN.name(), MANAGER.name())
                                        .requestMatchers(GET, "/mgr").hasAnyAuthority(
                                                ADMIN_READ.name(), MANAGER_READ.name())
                                        .anyRequest()
                                        .authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout.logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) ->
                                SecurityContextHolder.clearContext()));
        return http.build();
    }
}
