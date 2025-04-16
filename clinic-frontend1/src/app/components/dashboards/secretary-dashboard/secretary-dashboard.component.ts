import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common'; // Import DatePipe
import { Router, RouterLink } from '@angular/router'; // Import Router, RouterLink
import { Observable } from 'rxjs';
import { DashboardService, SecretaryDashboardStats } from '../../../services/dashboard/dashboard.service'; // Import DashboardService
import { AppointmentDTO } from '../../../models/appointment.dto'; // Import AppointmentDTO
import { AuthService } from '../../../services/auth/auth.service'; // Import AuthService

@Component({
  selector: 'app-secretary-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe], // Add RouterLink, DatePipe
  templateUrl: './secretary-dashboard.component.html',
  styleUrls: ['./secretary-dashboard.component.css']
})
export class SecretaryDashboardComponent implements OnInit {

  isLoading = true;
  errorMessage: string | null = null;
  currentUserName: string = '';

  // Data properties
  stats: SecretaryDashboardStats | null = null;
  todayAppointments$: Observable<AppointmentDTO[]> | undefined;

  constructor(
    private dashboardService: DashboardService,
    private authService: AuthService, // Inject AuthService
    private router: Router // Inject Router for navigation
  ) {}

  ngOnInit(): void {
    this.currentUserName = this.authService.getCurrentUserValue()?.firstName ?? 'Secretary';
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.todayAppointments$ = this.dashboardService.getSecretaryTodayAppointments();

    this.dashboardService.getSecretaryStats().subscribe({
      next: (statsData) => {
        this.stats = statsData;
        this.isLoading = false; // Set loading false after stats arrive
      },
      error: (err) => {
        console.error('Error loading secretary dashboard stats:', err);
        this.errorMessage = 'Failed to load dashboard statistics.';
        if (err.status === 403) {
           this.errorMessage = 'Access Denied: You do not have permission to view this dashboard.';
        }
        this.isLoading = false;
      }
    });

    // Handle potential error for appointments separately
    if (this.todayAppointments$) {
        this.todayAppointments$.subscribe({
           error: (err) => {
               console.error('Error loading today\'s appointments:', err);
               this.errorMessage = (this.errorMessage ? this.errorMessage + ' ' : '') + 'Failed to load today\'s appointments.';
               if (!this.stats) { this.isLoading = false; }
           }
        });
    } else {
        this.errorMessage = 'Failed to initialize appointments loading.';
        this.isLoading = false;
    }
  }

  // Action handlers specific to Secretary
  addAppointment(): void {
    console.log('Navigating to add appointment');
    this.router.navigate(['/appointments/new']); // Navigate to the new appointment form route
    // Removed alert
  }

  editAppointment(appointmentId: number | undefined): void {
     if (!appointmentId) return;
     console.log('Navigating to edit appointment:', appointmentId);
     this.router.navigate(['/appointments/edit', appointmentId]); // Navigate to the edit appointment form route
  }

   checkIn(appointmentId: number | undefined): void {
     if (!appointmentId) return;
     console.log('Checking in appointment:', appointmentId);
     // TODO: Implement check-in logic (likely involves updating appointment status via AppointmentService)
     alert('Check-in functionality not yet implemented.');
   }

}
