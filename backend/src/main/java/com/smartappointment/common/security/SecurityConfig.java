package com.smartappointment.common.security;

import com.smartappointment.common.config.JwtProperties;
import com.smartappointment.common.web.ProblemResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;

/**
 * Stateless API security (ADR-0015): JWT from the Authorization header or the access cookie, CSRF
 * protection for cookie-authenticated requests, RFC 7807 bodies for 401 and 403.
 */
@Configuration
public class SecurityConfig {

  // Health, sign-in and the API documentation (Swagger UI is off in prod, ADR-0014).
  private static final String[] PUBLIC_PATHS = {
    "/actuator/health/**",
    "/api/v1/auth/**",
    "/openapi.yaml",
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/v3/api-docs/**"
  };

  // GET-only: the contract marks these security: [] (getOrganization, listServices,
  // getAppointmentRule). Other methods on the same paths (update, create, set) stay authenticated.
  private static final String[] PUBLIC_GET_PATHS = {
    "/api/v1/organizations/{organizationId}",
    "/api/v1/organizations/{organizationId}/services",
    "/api/v1/services/{serviceId}/rule"
  };

  @Bean
  SecurityFilterChain apiSecurity(
      HttpSecurity http,
      JwtProperties jwtProperties,
      JwtDecoder jwtDecoder,
      JwtAuthenticationConverter jwtAuthenticationConverter)
      throws Exception {
    http.csrf(csrf ->
            csrf.spa()
                .ignoringRequestMatchers(AccessCookieAuthenticationFilter::hasAuthorizationHeader)
                .ignoringRequestMatchers("/api/v1/auth/login"))
        .addFilterAfter(
            new AccessCookieAuthenticationFilter(jwtProperties.cookieName(), jwtDecoder),
            CsrfFilter.class)
        .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(basic -> basic.disable())
        .formLogin(form -> form.disable())
        .oauth2ResourceServer(resourceServer ->
            resourceServer
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .authenticationEntryPoint(unauthorized()))
        .exceptionHandling(errors ->
            errors.authenticationEntryPoint(unauthorized()).accessDeniedHandler(forbidden()))
        .authorizeHttpRequests(requests ->
            requests
                .requestMatchers(PUBLIC_PATHS)
                .permitAll()
                .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS)
                .permitAll()
                .anyRequest()
                .authenticated());
    return http.build();
  }

  private static AuthenticationEntryPoint unauthorized() {
    return (request, response, exception) ->
        ProblemResponses.write(
            response, HttpStatus.UNAUTHORIZED, "Sign in to use this part of the API.");
  }

  private static AccessDeniedHandler forbidden() {
    return (request, response, exception) ->
        ProblemResponses.write(
            response,
            HttpStatus.FORBIDDEN,
            "This request is not allowed. Reload the page and try again.");
  }
}
