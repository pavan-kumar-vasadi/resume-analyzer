package com.resumeanalyzer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.resumeanalyzer.dto.*;
import com.resumeanalyzer.entity.ResumeStatus;
import com.resumeanalyzer.entity.User;
import com.resumeanalyzer.repository.*;
import com.resumeanalyzer.service.AdminDashboardService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {
    @Mock UserRepository users;
    @Mock ResumeRepository resumes;
    @Mock JobRepository jobs;
    @Mock RecommendationRepository recs;
    AdminDashboardService svc;
    final LocalDate today = LocalDate.of(2026, 10, 4);

    @BeforeEach void init() { svc = new AdminDashboardService(users, resumes, jobs, recs); }

    private User userCreated(LocalDateTime at) {
        User u = new User();
        ReflectionTestUtils.setField(u, "createdAt", at); // createdAt is database-managed, so there is no setter
        return u;
    }

    @Test void registrationsCoverEveryDayAndFillEmptyDaysWithZero() {
        when(users.findByCreatedAtGreaterThanEqual(any())).thenReturn(List.of(
            userCreated(today.atTime(9, 0)), userCreated(today.atTime(18, 30)), userCreated(today.minusDays(2).atTime(12, 0))));
        List<DayCountDto> days = svc.registrations(today);
        assertEquals(14, days.size());
        assertEquals(today.minusDays(13).toString(), days.get(0).date());
        assertEquals(today.toString(), days.get(13).date());
        assertEquals(2, days.get(13).count());
        assertEquals(0, days.get(12).count());
        assertEquals(1, days.get(11).count());
        assertEquals(3, days.stream().mapToInt(DayCountDto::count).sum());
    }

    @Test void usersOutsideTheWindowOrWithoutDateAreNotCounted() {
        when(users.findByCreatedAtGreaterThanEqual(any())).thenReturn(List.of(userCreated(today.minusDays(40).atStartOfDay()), userCreated(null)));
        assertEquals(0, svc.registrations(today).stream().mapToInt(DayCountDto::count).sum());
    }

    @Test void emptyDatabaseGivesZerosAndEmptyListsNotErrors() {
        AdminDashboardDto d = svc.dashboard();
        assertEquals(0, d.totalUsers());
        assertEquals(0, d.totalResumes());
        assertEquals(0, d.analyzedResumes());
        assertTrue(d.topSkills().isEmpty());
        assertTrue(d.recentUsers().isEmpty());
        assertTrue(d.recentResumes().isEmpty());
        assertTrue(d.recentAnalyses().isEmpty());
        assertEquals(14, d.registrationsByDay().size());
        assertEquals(ResumeStatus.values().length, d.resumesByStatus().size());
    }

    @Test void jobsByLevelKeepsFixedOrderAndGroupsUnknownAsUnspecified() {
        when(jobs.countByLevel()).thenReturn(List.of(new Object[]{"Senior", 2L}, new Object[]{"Entry", 5L}, new Object[]{null, 1L}));
        List<LabelCountDto> l = svc.dashboard().jobsByLevel();
        assertEquals(List.of("Entry", "Mid", "Senior", "Unspecified"), l.stream().map(LabelCountDto::label).toList());
        assertEquals(List.of(5L, 0L, 2L, 1L), l.stream().map(LabelCountDto::count).toList());
    }

    @Test void totalsAndStatusCountsComeFromTheRepositories() {
        when(users.count()).thenReturn(5L);
        when(jobs.count()).thenReturn(12L);
        when(jobs.countByActive(true)).thenReturn(9L);
        when(resumes.countByStatus(ResumeStatus.ANALYZED)).thenReturn(3L);
        AdminDashboardDto d = svc.dashboard();
        assertEquals(5, d.totalUsers());
        assertEquals(9, d.activeJobs());
        assertEquals(12, d.totalJobs());
        assertEquals(3, d.resumesByStatus().stream().filter(x -> x.label().equals("ANALYZED")).findFirst().orElseThrow().count());
    }
}
