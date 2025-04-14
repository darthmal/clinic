package com.clinicapp.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface DashboardService {

    // --- Admin Stats ---
    long getTotalPatients();
    long getAppointmentsTodayCount();
    BigDecimal getMonthlyRevenue(int year, int month); // Or calculate for current month
    long getActiveStaffCount(); // Doctors + Secretaries

    // --- Doctor Stats ---
    long getDoctorAppointmentsTodayCount(Long doctorId);
    long getDoctorTotalPatientsCount(Long doctorId);
    long getDoctorPendingPrescriptionsCount(Long doctorId);
    // Method to get next appointment details might be complex, maybe handle in AppointmentService

    // --- Secretary Stats ---
    long getSecretaryAppointmentsTodayCount(); // All appointments today
    long getPendingInvoicesCount();
    long getRegisteredPatientsCount(); // Same as getTotalPatients? Or maybe recent registrations?
    long getUrgentMattersCount(); // Needs definition - e.g., overdue invoices?

    // --- Chart Data (Example) ---
    // Key: Month (e.g., "Jan", "Feb"), Value: Revenue
    Map<String, BigDecimal> getRevenueOverviewForYear(int year);
    // Key: Day of Week (e.g., "Mon", "Tue"), Value: Count
    Map<String, Long> getWeeklyAppointmentsCount(LocalDate startDate);

}