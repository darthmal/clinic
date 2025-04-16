// Matches com.clinicapp.backend.dto.core.PrescriptionDTO

export interface PrescriptionDTO {
  id?: number; // Optional for creation
  patientId: number;
  doctorId: number;
  medicationName: string;
  dosage: string;
  frequency: string;
  instructions?: string;
  prescriptionDate: string; // Use string for ISO date format (e.g., "YYYY-MM-DD")
  endDate?: string; // Optional, use string for ISO date format
  patientFirstName?: string; // Read-only
  patientLastName?: string;  // Read-only
  doctorFirstName?: string;  // Read-only
  doctorLastName?: string;   // Read-only
  createdAt?: string;        // Read-only
  updatedAt?: string;        // Read-only
}