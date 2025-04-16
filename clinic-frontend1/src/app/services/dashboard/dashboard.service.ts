import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../auth/auth.service'; // To get auth headers

// Interfaces for expected data structures
export interface AdminDashboardStats {
  totalPatients: number;
  appointmentsToday: number;
  monthlyRevenue: number;
  activeStaff: number;
  // Add percentage change fields if backend provides them
}

// Interface for Doctor Stats
export interface DoctorDashboardStats {
  appointmentsToday: number;
  totalPatients: number;
  pendingPrescriptions: number;
  nextAppointmentTime: string; // Or a Date object if backend sends structured time
}

// Interface matching AppointmentDTO (or import it)
import { AppointmentDTO } from '../../models/appointment.dto';

// Interface for Secretary Stats
export interface SecretaryDashboardStats {
  appointmentsToday: number;
  pendingInvoices: number;
  registeredPatients: number;
  urgentMatters: number;
}
export interface RevenueData {
  [month: string]: number; // e.g., { "Jan": 1500.50, "Feb": 2100.75 }
}

export interface AppointmentCountData {
   [day: string]: number; // e.g., { "Mon": 5, "Tue": 8 }
}


@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  // Base URLs for different dashboard types
  private adminApiUrl = `${environment.apiUrl}/admin/dashboard`;
  private doctorApiUrl = `${environment.apiUrl}/doctor/dashboard`;
  private secretaryApiUrl = `${environment.apiUrl}/secretary/dashboard`; // Uncomment and define

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // --- Admin Dashboard Methods ---

  getAdminStats(): Observable<AdminDashboardStats> {
    return this.http.get<AdminDashboardStats>(`${this.adminApiUrl}/stats`, { headers: this.authService.getAuthHeaders() });
  }

  getAdminRevenueOverview(year?: number): Observable<RevenueData> {
    let params = new HttpParams();
    if (year) {
      params = params.set('year', year.toString());
    }
    return this.http.get<RevenueData>(`${this.adminApiUrl}/revenue-overview`, { headers: this.authService.getAuthHeaders(), params });
  }

  getAdminWeeklyAppointments(startDate?: string): Observable<AppointmentCountData> {
     let params = new HttpParams();
     if (startDate) {
       params = params.set('startDate', startDate); // Expects YYYY-MM-DD format
     }
     return this.http.get<AppointmentCountData>(`${this.adminApiUrl}/weekly-appointments`, { headers: this.authService.getAuthHeaders(), params });
   }
// --- Doctor Dashboard Methods ---

getDoctorStats(): Observable<DoctorDashboardStats> {
  return this.http.get<DoctorDashboardStats>(`${this.doctorApiUrl}/stats`, { headers: this.authService.getAuthHeaders() });
}

// Note: Fetching today's appointments list is handled by AppointmentService,
// but if there was a specific dashboard endpoint for it, it would go here.
getDoctorTodayAppointments(): Observable<AppointmentDTO[]> {
   return this.http.get<AppointmentDTO[]>(`${this.doctorApiUrl}/today-appointments`, { headers: this.authService.getAuthHeaders() });
}
// --- Secretary Dashboard Methods ---

getSecretaryStats(): Observable<SecretaryDashboardStats> {
  return this.http.get<SecretaryDashboardStats>(`${this.secretaryApiUrl}/stats`, { headers: this.authService.getAuthHeaders() });
}

getSecretaryTodayAppointments(): Observable<AppointmentDTO[]> {
   return this.http.get<AppointmentDTO[]>(`${this.secretaryApiUrl}/today-appointments`, { headers: this.authService.getAuthHeaders() });
}

}
// Removed extra closing brace
