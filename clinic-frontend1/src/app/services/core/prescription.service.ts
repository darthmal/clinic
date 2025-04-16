import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PrescriptionDTO } from '../../models/prescription.dto'; // Import the DTO
import { AuthService } from '../auth/auth.service'; // To get auth headers

@Injectable({
  providedIn: 'root'
})
export class PrescriptionService {
  private apiUrl = `${environment.apiUrl}/prescriptions`; // Base URL for prescription endpoints
  private docApiUrl = `${environment.apiUrl}/documents`; // Base URL for document endpoints

  constructor(
    private http: HttpClient,
    private authService: AuthService // Inject AuthService
  ) {}

  // Get prescriptions with optional filtering
  getPrescriptions(patientId?: number, doctorId?: number): Observable<PrescriptionDTO[]> {
    let params = new HttpParams();
    if (patientId) {
      params = params.set('patientId', patientId.toString());
    }
    if (doctorId) {
      params = params.set('doctorId', doctorId.toString());
    }
    return this.http.get<PrescriptionDTO[]>(this.apiUrl, { headers: this.authService.getAuthHeaders(), params });
  }

  // Get a single prescription by ID
  getPrescriptionById(id: number): Observable<PrescriptionDTO> {
    return this.http.get<PrescriptionDTO>(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }

  // Create a new prescription
  createPrescription(prescription: PrescriptionDTO): Observable<PrescriptionDTO> {
    // Remove read-only fields before sending
    const { id, patientFirstName, patientLastName, doctorFirstName, doctorLastName, createdAt, updatedAt, ...createData } = prescription;
    return this.http.post<PrescriptionDTO>(this.apiUrl, createData, { headers: this.authService.getAuthHeaders() });
  }

  // Update an existing prescription (if allowed by business logic)
  updatePrescription(id: number, prescription: PrescriptionDTO): Observable<PrescriptionDTO> {
     // Remove read-only fields before sending
     const { patientFirstName, patientLastName, doctorFirstName, doctorLastName, createdAt, updatedAt, ...updateData } = prescription;
    return this.http.put<PrescriptionDTO>(`${this.apiUrl}/${id}`, updateData, { headers: this.authService.getAuthHeaders() });
  }

  // Delete a prescription (use with caution, might not be standard practice)
  deletePrescription(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }

  // Download Prescription PDF
  downloadPrescriptionPdf(id: number): Observable<Blob> {
    // Request the PDF as a Blob
    return this.http.get(`${this.docApiUrl}/prescriptions/${id}/pdf`, {
      headers: this.authService.getAuthHeaders(),
      responseType: 'blob' // Important: response type is blob for files
    });
  }
}
