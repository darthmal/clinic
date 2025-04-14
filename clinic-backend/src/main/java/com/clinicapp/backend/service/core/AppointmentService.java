package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.AppointmentDTO;
import com.clinicapp.backend.model.core.Appointment;
import com.clinicapp.backend.model.core.AppointmentStatus;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.core.AppointmentRepository;
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.model.notification.Notification; // Import Notification model
import com.clinicapp.backend.repository.security.UserRepository;
import com.clinicapp.backend.service.notification.NotificationService; // Import NotificationService
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository; // To fetch doctor details
    private final NotificationService notificationService; // Inject NotificationService

    private static final long CANCELLATION_NOTICE_HOURS = 24; // Configurable notice period

    // --- Mapping Logic ---

    private AppointmentDTO mapToDTO(Appointment appointment) {
        return AppointmentDTO.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .doctorId(appointment.getDoctor().getId())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .room(appointment.getRoom())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .patientFirstName(appointment.getPatient().getFirstName()) // Include names
                .patientLastName(appointment.getPatient().getLastName())
                .doctorFirstName(appointment.getDoctor().getFirstName())
                .doctorLastName(appointment.getDoctor().getLastName())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

    // Note: Mapping from DTO to Entity happens within create/update methods
    // as we need to fetch related entities (Patient, Doctor).

    // --- Business Logic / Validation ---

    private void validateAppointmentTiming(LocalDateTime startTime, LocalDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Appointment end time must be after start time.");
        }
        // Add more checks if needed (e.g., minimum duration)
    }

    private void validateDoctor(User doctor) {
        if (doctor.getRole() != Role.DOCTOR) {
            throw new IllegalArgumentException("Assigned user is not a doctor.");
        }
    }

    private void checkDoctorAvailability(User doctor, LocalDateTime startTime, LocalDateTime endTime, Long excludeAppointmentId) {
         boolean overlaps;
         if (excludeAppointmentId != null) {
             overlaps = appointmentRepository.existsOverlappingAppointmentForDoctor(doctor, startTime, endTime, excludeAppointmentId);
         } else {
             overlaps = appointmentRepository.existsOverlappingAppointmentForDoctor(doctor, startTime, endTime);
         }
        if (overlaps) {
            throw new IllegalArgumentException("Doctor already has an overlapping appointment scheduled.");
        }
    }

    private void checkPatientAvailability(Patient patient, LocalDateTime startTime, Long excludeAppointmentId) {
        LocalDate appointmentDate = startTime.toLocalDate();
        boolean hasOtherAppointment;
         if (excludeAppointmentId != null) {
             hasOtherAppointment = appointmentRepository.existsOtherAppointmentForPatientOnDate(patient, appointmentDate, excludeAppointmentId);
         } else {
             hasOtherAppointment = appointmentRepository.existsOtherAppointmentForPatientOnDate(patient, appointmentDate);
         }
        if (hasOtherAppointment) {
            throw new IllegalArgumentException("Patient already has an appointment scheduled on this day.");
        }
    }


    // --- CRUD Operations ---

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

     @Transactional(readOnly = true)
     public List<AppointmentDTO> getAppointmentsByDoctor(Long doctorId) {
         User doctor = userRepository.findById(doctorId)
                 .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + doctorId));
         validateDoctor(doctor); // Ensure it's a doctor
         return appointmentRepository.findByDoctorAndStartTimeAfterOrderByStartTimeAsc(doctor, LocalDateTime.now().minusDays(1)) // Show recent past too?
                 .stream()
                 .map(this::mapToDTO)
                 .collect(Collectors.toList());
     }

     @Transactional(readOnly = true)
     public List<AppointmentDTO> getAppointmentsByPatient(Long patientId) {
         Patient patient = patientRepository.findById(patientId)
                 .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + patientId));
         return appointmentRepository.findByPatientAndStartTimeBetween(patient, LocalDateTime.now().minusYears(1), LocalDateTime.now().plusYears(1)) // Example range
                 .stream()
                 .map(this::mapToDTO)
                 .collect(Collectors.toList());
     }


    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id: " + id));
        return mapToDTO(appointment);
    }

    @Transactional
    public AppointmentDTO createAppointment(AppointmentDTO appointmentDTO) {
        validateAppointmentTiming(appointmentDTO.getStartTime(), appointmentDTO.getEndTime());

        Patient patient = patientRepository.findById(appointmentDTO.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + appointmentDTO.getPatientId()));
        User doctor = userRepository.findById(appointmentDTO.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + appointmentDTO.getDoctorId()));

        validateDoctor(doctor);
        checkDoctorAvailability(doctor, appointmentDTO.getStartTime(), appointmentDTO.getEndTime(), null);
        checkPatientAvailability(patient, appointmentDTO.getStartTime(), null);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .startTime(appointmentDTO.getStartTime())
                .endTime(appointmentDTO.getEndTime())
                .room(appointmentDTO.getRoom())
                .status(AppointmentStatus.SCHEDULED) // Default for new appointments
                .notes(appointmentDTO.getNotes())
                // createdAt/updatedAt handled by @PrePersist
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // --- Send Notifications ---
        // Notify Doctor
        notificationService.createNotification(
                doctor,
                Notification.NotificationType.APPOINTMENT_MODIFIED, // Or a more specific type like APPOINTMENT_CREATED
                "New Appointment Scheduled",
                String.format("New appointment with %s %s at %s",
                        patient.getFirstName(), patient.getLastName(), savedAppointment.getStartTime()),
                "APPOINTMENT",
                savedAppointment.getId()
        );
        // Notify Patient (Optional - depends on requirements)
        // Consider if patients have User accounts or if notifications are handled differently
        // If patients are Users:
        // notificationService.createNotification(patient.getUserAccount(), ...);

        return mapToDTO(savedAppointment);
    }

    @Transactional
    public AppointmentDTO updateAppointment(Long id, AppointmentDTO appointmentDTO) {
        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id: " + id));

        // Prevent updates if appointment is completed or cancelled? (Optional business rule)
        if (existingAppointment.getStatus() == AppointmentStatus.COMPLETED || existingAppointment.getStatus() == AppointmentStatus.CANCELLED) {
             throw new IllegalArgumentException("Cannot update a completed or cancelled appointment.");
        }

        validateAppointmentTiming(appointmentDTO.getStartTime(), appointmentDTO.getEndTime());

        Patient patient = patientRepository.findById(appointmentDTO.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + appointmentDTO.getPatientId()));
        User doctor = userRepository.findById(appointmentDTO.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + appointmentDTO.getDoctorId()));

        validateDoctor(doctor);
        // Check availability excluding the current appointment being updated
        checkDoctorAvailability(doctor, appointmentDTO.getStartTime(), appointmentDTO.getEndTime(), id);
        checkPatientAvailability(patient, appointmentDTO.getStartTime(), id);

        // Update fields
        existingAppointment.setPatient(patient);
        existingAppointment.setDoctor(doctor);
        existingAppointment.setStartTime(appointmentDTO.getStartTime());
        existingAppointment.setEndTime(appointmentDTO.getEndTime());
        existingAppointment.setRoom(appointmentDTO.getRoom());
        existingAppointment.setStatus(appointmentDTO.getStatus()); // Allow status update (e.g., to COMPLETED)
        existingAppointment.setNotes(appointmentDTO.getNotes());
        // updatedAt handled by @PreUpdate

        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);

        // --- Send Notifications ---
        // Notify Doctor
        notificationService.createNotification(
                doctor,
                Notification.NotificationType.APPOINTMENT_MODIFIED,
                "Appointment Updated",
                String.format("Appointment with %s %s at %s has been updated.",
                        patient.getFirstName(), patient.getLastName(), updatedAppointment.getStartTime()),
                "APPOINTMENT",
                updatedAppointment.getId()
        );
        // Notify Patient (Optional)
        // notificationService.createNotification(patient.getUserAccount(), ...);

        return mapToDTO(updatedAppointment);
    }

     @Transactional
     public AppointmentDTO cancelAppointment(Long id) {
         Appointment appointment = appointmentRepository.findById(id)
                 .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id: " + id));

         if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
             throw new IllegalStateException("Appointment is already cancelled.");
         }
         if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
             throw new IllegalStateException("Cannot cancel a completed appointment.");
         }

         // Check cancellation notice period
         LocalDateTime now = LocalDateTime.now();
         if (appointment.getStartTime().isBefore(now.plusHours(CANCELLATION_NOTICE_HOURS))) {
             throw new IllegalArgumentException(
                     "Appointment cannot be cancelled within " + CANCELLATION_NOTICE_HOURS + " hours of start time.");
         }

         appointment.setStatus(AppointmentStatus.CANCELLED);
         Appointment cancelledAppointment = appointmentRepository.save(appointment);
 
         // --- Send Notifications ---
         // Notify Doctor
         notificationService.createNotification(
                 cancelledAppointment.getDoctor(),
                 Notification.NotificationType.APPOINTMENT_CANCELLED,
                 "Appointment Cancelled",
                 String.format("Appointment with %s %s at %s has been cancelled.",
                         cancelledAppointment.getPatient().getFirstName(), cancelledAppointment.getPatient().getLastName(), cancelledAppointment.getStartTime()),
                 "APPOINTMENT",
                 cancelledAppointment.getId()
         );
         // Notify Patient (Optional)
         // notificationService.createNotification(cancelledAppointment.getPatient().getUserAccount(), ...);
 
         return mapToDTO(cancelledAppointment);
     }


    // Note: Deleting appointments might not be desirable; cancelling is usually preferred.
    // If deletion is needed, implement a delete method similar to PatientService.
    // @Transactional
    // public void deleteAppointment(Long id) { ... }
}