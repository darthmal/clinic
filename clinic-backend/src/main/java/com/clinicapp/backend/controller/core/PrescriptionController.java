package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.PrescriptionDTO;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.service.core.PrescriptionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions") // Base path for prescription endpoints
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    // Get prescriptions for a specific patient
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByPatient(@PathVariable Long patientId) {
        try {
            List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionsByPatient(patientId);
            return ResponseEntity.ok(prescriptions);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Patient not found
        }
    }

    // Get prescriptions issued by a specific doctor
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByDoctor(@PathVariable Long doctorId) {
         try {
             List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionsByDoctor(doctorId);
             return ResponseEntity.ok(prescriptions);
         } catch (EntityNotFoundException e) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Doctor not found
         } catch (IllegalArgumentException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e); // User is not a doctor
         }
    }

    // Get prescriptions issued by the connected doctor
    @GetMapping
    public ResponseEntity<List<PrescriptionDTO>> getPrescriptionsByLoggedInUser() {
        try {
            User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<PrescriptionDTO> prescriptions = prescriptionService.getPrescriptionsByDoctor(loggedInUser.getId());
            return ResponseEntity.ok(prescriptions);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionDTO> getPrescriptionById(@PathVariable Long id) {
        try {
            PrescriptionDTO prescription = prescriptionService.getPrescriptionById(id);
            return ResponseEntity.ok(prescription);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PostMapping
    public ResponseEntity<PrescriptionDTO> createPrescription(@Valid @RequestBody PrescriptionDTO prescriptionDTO) {
        try {
            PrescriptionDTO createdPrescription = prescriptionService.createPrescription(prescriptionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPrescription);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Patient or Doctor not found
        } catch (IllegalArgumentException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e); // User is not a doctor
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating prescription", e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionDTO> updatePrescription(@PathVariable Long id, @Valid @RequestBody PrescriptionDTO prescriptionDTO) {
        try {
            PrescriptionDTO updatedPrescription = prescriptionService.updatePrescription(id, prescriptionDTO);
            return ResponseEntity.ok(updatedPrescription);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Prescription, Patient or Doctor not found
        } catch (IllegalArgumentException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e); // User is not a doctor
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating prescription", e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrescription(@PathVariable Long id) {
        try {
            prescriptionService.deletePrescription(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting prescription", e);
        }
    }
}