package com.school.hei.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public String generateToken(UserDetails userDetails) {

        Instant now = Instant.now();

        String role =
                userDetails.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(authority -> authority.getAuthority())
                        .orElse("");

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer("heigrade")
                        .issuedAt(now)
                        .expiresAt(now.plus(2, ChronoUnit.HOURS))
                        .subject(userDetails.getUsername())
                        .claim("role", role)
                        .build();

        JwsHeader header =
                JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}