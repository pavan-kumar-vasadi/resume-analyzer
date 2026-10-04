package com.resumeanalyzer;

import static org.junit.jupiter.api.Assertions.*;

import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = "test-secret-test-secret-test-secret-123";

    @Test void tokenRoundTrip() {
        JwtService s = new JwtService(SECRET, 5);
        User u = new User(); u.setEmail("a@b.com");
        assertEquals("a@b.com", s.extractEmail(s.generate(u)));
    }

    @Test void tamperedTokenIsRejected() {
        JwtService s = new JwtService(SECRET, 5);
        User u = new User(); u.setEmail("a@b.com");
        assertThrows(JwtException.class, () -> s.extractEmail(s.generate(u) + "x"));
    }

    @Test void expiredTokenIsRejected() {
        JwtService s = new JwtService(SECRET, -1);
        User u = new User(); u.setEmail("a@b.com");
        assertThrows(JwtException.class, () -> s.extractEmail(s.generate(u)));
    }
}
