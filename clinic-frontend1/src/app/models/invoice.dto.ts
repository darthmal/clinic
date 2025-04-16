// Matches com.clinicapp.backend.dto.core.InvoiceDTO

export interface InvoiceDTO {
  id?: number; // Optional for creation
  patientId: number;
  appointmentId?: number; // Optional, might be linked to an appointment
  issueDate: string; // Use string for ISO date format (e.g., "YYYY-MM-DD")
  dueDate: string;   // Use string for ISO date format
  totalAmount: number;
  status: 'DRAFT' | 'SENT' | 'PAID' | 'OVERDUE' | 'CANCELLED'; // Match backend enum
  notes?: string;
  patientFirstName?: string; // Read-only
  patientLastName?: string;  // Read-only
  createdAt?: string;        // Read-only
  updatedAt?: string;        // Read-only
}