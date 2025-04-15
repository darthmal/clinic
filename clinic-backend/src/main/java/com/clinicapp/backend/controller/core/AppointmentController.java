package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.AppointmentDTO;
import com.clinicapp.backend.service.core.AppointmentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments") // Base path for appointment endpoints
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()") // Require authentication at the class level as a baseline
public class AppointmentController {

    private final AppointmentService appointmentService;

    // Allow all authenticated roles to view lists/specific appointments
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'DOCTOR')")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        List<AppointmentDTO> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'DOCTOR')")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        try {
            AppointmentDTO appointment = appointmentService.getAppointmentById(id);
            return ResponseEntity.ok(appointment);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

     // Get appointments for a specific doctor
     @GetMapping("/doctor/{doctorId}")
     @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'DOCTOR')") // Or maybe just ADMIN/SECRETARY/OWN_DOCTOR?
     public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
         try {
             List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
             return ResponseEntity.ok(appointments);
         } catch (EntityNotFoundException e) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Doctor not found
         } catch (IllegalArgumentException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e); // User is not a doctor
         }
     }

     // Get appointments for a specific patient
     @GetMapping("/patient/{patientId}")
     @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY', 'DOCTOR')") // Or maybe restrict based on relationship?
     public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatient(@PathVariable Long patientId) {
         try {
             List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatient(patientId);
             return ResponseEntity.ok(appointments);
         } catch (EntityNotFoundException e) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Patient not found
         }
     }


    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')") // Only Admin/Secretary can create
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) {
        try {
            AppointmentDTO createdAppointment = appointmentService.createAppointment(appointmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Patient or Doctor not found
        } catch (IllegalArgumentException e) {
            // Handles validation errors (timing, conflicts, non-doctor user)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating appointment", e);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')") // Only Admin/Secretary can update
    public ResponseEntity<AppointmentDTO> updateAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentDTO appointmentDTO) {
        try {
            AppointmentDTO updatedAppointment = appointmentService.updateAppointment(id, appointmentDTO);
            return ResponseEntity.ok(updatedAppointment);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Appointment, Patient or Doctor not found
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Handles validation errors (timing, conflicts, non-doctor user, updating completed/cancelled)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating appointment", e);
        }
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')") // Only Admin/Secretary can cancel
    public ResponseEntity<AppointmentDTO> cancelAppointment(@PathVariable Long id) {
         try {
             AppointmentDTO cancelledAppointment = appointmentService.cancelAppointment(id);
             return ResponseEntity.ok(cancelledAppointment);
         } catch (EntityNotFoundException e) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Appointment not found
         } catch (IllegalArgumentException | IllegalStateException e) {
             // Handles already cancelled/completed or cancellation window violation
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
         } catch (Exception e) {
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error cancelling appointment", e);
         }
    }
}