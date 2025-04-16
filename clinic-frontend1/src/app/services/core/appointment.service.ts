import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AppointmentDTO } from '../../models/appointment.dto'; // Import the DTO
import { AuthService } from '../auth/auth.service'; // To get auth headers

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private apiUrl = `${environment.apiUrl}/appointments`; // Base URL for appointment endpoints

  constructor(
    private http: HttpClient,
    private authService: AuthService // Inject AuthService
  ) {}

  // Get all appointments (consider pagination/filtering for large datasets)
  getAppointments(): Observable<AppointmentDTO[]> {
    return this.http.get<AppointmentDTO[]>(this.apiUrl, { headers: this.authService.getAuthHeaders() });
  }

  // Get appointments within a specific date range (for FullCalendar)
  getAppointmentsInRange(start: Date, end: Date): Observable<AppointmentDTO[]> {
    const params = new HttpParams()
      .set('start', start.toISOString())
      .set('end', end.toISOString());
    // Assuming backend supports start/end query parameters
    // Adjust endpoint if needed, e.g., /api/v1/appointments/range
    return this.http.get<AppointmentDTO[]>(this.apiUrl, { headers: this.authService.getAuthHeaders(), params });
  }

   // Get appointments for a specific doctor
   getAppointmentsByDoctor(doctorId: number): Observable<AppointmentDTO[]> {
     return this.http.get<AppointmentDTO[]>(`${this.apiUrl}/doctor/${doctorId}`, { headers: this.authService.getAuthHeaders() });
   }

   // Get appointments for a specific patient
   getAppointmentsByPatient(patientId: number): Observable<AppointmentDTO[]> {
     return this.http.get<AppointmentDTO[]>(`${this.apiUrl}/patient/${patientId}`, { headers: this.authService.getAuthHeaders() });
   }

  // Get a single appointment by ID
  getAppointmentById(id: number): Observable<AppointmentDTO> {
    return this.http.get<AppointmentDTO>(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }

  // Create a new appointment
  createAppointment(appointment: AppointmentDTO): Observable<AppointmentDTO> {
    // Remove read-only fields before sending
    const { id, patientFirstName, patientLastName, doctorFirstName, doctorLastName, createdAt, updatedAt, ...createData } = appointment;
    return this.http.post<AppointmentDTO>(this.apiUrl, createData, { headers: this.authService.getAuthHeaders() });
  }

  // Update an existing appointment
  updateAppointment(id: number, appointment: AppointmentDTO): Observable<AppointmentDTO> {
     // Remove read-only fields before sending
     const { patientFirstName, patientLastName, doctorFirstName, doctorLastName, createdAt, updatedAt, ...updateData } = appointment;
    return this.http.put<AppointmentDTO>(`${this.apiUrl}/${id}`, updateData, { headers: this.authService.getAuthHeaders() });
  }

  // Cancel an appointment
  cancelAppointment(id: number): Observable<AppointmentDTO> {
    return this.http.post<AppointmentDTO>(`${this.apiUrl}/${id}/cancel`, {}, { headers: this.authService.getAuthHeaders() });
  }

  // Note: Delete endpoint might not exist based on backend implementation (cancel is preferred)
  // deleteAppointment(id: number): Observable<void> {
  //   return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  // }
}
