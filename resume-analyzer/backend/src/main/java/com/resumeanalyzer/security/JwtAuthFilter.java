package com.resumeanalyzer.security;

import com.resumeanalyzer.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserRepository users;

    public JwtAuthFilter(JwtService jwt, UserRepository users) { this.jwt = jwt; this.users = users; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            try {
                String email = jwt.extractEmail(h.substring(7));
                // Role is read from the database, never trusted from the token.
                users.findByEmail(email).ifPresent(u -> SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(u.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())))));
            } catch (JwtException | IllegalArgumentException ignored) {
                // invalid/expired token: request stays unauthenticated, so protected routes return 401
            }
        }
        chain.doFilter(req, res);
    }
}
