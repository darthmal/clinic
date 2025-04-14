package com.clinicapp.backend.controller;

import com.clinicapp.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.DayOfWeek; // Import DayOfWeek
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Ensure only ADMIN can access dashboard data
public class AdminDashboardController {

    private final DashboardService dashboardService;

    // Endpoint for the main statistic cards
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();

        stats.put("totalPatients", dashboardService.getTotalPatients());
        stats.put("appointmentsToday", dashboardService.getAppointmentsTodayCount());
        // Get revenue for the current month
        stats.put("monthlyRevenue", dashboardService.getMonthlyRevenue(today.getYear(), today.getMonthValue()));
        stats.put("activeStaff", dashboardService.getActiveStaffCount());

        // TODO: Add percentage change calculations if needed (requires fetching previous period data)

        return ResponseEntity.ok(stats);
    }

    // Endpoint for the revenue overview chart data
    @GetMapping("/revenue-overview")
    public ResponseEntity<Map<String, BigDecimal>> getRevenueOverview(
            @RequestParam(defaultValue = "0") int year) { // Default to current year if not provided
        if (year == 0) {
            year = LocalDate.now().getYear();
        }
        Map<String, BigDecimal> revenueData = dashboardService.getRevenueOverviewForYear(year);
        return ResponseEntity.ok(revenueData);
    }

    // Endpoint for the weekly appointments chart data
    @GetMapping("/weekly-appointments")
    public ResponseEntity<Map<String, Long>> getWeeklyAppointments(
            @RequestParam(required = false) String startDate) { // Optional start date string (e.g., YYYY-MM-DD)
        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : LocalDate.now().with(DayOfWeek.MONDAY); // Default to start of current week
        Map<String, Long> appointmentData = dashboardService.getWeeklyAppointmentsCount(start);
        return ResponseEntity.ok(appointmentData);
    }

    // TODO: Add endpoints for Recent Staff Activity and Upcoming Tasks if needed
    // These might require dedicated services/repositories depending on complexity.

}