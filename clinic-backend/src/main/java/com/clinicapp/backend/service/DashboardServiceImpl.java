package com.clinicapp.backend.service;

// Core Models (Assuming package structure)
import com.clinicapp.backend.model.core.*;
// Security Models
import com.clinicapp.backend.model.security.Role; // Import Role
import com.clinicapp.backend.model.security.User;
// Repositories
import com.clinicapp.backend.repository.core.AppointmentRepository;
import com.clinicapp.backend.repository.core.InvoiceRepository;
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.repository.core.PrescriptionRepository;
import com.clinicapp.backend.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Most dashboard methods are read-only
public class DashboardServiceImpl implements DashboardService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final PrescriptionRepository prescriptionRepository;

    // --- Admin Stats ---

    @Override
    public long getTotalPatients() {
        return patientRepository.count();
    }

    @Override
    public long getAppointmentsTodayCount() {
        LocalDate today = LocalDate.now();
        return appointmentRepository.countByAppointmentDate(today);
    }

    @Override
    public BigDecimal getMonthlyRevenue(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        // Sum 'totalAmount' for PAID invoices issued within the month
        // Note: Assumes InvoiceRepository has findPaidInvoicesIssuedBetween method
        return invoiceRepository.findPaidInvoicesIssuedBetween(startOfMonth, endOfMonth)
                .stream()
                .map(Invoice::getTotalAmount) // Requires Invoice model import
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public long getActiveStaffCount() {
        // Assuming all users in DB are active for now
        return userRepository.countByRoleIn(List.of(Role.DOCTOR, Role.SECRETARY)); // Requires Role model import
    }

    // --- Doctor Stats ---

    @Override
    public long getDoctorAppointmentsTodayCount(Long doctorId) {
        LocalDate today = LocalDate.now();
        // Note: Assumes AppointmentRepository has countByDoctorIdAndAppointmentDate method
        return appointmentRepository.countByDoctorIdAndAppointmentDate(doctorId, today);
        // TODO: Add logic for completed/remaining if needed based on time/status
    }

    @Override
    public long getDoctorTotalPatientsCount(Long doctorId) {
        // This requires linking patients to doctors, e.g., via appointments
        // Simplistic approach: count distinct patients with appointments with this doctor
        // Note: Assumes AppointmentRepository has findDistinctPatientIdsByDoctorId method
        return appointmentRepository.findDistinctPatientIdsByDoctorId(doctorId).size();
    }

    @Override
    public long getDoctorPendingPrescriptionsCount(Long doctorId) {
        // Define "pending" - e.g., prescriptions created but not yet marked as 'filled' or similar status if added
        // For now, just count all prescriptions by this doctor (needs refinement)
        // Note: Assumes PrescriptionRepository has countByDoctorId method
        return prescriptionRepository.countByDoctorId(doctorId);
    }

    // --- Secretary Stats ---

    @Override
    public long getSecretaryAppointmentsTodayCount() {
        return getAppointmentsTodayCount(); // Same as admin view for now
    }

    @Override
    public long getPendingInvoicesCount() {
        // Count invoices that are SENT or OVERDUE
        // Note: Assumes InvoiceRepository has countByStatusIn method
        return invoiceRepository.countByStatusIn(List.of(InvoiceStatus.SENT, InvoiceStatus.OVERDUE)); // Requires Invoice model import
    }

    @Override
    public long getRegisteredPatientsCount() {
        // Same as total patients for now, could be modified for "recently registered"
        return getTotalPatients();
    }

    @Override
    public long getUrgentMattersCount() {
        // Example definition: Overdue invoices
        return invoiceRepository.countByStatusIn(List.of(InvoiceStatus.OVERDUE)); // Use direct enum reference
    }

    // --- Chart Data ---

    @Override
    public Map<String, BigDecimal> getRevenueOverviewForYear(int year) {
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>(); // Keep month order
        for (int month = 1; month <= 12; month++) {
            BigDecimal revenue = getMonthlyRevenue(year, month);
            String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            monthlyRevenue.put(monthName, revenue);
        }
        return monthlyRevenue;
    }

    @Override
    public Map<String, Long> getWeeklyAppointmentsCount(LocalDate startDate) {
        Map<String, Long> weeklyCounts = new LinkedHashMap<>();
        LocalDate currentDate = startDate;
        // Get counts for the next 7 days (adjust range as needed)
        for (int i = 0; i < 7; i++) {
            long count = appointmentRepository.countByAppointmentDate(currentDate);
            String dayName = currentDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            weeklyCounts.put(dayName, count);
            currentDate = currentDate.plusDays(1);
        }
        return weeklyCounts;
    }
}