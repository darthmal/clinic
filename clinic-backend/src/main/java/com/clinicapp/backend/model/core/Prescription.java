package com.clinicapp.backend.model.core;

import com.clinicapp.backend.model.security.User; // Assuming Doctor is a User
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "prescription")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Patient cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull(message = "Doctor cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor; // Link to the User entity (ensure this user has DOCTOR role)

    @NotBlank(message = "Medication name cannot be blank")
    @Column(nullable = false)
    private String medicationName;

    @NotBlank(message = "Dosage cannot be blank")
    @Column(nullable = false)
    private String dosage; // e.g., "10mg", "2 pills"

    @NotBlank(message = "Frequency cannot be blank")
    @Column(nullable = false)
    private String frequency; // e.g., "Twice daily", "As needed"

    @Lob
    @Column(columnDefinition = "TEXT")
    private String instructions; // Additional instructions

    @NotNull(message = "Prescription date cannot be null")
    @PastOrPresent(message = "Prescription date must be in the past or present")
    @Column(nullable = false)
    private LocalDate prescriptionDate;

    private LocalDate endDate; // Optional: End date for the prescription

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (prescriptionDate == null) {
            prescriptionDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // TODO: Add validation in service layer: Ensure the 'doctor' User actually has the DOCTOR role.
}