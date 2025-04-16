import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PatientDTO } from '../../models/patient.dto'; // Import the DTO
import { AuthService } from '../auth/auth.service'; // To get auth headers

@Injectable({
  providedIn: 'root'
})
export class PatientService {
  private apiUrl = `${environment.apiUrl}/patients`; // Base URL for patient endpoints

  constructor(
    private http: HttpClient,
    private authService: AuthService // Inject AuthService
  ) {}

  // Get all patients (consider pagination for large datasets)
  getPatients(): Observable<PatientDTO[]> {
    return this.http.get<PatientDTO[]>(this.apiUrl, { headers: this.authService.getAuthHeaders() });
  }

  // Get a single patient by ID
  getPatientById(id: number): Observable<PatientDTO> {
    return this.http.get<PatientDTO>(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }

  // Create a new patient
  createPatient(patient: PatientDTO): Observable<PatientDTO> {
    // Remove read-only fields before sending
    const { id, createdAt, updatedAt, ...createData } = patient;
    return this.http.post<PatientDTO>(this.apiUrl, createData, { headers: this.authService.getAuthHeaders() });
  }

  // Update an existing patient
  updatePatient(id: number, patient: PatientDTO): Observable<PatientDTO> {
     // Remove read-only fields before sending
     const { createdAt, updatedAt, ...updateData } = patient;
    return this.http.put<PatientDTO>(`${this.apiUrl}/${id}`, updateData, { headers: this.authService.getAuthHeaders() });
  }

  // Delete a patient
  deletePatient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }
}
