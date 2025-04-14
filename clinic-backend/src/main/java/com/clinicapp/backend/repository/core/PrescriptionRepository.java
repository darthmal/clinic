package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.core.Prescription;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.model.security.User; // Doctor
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    // Find prescriptions for a specific patient
    List<Prescription> findByPatientOrderByPrescriptionDateDesc(Patient patient);

    // Find prescriptions issued by a specific doctor
    List<Prescription> findByDoctorOrderByPrescriptionDateDesc(User doctor);

    // Find prescriptions for a patient issued by a specific doctor
    List<Prescription> findByPatientAndDoctorOrderByPrescriptionDateDesc(Patient patient, User doctor);

    // Find prescriptions issued within a date range
    List<Prescription> findByPrescriptionDateBetween(LocalDate startDate, LocalDate endDate);

    // Count prescriptions issued by a specific doctor (Needed for DashboardService)
    long countByDoctorId(Long doctorId);
}