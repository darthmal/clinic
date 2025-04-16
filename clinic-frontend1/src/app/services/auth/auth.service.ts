import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment'; // Assuming environment setup

// Interfaces matching backend DTOs
interface LoginRequest {
  email: string;
  password?: string; // Optional for potential future passwordless flows
}

interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  role: 'ADMIN' | 'DOCTOR' | 'SECRETARY'; // Match backend Role enum
}

interface AuthResponse {
  token: string;
  user: { // Simplified user details needed by frontend
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: 'ADMIN' | 'DOCTOR' | 'SECRETARY';
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`; // Use environment variable for API base URL
  private tokenKey = 'clinic_auth_token';
  private userKey = 'clinic_auth_user';

  // BehaviorSubjects to hold authentication state
  private loggedIn = new BehaviorSubject<boolean>(this.hasToken());
  private currentUser = new BehaviorSubject<AuthResponse['user'] | null>(this.getUser());

  // Public observables for components to subscribe to
  isLoggedIn$: Observable<boolean> = this.loggedIn.asObservable();
  currentUser$: Observable<AuthResponse['user'] | null> = this.currentUser.asObservable();

  constructor(private http: HttpClient) {}

  private hasToken(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  private getUser(): AuthResponse['user'] | null {
    const userJson = localStorage.getItem(this.userKey);
    if (!userJson) {
      return null; // Return null if no user data in localStorage
    }
    try {
      return JSON.parse(userJson); // Parse only if userJson exists
    } catch (e) {
      console.error("Error parsing user data from localStorage", e);
      localStorage.removeItem(this.userKey); // Clear invalid data
      return null; // Return null if parsing fails
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getCurrentUserRole(): 'ADMIN' | 'DOCTOR' | 'SECRETARY' | null {
    return this.currentUser.value?.role ?? null;
  }

  // Getter for the synchronous current user value
  getCurrentUserValue(): AuthResponse['user'] | null {
    return this.currentUser.value;
  }
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        localStorage.setItem(this.tokenKey, response.token);
        localStorage.setItem(this.userKey, JSON.stringify(response.user));
        this.loggedIn.next(true);
        this.currentUser.next(response.user);
      })
    );
  }

  // Optional: Register method if needed from frontend
  register(userInfo: RegisterRequest): Observable<AuthResponse> {
     return this.http.post<AuthResponse>(`${this.apiUrl}/register`, userInfo).pipe(
       tap(response => {
         // Optionally log in the user immediately after registration
         localStorage.setItem(this.tokenKey, response.token);
         localStorage.setItem(this.userKey, JSON.stringify(response.user));
         this.loggedIn.next(true);
         this.currentUser.next(response.user);
       })
     );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.loggedIn.next(false);
    this.currentUser.next(null);
    // Optionally: Navigate to login page via Router
    // this.router.navigate(['/login']);
    // Optionally: Call a backend logout endpoint if it exists
  }

  // Helper to get HttpHeaders with Authorization token
  getAuthHeaders(): HttpHeaders {
    const token = this.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }
}