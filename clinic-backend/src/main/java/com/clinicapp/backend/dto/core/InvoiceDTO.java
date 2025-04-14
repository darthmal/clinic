package com.clinicapp.backend.dto.core;

import com.clinicapp.backend.model.core.InvoiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Invoice information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {

    private Long id;

    @NotNull(message = "Patient ID cannot be null")
    private Long patientId;

    // Optional: Include appointment ID if creating invoice from appointment
    private Long appointmentId;

    @NotNull(message = "Issue date cannot be null")
    @PastOrPresent(message = "Issue date must be in the past or present")
    private LocalDate issueDate;

    @NotNull(message = "Due date cannot be null")
    private LocalDate dueDate;

    @NotNull(message = "Total amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Status cannot be null")
    private InvoiceStatus status;

    private String notes;

    // Optional fields for response convenience
    private String patientFirstName;
    private String patientLastName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}