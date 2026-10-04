package com.resumeanalyzer.service;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.exception.ApiException;
import com.resumeanalyzer.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public UserService(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    @Transactional
    public UserDto updateProfile(String email, ProfileRequest r) {
        User u = find(email);
        u.setFullName(r.fullName().trim());
        return UserDto.from(users.save(u));
    }

    // Validation failures use 400 (not 401) so the frontend does not treat them as an expired session.
    @Transactional
    public void changePassword(String email, ChangePasswordRequest r) {
        User u = find(email);
        if (!encoder.matches(r.currentPassword(), u.getPasswordHash())) throw bad("Current password is incorrect");
        if (!r.newPassword().equals(r.confirmPassword())) throw bad("Passwords do not match");
        if (r.newPassword().equals(r.currentPassword())) throw bad("New password must be different from the current password");
        u.setPasswordHash(encoder.encode(r.newPassword()));
        users.save(u);
    }

    private User find(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }
    private static ApiException bad(String m) { return new ApiException(HttpStatus.BAD_REQUEST, m); }
}
