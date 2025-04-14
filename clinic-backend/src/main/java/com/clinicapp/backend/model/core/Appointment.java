package com.clinicapp.backend.model.core;

import com.clinicapp.backend.model.security.User; // Assuming Doctor is a User
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Patient cannot be null")
    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetch is often preferred for performance
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull(message = "Doctor cannot be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor; // Link to the User entity (ensure this user has DOCTOR role)

    @NotNull(message = "Appointment start time cannot be null")
    @Future(message = "Appointment must be in the future")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "Appointment end time cannot be null")
    @Future(message = "Appointment must be in the future")
    @Column(nullable = false)
    private LocalDateTime endTime; // Consider calculating this based on startTime + duration

    @NotBlank(message = "Room cannot be blank")
    private String room;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status; // Enum: SCHEDULED, COMPLETED, CANCELLED, NO_SHOW

    @Lob
    @Column(columnDefinition = "TEXT")
    private String notes; // Optional notes for the appointment

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AppointmentStatus.SCHEDULED; // Default status
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // TODO: Add validation logic in the service layer:
    // 1. Doctor cannot have 2 appointments at the same time.
    // 2. Patient cannot book two appointments on the same day.
    // 3. Cancellation only allowed > 24h prior.
    // 4. Ensure the 'doctor' User actually has the DOCTOR role.
}

