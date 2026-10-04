package com.resumeanalyzer;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.resumeanalyzer.config.SecurityConfig;
import com.resumeanalyzer.controller.*;
import com.resumeanalyzer.entity.Role;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.repository.UserRepository;
import com.resumeanalyzer.security.JwtService;
import com.resumeanalyzer.service.*;
import io.jsonwebtoken.JwtException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Verifies the URL rules in SecurityConfig and the JWT filter without a database:
 * services and repositories are mocked, only the security layer and controllers are real.
 */
@WebMvcTest(controllers = {AdminController.class, AdminJobController.class, DashboardController.class, ResumeController.class,
    RecommendationController.class, SavedJobController.class, UserController.class, JobController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class SecurityAccessTest {
    @Autowired MockMvc mvc;
    @MockBean JwtService jwt;
    @MockBean UserRepository users;
    @MockBean AdminDashboardService adminDashboard;
    @MockBean JobService jobService;
    @MockBean DashboardService dashboardService;
    @MockBean ResumeService resumeService;
    @MockBean RecommendationService recommendationService;
    @MockBean UserService userService;

    private static MockHttpServletRequestBuilder req(HttpMethod m, String url) {
        return request(m, url).contentType(MediaType.APPLICATION_JSON).content("{}");
    }

    static Stream<Arguments> loginRequired() {
        return Stream.of(
            Arguments.of(HttpMethod.GET, "/api/dashboard"), Arguments.of(HttpMethod.GET, "/api/history"),
            Arguments.of(HttpMethod.GET, "/api/resumes/my"), Arguments.of(HttpMethod.GET, "/api/resumes/1"),
            Arguments.of(HttpMethod.DELETE, "/api/resumes/1"), Arguments.of(HttpMethod.POST, "/api/recommendations/analyze/1"),
            Arguments.of(HttpMethod.GET, "/api/saved-jobs"), Arguments.of(HttpMethod.POST, "/api/saved-jobs/1"),
            Arguments.of(HttpMethod.PUT, "/api/users/me"), Arguments.of(HttpMethod.PUT, "/api/users/me/password"),
            Arguments.of(HttpMethod.GET, "/api/admin/users"), Arguments.of(HttpMethod.GET, "/api/admin/dashboard"),
            Arguments.of(HttpMethod.POST, "/api/admin/jobs"), Arguments.of(HttpMethod.DELETE, "/api/admin/jobs/1"));
    }

    static Stream<Arguments> adminOnly() {
        return Stream.of(
            Arguments.of(HttpMethod.GET, "/api/admin/users"), Arguments.of(HttpMethod.GET, "/api/admin/dashboard"),
            Arguments.of(HttpMethod.GET, "/api/admin/jobs"), Arguments.of(HttpMethod.POST, "/api/admin/jobs"),
            Arguments.of(HttpMethod.PUT, "/api/admin/jobs/1"), Arguments.of(HttpMethod.PATCH, "/api/admin/jobs/1/active"),
            Arguments.of(HttpMethod.DELETE, "/api/admin/jobs/1"));
    }

    @ParameterizedTest @MethodSource("loginRequired")
    void withoutTokenProtectedEndpointsReturn401(HttpMethod m, String url) throws Exception {
        mvc.perform(req(m, url)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest @MethodSource("adminOnly")
    @WithMockUser(roles = "USER")
    void normalUserGets403OnAdminEndpoints(HttpMethod m, String url) throws Exception {
        mvc.perform(req(m, url)).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void adminCanReachAdminEndpoints() throws Exception {
        when(users.findAll()).thenReturn(List.of());
        mvc.perform(get("/api/admin/users")).andExpect(status().isOk());
        mvc.perform(get("/api/admin/dashboard")).andExpect(status().isOk());
    }

    @Test void publicJobListingNeedsNoLogin() throws Exception {
        mvc.perform(get("/api/jobs")).andExpect(status().isOk());
    }

    @Test void writingToPublicJobPathsStillRequiresLogin() throws Exception {
        // only GET /api/jobs/** is public
        mvc.perform(req(HttpMethod.POST, "/api/jobs")).andExpect(status().isUnauthorized());
    }

    @Test void tamperedOrExpiredTokenIsTreatedAsNotLoggedIn() throws Exception {
        when(jwt.extractEmail("bad")).thenThrow(new JwtException("invalid"));
        mvc.perform(get("/api/dashboard").header("Authorization", "Bearer bad")).andExpect(status().isUnauthorized());
    }

    @Test void malformedAuthorizationHeaderIsIgnored() throws Exception {
        mvc.perform(get("/api/dashboard").header("Authorization", "Token abc")).andExpect(status().isUnauthorized());
    }

    @Test void tokenForADeletedUserIsRejected() throws Exception {
        when(jwt.extractEmail("t")).thenReturn("gone@x.com");
        when(users.findByEmail("gone@x.com")).thenReturn(Optional.empty());
        mvc.perform(get("/api/dashboard").header("Authorization", "Bearer t")).andExpect(status().isUnauthorized());
    }

    @Test void roleComesFromTheDatabaseNotFromTheToken() throws Exception {
        User u = new User(); u.setEmail("u@x.com"); u.setRole(Role.USER);
        when(jwt.extractEmail("t")).thenReturn("u@x.com");
        when(users.findByEmail("u@x.com")).thenReturn(Optional.of(u));
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer t")).andExpect(status().isForbidden());

        u.setRole(Role.ADMIN); // demoted/promoted in the database takes effect on the next request
        when(users.findAll()).thenReturn(List.of());
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer t")).andExpect(status().isOk());
    }
}
