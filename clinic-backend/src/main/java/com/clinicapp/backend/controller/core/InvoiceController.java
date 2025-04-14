package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.InvoiceDTO;
import com.clinicapp.backend.model.core.InvoiceStatus;
import com.clinicapp.backend.service.core.InvoiceService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices") // Base path for invoice endpoints
@RequiredArgsConstructor
// TODO: Add role-based security annotations (e.g., @PreAuthorize - likely SECRETARY/ADMIN)
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices(
            @RequestParam(required = false) InvoiceStatus status // Optional filter by status
    ) {
        List<InvoiceDTO> invoices;
        if (status != null) {
            invoices = invoiceService.getInvoicesByStatus(status);
        } else {
            invoices = invoiceService.getAllInvoices();
        }
        return ResponseEntity.ok(invoices);
    }

     @GetMapping("/unpaid")
     public ResponseEntity<List<InvoiceDTO>> getUnpaidInvoices() {
         List<InvoiceDTO> invoices = invoiceService.getUnpaidInvoices();
         return ResponseEntity.ok(invoices);
     }


    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByPatient(@PathVariable Long patientId) {
        try {
            List<InvoiceDTO> invoices = invoiceService.getInvoicesByPatient(patientId);
            return ResponseEntity.ok(invoices);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Patient not found
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable Long id) {
        try {
            InvoiceDTO invoice = invoiceService.getInvoiceById(id);
            return ResponseEntity.ok(invoice);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(@Valid @RequestBody InvoiceDTO invoiceDTO) {
        try {
            InvoiceDTO createdInvoice = invoiceService.createInvoice(invoiceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInvoice);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Patient or Appointment not found
        } catch (IllegalArgumentException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e); // Date validation, patient mismatch
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating invoice", e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDTO> updateInvoice(@PathVariable Long id, @Valid @RequestBody InvoiceDTO invoiceDTO) {
        try {
            InvoiceDTO updatedInvoice = invoiceService.updateInvoice(id, invoiceDTO);
            return ResponseEntity.ok(updatedInvoice);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e); // Invoice, Patient or Appointment not found
        } catch (IllegalArgumentException | IllegalStateException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e); // Date validation, patient mismatch, updating paid/cancelled
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating invoice", e);
        }
    }

     // Endpoint to specifically update status (e.g., mark as PAID)
     @PatchMapping("/{id}/status")
     public ResponseEntity<InvoiceDTO> updateInvoiceStatus(
             @PathVariable Long id,
             @RequestParam InvoiceStatus status // Pass new status as query param
     ) {
         try {
             InvoiceDTO updatedInvoice = invoiceService.updateInvoiceStatus(id, status);
             return ResponseEntity.ok(updatedInvoice);
         } catch (EntityNotFoundException e) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
         } catch (Exception e) {
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating invoice status", e);
         }
     }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        try {
            invoiceService.deleteInvoice(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (Exception e) {
            // Consider specific exceptions for constraint violations if deletion is blocked
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting invoice", e);
        }
    }
}