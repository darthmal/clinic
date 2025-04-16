import { AppointmentStatus } from "./appointment-status.enum"; // Import the enum

// Matches com.clinicapp.backend.dto.core.AppointmentDTO
export interface AppointmentDTO {
  id?: number; // Optional for creation
  patientId: number;
  doctorId: number;
  startTime: string; // Use string for ISO date format
  endTime: string;   // Use string for ISO date format
  room?: string;
  status: AppointmentStatus; // Use the imported enum
  notes?: string;
  patientFirstName?: string; // Read-only, provided by backend
  patientLastName?: string;  // Read-only
  doctorFirstName?: string;  // Read-only
  doctorLastName?: string;   // Read-only
  createdAt?: string;        // Read-only
  updatedAt?: string;        // Read-only
}