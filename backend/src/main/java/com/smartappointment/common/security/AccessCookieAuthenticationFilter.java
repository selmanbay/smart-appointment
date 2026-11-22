package com.smartappointment.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lets browsers authenticate with the HttpOnly access cookie (ADR-0015).
 *
 * <p>Runs after the CSRF filter: the CSRF check sees a cookie request without an Authorization
 * header and protects it. Only then is the cookie's token presented to the JWT filter as a bearer
 * header. Resolving the cookie inside the bearer resolver instead would make Spring Security treat
 * cookie requests as bearer requests and skip CSRF protection.
 */
public final class AccessCookieAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final String cookieName;

  public AccessCookieAuthenticationFilter(String cookieName) {
    this.cookieName = cookieName;
  }

  /** True when the request authenticates with a header, so it needs no CSRF token. */
  public static boolean hasAuthorizationHeader(HttpServletRequest request) {
    return request.getHeader(HttpHeaders.AUTHORIZATION) != null;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    Optional<String> cookieToken =
        hasAuthorizationHeader(request) ? Optional.empty() : find(request);
    chain.doFilter(
        cookieToken
            .<HttpServletRequest>map(token -> new BearerRequest(request, token))
            .orElse(request),
        response);
  }

  private Optional<String> find(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }
    return Arrays.stream(cookies)
        .filter(cookie -> cookieName.equals(cookie.getName()) && !cookie.getValue().isBlank())
        .map(Cookie::getValue)
        .findFirst();
  }

  private static final class BearerRequest extends HttpServletRequestWrapper {

    private final String authorization;

    BearerRequest(HttpServletRequest request, String token) {
      super(request);
      this.authorization = BEARER_PREFIX + token;
    }

    @Override
    public String getHeader(String name) {
      return HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)
          ? authorization
          : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      return HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)
          ? Collections.enumeration(List.of(authorization))
          : super.getHeaders(name);
    }
  }
}
