package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.InvoiceDTO;
import com.clinicapp.backend.model.core.Appointment;
import com.clinicapp.backend.model.core.Invoice;
import com.clinicapp.backend.model.core.InvoiceStatus;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.repository.core.AppointmentRepository;
import com.clinicapp.backend.repository.core.InvoiceRepository;
import com.clinicapp.backend.repository.core.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository; // Optional, if linking invoices to appointments

    // --- Mapping Logic ---

    private InvoiceDTO mapToDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .patientId(invoice.getPatient().getId())
                .appointmentId(invoice.getAppointment() != null ? invoice.getAppointment().getId() : null)
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .patientFirstName(invoice.getPatient().getFirstName())
                .patientLastName(invoice.getPatient().getLastName())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }

    // --- Business Logic / Validation ---

    private void validateDates(LocalDate issueDate, LocalDate dueDate) {
        if (dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Due date cannot be before issue date.");
        }
    }

    // --- CRUD Operations ---

    @Transactional(readOnly = true)
    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> getInvoicesByPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + patientId));
        return invoiceRepository.findByPatientOrderByIssueDateDesc(patient).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

     @Transactional(readOnly = true)
     public List<InvoiceDTO> getInvoicesByStatus(InvoiceStatus status) {
         return invoiceRepository.findByStatusOrderByDueDateAsc(status).stream()
                 .map(this::mapToDTO)
                 .collect(Collectors.toList());
     }

     @Transactional(readOnly = true)
     public List<InvoiceDTO> getUnpaidInvoices() {
         List<InvoiceStatus> unpaidStatuses = Arrays.asList(InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE);
         return invoiceRepository.findByStatusInOrderByDueDateAsc(unpaidStatuses).stream() // Assuming findByStatusIn exists or is added
                 .map(this::mapToDTO)
                 .collect(Collectors.toList());
     }


    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + id));
        return mapToDTO(invoice);
    }

    @Transactional
    public InvoiceDTO createInvoice(InvoiceDTO invoiceDTO) {
        validateDates(invoiceDTO.getIssueDate(), invoiceDTO.getDueDate());

        Patient patient = patientRepository.findById(invoiceDTO.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + invoiceDTO.getPatientId()));

        Appointment appointment = null;
        if (invoiceDTO.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(invoiceDTO.getAppointmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id: " + invoiceDTO.getAppointmentId()));
            // Optional: Check if appointment patient matches invoice patient
            if (!appointment.getPatient().getId().equals(patient.getId())) {
                 throw new IllegalArgumentException("Invoice patient does not match appointment patient.");
            }
        }


        Invoice invoice = Invoice.builder()
                .patient(patient)
                .appointment(appointment) // Can be null
                .issueDate(invoiceDTO.getIssueDate())
                .dueDate(invoiceDTO.getDueDate())
                .totalAmount(invoiceDTO.getTotalAmount())
                .status(invoiceDTO.getStatus() != null ? invoiceDTO.getStatus() : InvoiceStatus.DRAFT) // Default to DRAFT
                .notes(invoiceDTO.getNotes())
                // createdAt/updatedAt handled by @PrePersist
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToDTO(savedInvoice);
    }

    @Transactional
    public InvoiceDTO updateInvoice(Long id, InvoiceDTO invoiceDTO) {
        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + id));

        // Prevent updates if PAID or CANCELLED? (Optional business rule)
         if (existingInvoice.getStatus() == InvoiceStatus.PAID || existingInvoice.getStatus() == InvoiceStatus.CANCELLED) {
             throw new IllegalStateException("Cannot update a paid or cancelled invoice.");
         }

        validateDates(invoiceDTO.getIssueDate(), invoiceDTO.getDueDate());

        Patient patient = patientRepository.findById(invoiceDTO.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + invoiceDTO.getPatientId()));

        Appointment appointment = null;
         if (invoiceDTO.getAppointmentId() != null) {
             appointment = appointmentRepository.findById(invoiceDTO.getAppointmentId())
                     .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id: " + invoiceDTO.getAppointmentId()));
             if (!appointment.getPatient().getId().equals(patient.getId())) {
                  throw new IllegalArgumentException("Invoice patient does not match appointment patient.");
             }
         }

        // Update fields
        existingInvoice.setPatient(patient);
        existingInvoice.setAppointment(appointment);
        existingInvoice.setIssueDate(invoiceDTO.getIssueDate());
        existingInvoice.setDueDate(invoiceDTO.getDueDate());
        existingInvoice.setTotalAmount(invoiceDTO.getTotalAmount());
        existingInvoice.setStatus(invoiceDTO.getStatus());
        existingInvoice.setNotes(invoiceDTO.getNotes());
        // updatedAt handled by @PreUpdate

        Invoice updatedInvoice = invoiceRepository.save(existingInvoice);
        return mapToDTO(updatedInvoice);
    }

     @Transactional
     public InvoiceDTO updateInvoiceStatus(Long id, InvoiceStatus newStatus) {
         Invoice invoice = invoiceRepository.findById(id)
                 .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + id));

         // Add any status transition validation if needed
         // e.g., cannot move from PAID back to DRAFT

         invoice.setStatus(newStatus);
         Invoice updatedInvoice = invoiceRepository.save(invoice);
         return mapToDTO(updatedInvoice);
     }


    @Transactional
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new EntityNotFoundException("Invoice not found with id: " + id);
        }
        // Consider implications before allowing deletion (e.g., if paid)
        invoiceRepository.deleteById(id);
    }
}