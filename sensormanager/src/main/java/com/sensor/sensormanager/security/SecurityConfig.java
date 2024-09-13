package com.sensor.sensormanager.security;

import com.sensor.sensormanager.controller.BaseController;
import com.sensor.sensormanager.controller.UserController;
import com.sensor.sensormanager.filter.CustomAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
/*
 * https://www.baeldung.com/spring-deprecated-websecurityconfigureradapter
 */
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final CustomAuthorizationFilter customAuthorizationFilter;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .requestMatchers(BaseController.BASE_PATH + UserController.USERS_PATH).hasAnyRole("USER", "ADMIN")
                                .requestMatchers(BaseController.BASE_PATH + UserController.ROLES_PATH).hasAnyRole("USER", "ADMIN")
                                .requestMatchers(BaseController.BASE_PATH + UserController.ROLES_PATH + "/**").hasAnyRole("USER", "ADMIN")
                                .requestMatchers(BaseController.BASE_PATH + UserController.ADMIN_USERS_PATH).hasAnyRole("ADMIN")
                                .requestMatchers(BaseController.BASE_PATH + UserController.ADMIN_ROLES_PATH).hasAnyRole("ADMIN")
                                .requestMatchers(BaseController.AUTH_PATH + "/**").permitAll()
                                .requestMatchers(BaseController.BASE_PATH + "/echoSensorEndpoint").permitAll()
                                //websocket
                                .requestMatchers("/sensor-gui").permitAll()
                                .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

//        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
