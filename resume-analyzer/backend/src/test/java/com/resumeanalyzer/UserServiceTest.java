package com.resumeanalyzer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.exception.ApiException;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository repo;
    PasswordEncoder enc = new BCryptPasswordEncoder();
    UserService svc;
    User user;

    @BeforeEach void init() {
        svc = new UserService(repo, enc);
        user = new User(); user.setEmail("a@b.com"); user.setFullName("Old"); user.setPasswordHash(enc.encode("Passw0rd1"));
        when(repo.findByEmail("a@b.com")).thenReturn(Optional.of(user));
    }

    @Test void wrongCurrentPasswordIsRejectedWith400() {
        ApiException ex = assertThrows(ApiException.class, () -> svc.changePassword("a@b.com", new ChangePasswordRequest("nope", "NewPass123", "NewPass123")));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus()); // 400, not 401: must not log the user out
    }
    @Test void confirmationMismatchIsRejected() {
        assertThrows(ApiException.class, () -> svc.changePassword("a@b.com", new ChangePasswordRequest("Passw0rd1", "NewPass123", "Other1234")));
    }
    @Test void sameAsCurrentIsRejected() {
        assertThrows(ApiException.class, () -> svc.changePassword("a@b.com", new ChangePasswordRequest("Passw0rd1", "Passw0rd1", "Passw0rd1")));
    }
    @Test void successStoresNewBcryptHash() {
        when(repo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        svc.changePassword("a@b.com", new ChangePasswordRequest("Passw0rd1", "NewPass123", "NewPass123"));
        assertTrue(enc.matches("NewPass123", user.getPasswordHash()));
        assertFalse(enc.matches("Passw0rd1", user.getPasswordHash()));
    }
    @Test void profileUpdateTrimsName() {
        when(repo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals("New Name", svc.updateProfile("a@b.com", new ProfileRequest("  New Name ")).fullName());
    }
}
