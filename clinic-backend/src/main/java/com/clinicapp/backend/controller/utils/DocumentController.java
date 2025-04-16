package com.clinicapp.backend.controller.utils;

import com.clinicapp.backend.model.core.Invoice;
import com.clinicapp.backend.model.core.Prescription;
import com.clinicapp.backend.repository.core.InvoiceRepository;
import com.clinicapp.backend.repository.core.PrescriptionRepository;
import com.clinicapp.backend.service.utils.PdfGenerationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/documents") // Base path for document generation
@RequiredArgsConstructor
public class DocumentController {

    private final PdfGenerationService pdfGenerationService;
    private final PrescriptionRepository prescriptionRepository;
    private final InvoiceRepository invoiceRepository;

    @GetMapping("/prescriptions/{id}/pdf")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(@PathVariable Long id) {
        try {
            Prescription prescription = prescriptionRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Prescription not found with id: " + id));

            byte[] pdfBytes = pdfGenerationService.generatePrescriptionPdf(prescription);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // Suggest a filename for the download
            String filename = String.format("prescription_%d_patient_%d.pdf",
                                            prescription.getId(),
                                            prescription.getPatient().getId());
            headers.setContentDispositionFormData("attachment", filename); // Use "attachment" to force download
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IOException e) {
            // Log the error
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating prescription PDF", e);
        }
    }

    @GetMapping("/invoices/{id}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
         try {
             Invoice invoice = invoiceRepository.findById(id)
                     .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + id));

             byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoice);

             HttpHeaders headers = new HttpHeaders();
             headers.setContentType(MediaType.APPLICATION_PDF);
             String filename = String.format("invoice_%d_patient_%d.pdf",
                                             invoice.getId(),
                                             invoice.getPatient().getId());
             headers.setContentDispositionFormData("attachment", filename);
             headers.setContentLength(pdfBytes.length);

             return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

         } catch (EntityNotFoundException e) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
         } catch (IOException e) {
             // Log the error
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating invoice PDF", e);
         }
    }
}