package com.clinicapp.backend.controller;

import com.clinicapp.backend.model.core.Appointment;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.DashboardService;
import com.clinicapp.backend.service.core.AppointmentService; // Use core service
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/secretary/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SECRETARY')") // Ensure only SECRETARY can access
public class SecretaryDashboardController {

    private final DashboardService dashboardService;
    private final AppointmentService appointmentService; // Inject AppointmentService

    // Endpoint for the main statistic cards
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@AuthenticationPrincipal User currentUser) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("appointmentsToday", dashboardService.getSecretaryAppointmentsTodayCount());
        stats.put("pendingInvoices", dashboardService.getPendingInvoicesCount());
        stats.put("registeredPatients", dashboardService.getRegisteredPatientsCount());
        stats.put("urgentMatters", dashboardService.getUrgentMattersCount()); // e.g., overdue invoices

        return ResponseEntity.ok(stats);
    }

    // Endpoint for today's appointments list (similar to doctor's, but maybe fetched differently if needed)
    @GetMapping("/today-appointments")
    public ResponseEntity<List<Appointment>> getTodayAppointments(@AuthenticationPrincipal User currentUser) {
        // For now, fetch all appointments for today. Could be refined.
        // Assuming AppointmentService has a method like getAppointmentsForDate
        List<Appointment> appointments = appointmentService.getAppointmentsForDate(LocalDate.now());
        return ResponseEntity.ok(appointments);
    }
}