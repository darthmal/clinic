package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.PrescriptionDTO;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.model.core.Prescription;
import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.repository.core.PrescriptionRepository;
import com.clinicapp.backend.repository.security.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    // --- Mapping Logic ---

    private PrescriptionDTO mapToDTO(Prescription prescription) {
        return PrescriptionDTO.builder()
                .id(prescription.getId())
                .patientId(prescription.getPatient().getId())
                .doctorId(prescription.getDoctor().getId())
                .medicationName(prescription.getMedicationName())
                .dosage(prescription.getDosage())
                .frequency(prescription.getFrequency())
                .instructions(prescription.getInstructions())
                .prescriptionDate(prescription.getPrescriptionDate())
                .endDate(prescription.getEndDate())
                .patientFirstName(prescription.getPatient().getFirstName())
                .patientLastName(prescription.getPatient().getLastName())
                .doctorFirstName(prescription.getDoctor().getFirstName())
                .doctorLastName(prescription.getDoctor().getLastName())
                .createdAt(prescription.getCreatedAt())
                .updatedAt(prescription.getUpdatedAt())
                .build();
    }

    // --- Business Logic / Validation ---

    private void validateDoctor(User doctor) {
        if (doctor.getRole() != Role.DOCTOR) {
            throw new IllegalArgumentException("User issuing prescription is not a doctor.");
        }
    }

    // --- CRUD Operations ---

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPrescriptionsByPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + patientId));
        return prescriptionRepository.findByPatientOrderByPrescriptionDateDesc(patient).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPrescriptionsByDoctor(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + doctorId));
        validateDoctor(doctor);
        return prescriptionRepository.findByDoctorOrderByPrescriptionDateDesc(doctor).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PrescriptionDTO getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found with id: " + id));
        return mapToDTO(prescription);
    }

    @Transactional
    public PrescriptionDTO createPrescription(PrescriptionDTO prescriptionDTO) {
        Patient patient = patientRepository.findById(prescriptionDTO.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + prescriptionDTO.getPatientId()));
        User doctor = userRepository.findById(prescriptionDTO.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + prescriptionDTO.getDoctorId()));

        validateDoctor(doctor); // Ensure the user is a doctor

        Prescription prescription = Prescription.builder()
                .patient(patient)
                .doctor(doctor)
                .medicationName(prescriptionDTO.getMedicationName())
                .dosage(prescriptionDTO.getDosage())
                .frequency(prescriptionDTO.getFrequency())
                .instructions(prescriptionDTO.getInstructions())
                .prescriptionDate(prescriptionDTO.getPrescriptionDate())
                .endDate(prescriptionDTO.getEndDate())
                // createdAt/updatedAt handled by @PrePersist
                .build();

        Prescription savedPrescription = prescriptionRepository.save(prescription);
        return mapToDTO(savedPrescription);
    }

    @Transactional
    public PrescriptionDTO updatePrescription(Long id, PrescriptionDTO prescriptionDTO) {
        Prescription existingPrescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prescription not found with id: " + id));

        // Re-validate patient and doctor if IDs change (though usually they shouldn't for an update)
        Patient patient = patientRepository.findById(prescriptionDTO.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + prescriptionDTO.getPatientId()));
        User doctor = userRepository.findById(prescriptionDTO.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + prescriptionDTO.getDoctorId()));

        validateDoctor(doctor);

        // Update fields
        existingPrescription.setPatient(patient);
        existingPrescription.setDoctor(doctor);
        existingPrescription.setMedicationName(prescriptionDTO.getMedicationName());
        existingPrescription.setDosage(prescriptionDTO.getDosage());
        existingPrescription.setFrequency(prescriptionDTO.getFrequency());
        existingPrescription.setInstructions(prescriptionDTO.getInstructions());
        existingPrescription.setPrescriptionDate(prescriptionDTO.getPrescriptionDate());
        existingPrescription.setEndDate(prescriptionDTO.getEndDate());
        // updatedAt handled by @PreUpdate

        Prescription updatedPrescription = prescriptionRepository.save(existingPrescription);
        return mapToDTO(updatedPrescription);
    }

    @Transactional
    public void deletePrescription(Long id) {
        if (!prescriptionRepository.existsById(id)) {
            throw new EntityNotFoundException("Prescription not found with id: " + id);
        }
        prescriptionRepository.deleteById(id);
    }
}