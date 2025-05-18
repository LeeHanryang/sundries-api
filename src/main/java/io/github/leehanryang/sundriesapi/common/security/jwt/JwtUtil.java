package io.github.leehanryang.sundriesapi.common.security.jwt;


import io.github.leehanryang.sundriesapi.common.security.principal.CustomUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Component
public class JwtUtil {
    @Value("${todo.jwt.secret}")
    private String secret;

    @Value("${todo.jwt.expire-seconds:3600}") // 기본 1h
    private long expireSeconds;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /* ───────── 생성 ───────── */

    public String generateToken(UUID id,
                                String username,
                                String email,
                                String role) {

        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofSeconds(expireSeconds));

        return Jwts.builder()
                .signWith(key)
                .subject(id.toString())
                .claim("username", username)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .compact();
    }

    /* ───────── 파싱 & 검증 ───────── */

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validate(String token) {
        try {
            parse(token);   // 만료·서명 오류 시 예외
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* ───────── Security Authentication 변환 ───────── */

    public Authentication toAuthentication(String token) {

        Claims c = parse(token);

        CustomUserPrincipal principal = new CustomUserPrincipal(
                UUID.fromString(c.getSubject()),
                c.get("username", String.class),
                null,
                Set.of(c.get("role", String.class))
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                token,
                principal.getAuthorities()
        );
    }
}
