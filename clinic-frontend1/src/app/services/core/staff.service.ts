import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserDTO } from '../../models/user.dto'; // Use the existing UserDTO
import { AuthService } from '../auth/auth.service'; // To get auth headers

@Injectable({
  providedIn: 'root'
})
export class StaffService {
  private apiUrl = `${environment.apiUrl}/staff`; // Base URL for staff endpoints

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // Get only doctors
  getDoctors(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(`${this.apiUrl}/doctors`, { headers: this.authService.getAuthHeaders() });
  }

  // Could add methods to get other staff roles if needed later
  // getSecretaries(): Observable<UserDTO[]> { ... }
}
