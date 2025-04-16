// Matches com.clinicapp.backend.dto.core.PatientDTO

export interface PatientDTO {
  id?: number; // Optional for creation
  firstName: string;
  lastName: string;
  dateOfBirth: string; // Use string for ISO date format (e.g., "YYYY-MM-DD")
  gender?: 'MALE' | 'FEMALE' | 'OTHER' | 'UNKNOWN'; // Optional, match backend enum if exists
  address?: string;
  phoneNumber?: string;
  email?: string;
  medicalHistory?: string; // Consider if this should be more structured
  allergies?: string;
  createdAt?: string; // Read-only
  updatedAt?: string; // Read-only
}