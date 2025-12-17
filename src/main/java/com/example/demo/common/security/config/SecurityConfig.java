package com.example.demo.common.security.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.example.demo.common.security.entrypoint.CustomAuthenticationEntryPoint;
import com.example.demo.common.security.handler.CustomAccessDeniedHandler;
import com.example.demo.common.security.jwt.filter.JwtAuthenticationFilter;
import com.example.demo.common.security.jwt.filter.JwtExceptionFilter;
import com.example.demo.infra.security.oauth.handler.OAuth2AuthenticationFailureHandler;
import com.example.demo.infra.security.oauth.handler.OAuth2AuthenticationSuccessHandler;
import com.example.demo.infra.security.oauth.service.CustomOAuth2UserService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * PackageName : com.example.demo.common.security.config
 * FileName    : SecurityConfig
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 시큐리티 설정
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter            jwtAuthenticationFilter;
    private final JwtExceptionFilter                 jwtExceptionFilter;
    private final CustomAuthenticationEntryPoint     authenticationEntryPoint;
    private final CustomAccessDeniedHandler          accessDeniedHandler;
    private final CustomOAuth2UserService            oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authenticationConfiguration)
    throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))

                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(authorize -> authorize
                        // Error & Swagger
                        .requestMatchers("/error",
                                         "/favicon.ico",
                                         "/swagger-ui/**",
                                         "/swagger-ui.html",
                                         "/v3/api-docs/**").permitAll()

                        // Actuator
                        .requestMatchers("/monitor").hasAuthority("ROLE_ADMIN")

                        // Auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/signin", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/signout").authenticated()

                        // Account
                        .requestMatchers(HttpMethod.GET, "/api/v1/accounts/verify-email").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                         "/api/v1/accounts",
                                         "/api/v1/accounts/verify-email-resend",
                                         "/api/v1/accounts/password-reset-request",
                                         "/api/v1/accounts/password-reset-confirm").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/accounts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/accounts").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/accounts").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/accounts").authenticated()

                        // Performance
                        .requestMatchers(HttpMethod.POST, "/api/v1/performances").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                         "/api/v1/performances",
                                         "/api/v1/performances/{performanceId}").permitAll()
                        .requestMatchers(HttpMethod.PUT,
                                         "/api/v1/performances/{performanceId}").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                         "/api/v1/performances/{performanceId}").hasAuthority("ROLE_ADMIN")

                        // Reservation
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservations").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/reservations").authenticated()

                        // ETC
                        .anyRequest().authenticated())

                .oauth2Login(oauth2 -> oauth2.userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                                             .successHandler(oAuth2AuthenticationSuccessHandler)
                                             .failureHandler(oAuth2AuthenticationFailureHandler))

                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint)
                                                         .accessDeniedHandler(accessDeniedHandler))

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
