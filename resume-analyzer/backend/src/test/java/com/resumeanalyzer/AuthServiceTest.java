package com.resumeanalyzer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.entity.*;
import com.resumeanalyzer.exception.ApiException;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.security.JwtService;
import com.resumeanalyzer.service.AuthService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository repo;
    @Mock JwtService jwt;
    PasswordEncoder enc = new BCryptPasswordEncoder();
    AuthService svc;

    @BeforeEach void init() { svc = new AuthService(repo, enc, jwt); }

    @Test void duplicateEmailIsRejected() {
        when(repo.existsByEmail("a@b.com")).thenReturn(true);
        ApiException ex = assertThrows(ApiException.class,
            () -> svc.register(new RegisterRequest("A", "A@B.com", "Passw0rd1", "Passw0rd1")));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test void passwordMismatchIsRejected() {
        ApiException ex = assertThrows(ApiException.class,
            () -> svc.register(new RegisterRequest("A", "a@b.com", "Passw0rd1", "Different1")));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test void registerStoresBcryptHashAndUserRole() {
        when(repo.existsByEmail("a@b.com")).thenReturn(false);
        when(repo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwt.generate(any())).thenReturn("token");
        svc.register(new RegisterRequest("Asha", "a@b.com", "Passw0rd1", "Passw0rd1"));
        ArgumentCaptor<User> c = ArgumentCaptor.forClass(User.class);
        verify(repo).save(c.capture());
        assertNotEquals("Passw0rd1", c.getValue().getPasswordHash());
        assertTrue(enc.matches("Passw0rd1", c.getValue().getPasswordHash()));
        assertEquals(Role.USER, c.getValue().getRole());
    }

    @Test void wrongPasswordGivesGenericUnauthorized() {
        User u = new User(); u.setEmail("a@b.com"); u.setPasswordHash(enc.encode("Passw0rd1"));
        when(repo.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        ApiException ex = assertThrows(ApiException.class, () -> svc.login(new LoginRequest("a@b.com", "wrong")));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("Invalid email or password", ex.getMessage());
    }
}
