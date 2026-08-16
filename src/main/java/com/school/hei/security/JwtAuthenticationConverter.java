package com.school.hei.security;

import java.util.Collection;
import java.util.Collections;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {

    String role = jwt.getClaimAsString("role");

    Collection<GrantedAuthority> authorities;

    if (role == null || role.isBlank()) {
      authorities = Collections.emptyList();
    } else {
      String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role;
      authorities = Collections.singletonList(new SimpleGrantedAuthority(normalized));
    }

    return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
  }
}
