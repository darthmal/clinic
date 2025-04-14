package com.clinicapp.backend.service.utils;

import com.clinicapp.backend.model.core.Invoice;
import com.clinicapp.backend.model.core.Prescription;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts; // Use Standard14Fonts
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerationService {

    private static final float MARGIN = 50;
    private static final float FONT_SIZE_NORMAL = 12;
    private static final float FONT_SIZE_LARGE = 16;
    private static final float LEADING = 1.5f * FONT_SIZE_NORMAL; // Line spacing

    private final PDType1Font FONT_NORMAL = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public byte[] generatePrescriptionPdf(Prescription prescription) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            float contentWidth = page.getMediaBox().getWidth() - 2 * MARGIN;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                // --- Header ---
                contentStream.setFont(FONT_BOLD, FONT_SIZE_LARGE);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Prescription");
                contentStream.endText();
                yPosition -= LEADING * 2; // Extra space after header

                // --- Patient & Doctor Info ---
                contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Patient:");
                contentStream.endText();

                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN + 100, yPosition); // Indent value
                contentStream.showText(prescription.getPatient().getFirstName() + " " + prescription.getPatient().getLastName());
                contentStream.endText();
                yPosition -= LEADING;

                contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Prescribed by:");
                contentStream.endText();

                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN + 100, yPosition);
                contentStream.showText("Dr. " + prescription.getDoctor().getFirstName() + " " + prescription.getDoctor().getLastName());
                contentStream.endText();
                yPosition -= LEADING;

                contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Date:");
                contentStream.endText();

                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN + 100, yPosition);
                contentStream.showText(prescription.getPrescriptionDate().format(DATE_FORMATTER));
                contentStream.endText();
                yPosition -= LEADING * 2; // Extra space

                // --- Prescription Details ---
                contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Medication:");
                contentStream.endText();

                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN + 100, yPosition);
                contentStream.showText(prescription.getMedicationName());
                contentStream.endText();
                yPosition -= LEADING;

                contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Dosage:");
                contentStream.endText();

                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN + 100, yPosition);
                contentStream.showText(prescription.getDosage());
                contentStream.endText();
                yPosition -= LEADING;

                contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Frequency:");
                contentStream.endText();

                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN + 100, yPosition);
                contentStream.showText(prescription.getFrequency());
                contentStream.endText();
                yPosition -= LEADING;

                if (prescription.getInstructions() != null && !prescription.getInstructions().isEmpty()) {
                    contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("Instructions:");
                    contentStream.endText();

                    contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN + 100, yPosition);
                    // Basic wrapping - consider a more robust text wrapping utility for long instructions
                    contentStream.showText(prescription.getInstructions());
                    contentStream.endText();
                    yPosition -= LEADING;
                }

                 if (prescription.getEndDate() != null) {
                     contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                     contentStream.beginText();
                     contentStream.newLineAtOffset(MARGIN, yPosition);
                     contentStream.showText("End Date:");
                     contentStream.endText();

                     contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                     contentStream.beginText();
                     contentStream.newLineAtOffset(MARGIN + 100, yPosition);
                     contentStream.showText(prescription.getEndDate().format(DATE_FORMATTER));
                     contentStream.endText();
                     yPosition -= LEADING;
                 }

                // --- Footer/Signature Line (Optional) ---
                yPosition -= LEADING * 3;
                contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Signature: _________________________");
                contentStream.endText();

            } // contentStream closes automatically

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] generateInvoicePdf(Invoice invoice) throws IOException {
         try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
             PDPage page = new PDPage();
             document.addPage(page);

             float yPosition = page.getMediaBox().getHeight() - MARGIN;
             float contentWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
             float rightMarginX = page.getMediaBox().getWidth() - MARGIN;

             try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                 // --- Header ---
                 contentStream.setFont(FONT_BOLD, FONT_SIZE_LARGE);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(MARGIN, yPosition);
                 contentStream.showText("Invoice");
                 contentStream.endText();
                 yPosition -= LEADING * 2;

                 // --- Clinic Info (Placeholder) ---
                 contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(MARGIN, yPosition);
                 contentStream.showText("Your Clinic Name");
                 contentStream.newLineAtOffset(0, -LEADING); // Move down
                 contentStream.showText("123 Clinic Address");
                 contentStream.newLineAtOffset(0, -LEADING);
                 contentStream.showText("City, Postal Code");
                 contentStream.newLineAtOffset(0, -LEADING);
                 contentStream.showText("Phone: (123) 456-7890");
                 contentStream.endText();

                 // --- Invoice Details (Right Aligned) ---
                 float invoiceDetailsY = yPosition; // Align with clinic info top
                 contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(rightMarginX - 150, invoiceDetailsY); // Adjust offset for alignment
                 contentStream.showText("Invoice #: ");
                 contentStream.endText();
                 contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(rightMarginX - 50, invoiceDetailsY);
                 contentStream.showText(String.valueOf(invoice.getId())); // Use invoice ID as number
                 contentStream.endText();
                 invoiceDetailsY -= LEADING;

                 contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(rightMarginX - 150, invoiceDetailsY);
                 contentStream.showText("Issue Date: ");
                 contentStream.endText();
                 contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(rightMarginX - 50, invoiceDetailsY);
                 contentStream.showText(invoice.getIssueDate().format(DATE_FORMATTER));
                 contentStream.endText();
                 invoiceDetailsY -= LEADING;

                 contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(rightMarginX - 150, invoiceDetailsY);
                 contentStream.showText("Due Date: ");
                 contentStream.endText();
                 contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(rightMarginX - 50, invoiceDetailsY);
                 contentStream.showText(invoice.getDueDate().format(DATE_FORMATTER));
                 contentStream.endText();
                 invoiceDetailsY -= LEADING;

                 contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(rightMarginX - 150, invoiceDetailsY);
                 contentStream.showText("Status: ");
                 contentStream.endText();
                 contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(rightMarginX - 50, invoiceDetailsY);
                 contentStream.showText(invoice.getStatus().name());
                 contentStream.endText();


                 yPosition -= LEADING * 6; // Move down past clinic/invoice details

                 // --- Bill To ---
                 contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(MARGIN, yPosition);
                 contentStream.showText("Bill To:");
                 contentStream.endText();
                 yPosition -= LEADING;

                 contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(MARGIN, yPosition);
                 contentStream.showText(invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName());
                 contentStream.newLineAtOffset(0, -LEADING);
                 contentStream.showText(invoice.getPatient().getAddress()); // Assuming Patient has address
                 contentStream.newLineAtOffset(0, -LEADING);
                 contentStream.showText("Phone: " + invoice.getPatient().getPhoneNumber());
                 if (invoice.getPatient().getEmail() != null) {
                     contentStream.newLineAtOffset(0, -LEADING);
                     contentStream.showText("Email: " + invoice.getPatient().getEmail());
                 }
                 contentStream.endText();
                 yPosition -= LEADING * 5; // Space before items


                 // --- Invoice Items (Simplified - using notes field) ---
                 // For a real application, you'd likely have InvoiceItem entities
                 contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(MARGIN, yPosition);
                 contentStream.showText("Description");
                 contentStream.newLineAtOffset(contentWidth - 50, 0); // Move to amount column
                 contentStream.showText("Amount");
                 contentStream.endText();
                 yPosition -= LEADING * 0.5f; // Small space

                 // Draw line
                 contentStream.moveTo(MARGIN, yPosition);
                 contentStream.lineTo(rightMarginX, yPosition);
                 contentStream.stroke();
                 yPosition -= LEADING;


                 contentStream.setFont(FONT_NORMAL, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 contentStream.newLineAtOffset(MARGIN, yPosition);
                 // Basic display of notes - needs wrapping for long text
                 String description = invoice.getNotes() != null ? invoice.getNotes() : "Consultation / Services";
                 if (invoice.getAppointment() != null) {
                     description += " (Appointment: " + invoice.getAppointment().getStartTime().format(DATE_FORMATTER) + ")";
                 }
                 contentStream.showText(description);

                 // Show total amount aligned right
                 String amountStr = String.format("%.2f", invoice.getTotalAmount());
                 float amountWidth = FONT_NORMAL.getStringWidth(amountStr) / 1000 * FONT_SIZE_NORMAL;
                 contentStream.newLineAtOffset(contentWidth - 50 - amountWidth, 0); // Adjust for alignment
                 contentStream.showText(amountStr);
                 contentStream.endText();
                 yPosition -= LEADING * 2; // Space after items


                 // --- Total ---
                 contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
                 contentStream.beginText();
                 float totalX = rightMarginX - 100; // Position for "Total:"
                 contentStream.newLineAtOffset(totalX, yPosition);
                 contentStream.showText("Total:");
                 // Align amount
                 String totalAmountStr = String.format("%.2f", invoice.getTotalAmount());
                 float totalAmountWidth = FONT_BOLD.getStringWidth(totalAmountStr) / 1000 * FONT_SIZE_NORMAL;
                 contentStream.newLineAtOffset(rightMarginX - totalX - totalAmountWidth, 0); // Adjust for alignment
                 contentStream.showText(totalAmountStr);
                 contentStream.endText();

             } // contentStream closes automatically

             document.save(outputStream);
             return outputStream.toByteArray();
         }
    }
}