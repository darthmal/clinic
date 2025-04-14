package com.clinicapp.backend.dto.core;

import com.clinicapp.backend.model.core.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Appointment information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {

    private Long id;

    @NotNull(message = "Patient ID cannot be null")
    private Long patientId; // ID of the patient

    @NotNull(message = "Doctor ID cannot be null")
    private Long doctorId; // ID of the doctor (User ID)

    @NotNull(message = "Appointment start time cannot be null")
    @Future(message = "Appointment must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "Appointment end time cannot be null")
    @Future(message = "Appointment must be in the future")
    private LocalDateTime endTime;

    @NotBlank(message = "Room cannot be blank")
    private String room;

    @NotNull(message = "Status cannot be null")
    private AppointmentStatus status;

    private String notes;

    // Optional fields to include patient/doctor names in response for convenience
    private String patientFirstName;
    private String patientLastName;
    private String doctorFirstName;
    private String doctorLastName;

    // Timestamps (usually not needed in request DTOs, but useful in responses)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}