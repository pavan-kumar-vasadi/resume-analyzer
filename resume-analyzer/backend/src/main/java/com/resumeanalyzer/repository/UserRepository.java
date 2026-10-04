package com.resumeanalyzer.repository;

import com.resumeanalyzer.entity.Role;
import com.resumeanalyzer.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(Role role);
    List<User> findTop5ByOrderByCreatedAtDesc();
    List<User> findByCreatedAtGreaterThanEqual(LocalDateTime from);
}
