package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.core.Appointment;
import com.clinicapp.backend.model.core.AppointmentStatus;
import com.clinicapp.backend.model.security.User; // Doctor
import com.clinicapp.backend.model.core.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set; // Import Set for distinct IDs
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Find appointments for a specific doctor within a time range
    List<Appointment> findByDoctorAndStartTimeBetween(User doctor, LocalDateTime start, LocalDateTime end);

    // Find appointments for a specific patient within a time range
    List<Appointment> findByPatientAndStartTimeBetween(Patient patient, LocalDateTime start, LocalDateTime end);

    // Find appointments for a specific doctor on a specific day
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND FUNCTION('DATE', a.startTime) = :date")
    List<Appointment> findByDoctorAndDate(@Param("doctor") User doctor, @Param("date") LocalDate date);

    // Find appointments for a specific patient on a specific day
    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient AND FUNCTION('DATE', a.startTime) = :date")
    List<Appointment> findByPatientAndDate(@Param("patient") Patient patient, @Param("date") LocalDate date);

    // Find upcoming appointments for a doctor
    List<Appointment> findByDoctorAndStartTimeAfterOrderByStartTimeAsc(User doctor, LocalDateTime now);

    // Find appointments by status
    List<Appointment> findByStatus(AppointmentStatus status);

    // Check for overlapping appointments for a specific doctor
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor = :doctor " +
           "AND a.id <> :excludeAppointmentId " + // Exclude specific appointment (for updates)
           "AND a.status = 'SCHEDULED' " +
           "AND ((a.startTime < :newEndTime AND a.endTime > :newStartTime))")
    boolean existsOverlappingAppointmentForDoctor(
            @Param("doctor") User doctor,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime,
            @Param("excludeAppointmentId") Long excludeAppointmentId); // Use 0L or null for new appointments

     // Simplified check for overlapping appointments for a specific doctor (for new appointments)
     @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor = :doctor " +
            "AND a.status = 'SCHEDULED' " +
            "AND ((a.startTime < :newEndTime AND a.endTime > :newStartTime))")
     boolean existsOverlappingAppointmentForDoctor(
             @Param("doctor") User doctor,
             @Param("newStartTime") LocalDateTime newStartTime,
             @Param("newEndTime") LocalDateTime newEndTime);

    // Check if a patient has another appointment on the same day
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.patient = :patient " +
           "AND a.id <> :excludeAppointmentId " + // Exclude specific appointment (for updates)
           "AND a.status = 'SCHEDULED' " +
           "AND FUNCTION('DATE', a.startTime) = :date")
    boolean existsOtherAppointmentForPatientOnDate(
            @Param("patient") Patient patient,
            @Param("date") LocalDate date,
            @Param("excludeAppointmentId") Long excludeAppointmentId); // Use 0L or null for new appointments

     // Simplified check if a patient has another appointment on the same day (for new appointments)
     @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.patient = :patient " +
            "AND a.status = 'SCHEDULED' " +
            "AND FUNCTION('DATE', a.startTime) = :date")
     boolean existsOtherAppointmentForPatientOnDate(
             @Param("patient") Patient patient,
             @Param("date") LocalDate date);

   // --- Methods needed for DashboardService ---

   // Count appointments on a specific date
   @Query("SELECT COUNT(a) FROM Appointment a WHERE FUNCTION('DATE', a.startTime) = :date")
   long countByAppointmentDate(@Param("date") LocalDate date);

   // Count appointments for a specific doctor on a specific date
   @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND FUNCTION('DATE', a.startTime) = :date")
   long countByDoctorIdAndAppointmentDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

   // Find distinct patient IDs associated with a specific doctor via appointments
   @Query("SELECT DISTINCT a.patient.id FROM Appointment a WHERE a.doctor.id = :doctorId")
   Set<Long> findDistinctPatientIdsByDoctorId(@Param("doctorId") Long doctorId);

   // Find appointments on a specific date, eagerly fetching patient and doctor
   @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE FUNCTION('DATE', a.startTime) = :date ORDER BY a.startTime ASC")
   List<Appointment> findByAppointmentDateWithDetails(@Param("date") LocalDate date);

   // Find appointments for a specific doctor on a specific date, eagerly fetching patient
   @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.doctor.id = :doctorId AND FUNCTION('DATE', a.startTime) = :date ORDER BY a.startTime ASC")
   List<Appointment> findByDoctorIdAndDateWithDetails(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
}