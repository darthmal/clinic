import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserDTO, UserCreateUpdateDTO, UserRole } from '../../models/user.dto'; // Import DTOs
import { AuthService } from '../auth/auth.service'; // To get auth headers

@Injectable({
  providedIn: 'root'
})
export class UserService {
  // Assuming backend endpoints are under /api/v1/admin/users
  private apiUrl = `${environment.apiUrl}/admin/users`;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  // Get users with optional filtering by role
  getUsers(role?: UserRole): Observable<UserDTO[]> {
    let params = new HttpParams();
    if (role) {
      params = params.set('role', role);
    }
    return this.http.get<UserDTO[]>(this.apiUrl, { headers: this.authService.getAuthHeaders(), params });
  }

  // Get a single user by ID
  getUserById(id: number): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }

  // Create a new user (likely requires ADMIN role)
  createUser(user: UserCreateUpdateDTO): Observable<UserDTO> {
    // Ensure password is provided for creation
    if (!user.password) {
       throw new Error("Password is required for creating a new user.");
    }
    // Remove ID if present, backend assigns it
    const { id, ...createData } = user;
    return this.http.post<UserDTO>(this.apiUrl, createData, { headers: this.authService.getAuthHeaders() });
  }

  // Update an existing user (likely requires ADMIN role)
  updateUser(id: number, user: UserCreateUpdateDTO): Observable<UserDTO> {
    // Password might be optional for update (e.g., separate password reset flow)
    // Backend should handle null/empty password field appropriately (e.g., don't update if empty)
    return this.http.put<UserDTO>(`${this.apiUrl}/${id}`, user, { headers: this.authService.getAuthHeaders() });
  }

  // Delete a user (likely requires ADMIN role)
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }
}
