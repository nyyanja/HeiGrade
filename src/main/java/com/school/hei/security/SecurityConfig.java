package com.school.hei.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;
  private final JwtAuthenticationConverter jwtAuthenticationConverter;

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder)
      throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/auth/**")
                                        .permitAll()
                                        .requestMatchers("/ping", "/health/**")
                                        .permitAll()
                                        .requestMatchers("/admins/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/promotions/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/specialities/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/speciality-courses/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/teacher-courses/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/group-exams/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers("/graduates/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.DELETE, "/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.POST, "/exams/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/exams/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.GET, "/exams/**")
                                        .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                                        .requestMatchers(HttpMethod.POST, "/courses/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/courses/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.GET, "/courses/**")
                                        .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                                        .requestMatchers(HttpMethod.POST, "/groups/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.PUT, "/groups/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.GET, "/groups/**")
                                        .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                                        .requestMatchers(HttpMethod.POST, "/grades/**")
                                        .hasAnyRole("ADMIN", "TEACHER")
                                        .requestMatchers(HttpMethod.PUT, "/grades/**")
                                        .hasAnyRole("ADMIN", "TEACHER")
                                        .requestMatchers(HttpMethod.GET, "/grades/**")
                                        .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                                        .requestMatchers(HttpMethod.GET, "/transcripts/**")
                                        .hasAnyRole("ADMIN", "STUDENT")
                                        .requestMatchers(HttpMethod.POST, "/transcripts/**/send-email")
                                        .hasAnyRole("ADMIN", "STUDENT")
                                        .requestMatchers(HttpMethod.GET, "/students/**")
                                        .hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                                        .requestMatchers("/students/**")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.GET, "/teachers/**")
                                        .hasAnyRole("ADMIN", "TEACHER")
                                        .requestMatchers("/teachers/**")
                                        .hasRole("ADMIN")

                                        .anyRequest()
                                        .authenticated())
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(
                                        jwt ->
                                                jwt.decoder(jwtDecoder)
                                                        .jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }
}