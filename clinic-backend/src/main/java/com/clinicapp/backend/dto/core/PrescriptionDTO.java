package com.clinicapp.backend.dto.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Prescription information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDTO {

    private Long id;

    @NotNull(message = "Patient ID cannot be null")
    private Long patientId;

    @NotNull(message = "Doctor ID cannot be null")
    private Long doctorId;

    @NotBlank(message = "Medication name cannot be blank")
    private String medicationName;

    @NotBlank(message = "Dosage cannot be blank")
    private String dosage;

    @NotBlank(message = "Frequency cannot be blank")
    private String frequency;

    private String instructions;

    @NotNull(message = "Prescription date cannot be null")
    @PastOrPresent(message = "Prescription date must be in the past or present")
    private LocalDate prescriptionDate;

    private LocalDate endDate; // Optional

    // Optional fields for response convenience
    private String patientFirstName;
    private String patientLastName;
    private String doctorFirstName;
    private String doctorLastName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}