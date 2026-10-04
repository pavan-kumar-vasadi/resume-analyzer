package com.resumeanalyzer.security;

import com.resumeanalyzer.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${app.jwt.secret:}") String secret,
                      @Value("${app.jwt.expiration-minutes:120}") long expirationMinutes) {
        byte[] bytes;
        if (secret == null || secret.length() < 32) {
            bytes = new byte[48];
            new SecureRandom().nextBytes(bytes);
            log.warn("JWT_SECRET not set (or shorter than 32 chars). Using a temporary random key: logins reset on every restart.");
        } else {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMinutes = expirationMinutes;
    }

    public String generate(User u) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(u.getEmail()).claim("role", u.getRole().name())
            .issuedAt(new Date(now)).expiration(new Date(now + expirationMinutes * 60_000))
            .signWith(key).compact();
    }

    /** Throws io.jsonwebtoken.JwtException if the token is invalid, tampered or expired. */
    public String extractEmail(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }
}
