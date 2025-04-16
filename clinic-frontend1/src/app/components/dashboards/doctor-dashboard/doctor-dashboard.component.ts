import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common'; // Import DatePipe
import { RouterLink } from '@angular/router';
import { forkJoin, Observable } from 'rxjs';
import { DashboardService, DoctorDashboardStats } from '../../../services/dashboard/dashboard.service'; // Import DashboardService
import { AppointmentService } from '../../../services/core/appointment.service'; // Import AppointmentService
import { AppointmentDTO } from '../../../models/appointment.dto'; // Import AppointmentDTO
import { AuthService } from '../../../services/auth/auth.service'; // Import AuthService

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe], // Add DatePipe
  templateUrl: './doctor-dashboard.component.html',
  styleUrls: ['./doctor-dashboard.component.css']
})
export class DoctorDashboardComponent implements OnInit {

  isLoading = true;
  errorMessage: string | null = null;
  currentUserName: string = '';

  // Data properties
  stats: DoctorDashboardStats | null = null;
  todayAppointments$: Observable<AppointmentDTO[]> | undefined; // Use observable directly in template

  constructor(
    private dashboardService: DashboardService,
    private appointmentService: AppointmentService, // Inject AppointmentService
    private authService: AuthService // Inject AuthService
  ) {}

  ngOnInit(): void {
    this.currentUserName = this.authService.getCurrentUserValue()?.firstName ?? 'Doctor';
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.errorMessage = null;

    // Fetch stats and today's appointments
    this.todayAppointments$ = this.dashboardService.getDoctorTodayAppointments(); // Call correct service method

    this.dashboardService.getDoctorStats().subscribe({
        next: (statsData) => {
          this.stats = statsData;
          // Combine loading state - set to false only when both requests potentially finish
          // For simplicity, we set isLoading based on stats fetch only for now.
          // A more robust solution might track loading state for both observables.
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error loading doctor dashboard stats:', err);
          this.errorMessage = 'Failed to load dashboard statistics.';
          if (err.status === 403) {
             this.errorMessage = 'Access Denied: You do not have permission to view this dashboard.';
          }
          this.isLoading = false; // Also set loading false on error
        }
      });

      // Handle potential error for appointments separately if needed
      // Check if observable exists before subscribing (though it should now)
      if (this.todayAppointments$) {
          this.todayAppointments$.subscribe({
             error: (err) => {
                 console.error('Error loading today\'s appointments:', err);
                 // Display a separate error message or combine them
                 this.errorMessage = (this.errorMessage ? this.errorMessage + ' ' : '') + 'Failed to load today\'s appointments.';
                 if (!this.stats) { // Only set isLoading false if stats haven't loaded either
                    this.isLoading = false;
                 }
             }
          });
      } else {
          // This case should ideally not happen now
          this.errorMessage = 'Failed to initialize appointments loading.';
          this.isLoading = false;
      }
  }

  // Placeholder for potential actions like checking in a patient
  checkIn(appointmentId: number | undefined): void {
    if (!appointmentId) return;
    console.log('Checking in appointment:', appointmentId);
    // TODO: Implement check-in logic (likely involves updating appointment status via AppointmentService)
    alert('Check-in functionality not yet implemented.');
  }

  viewPatient(patientId: number | undefined): void {
     if (!patientId) return;
     console.log('Viewing patient:', patientId);
     // TODO: Navigate to patient detail view
     alert('Navigate to patient detail view not yet implemented.');
     // this.router.navigate(['/patients', patientId]); // Example navigation
  }
}
