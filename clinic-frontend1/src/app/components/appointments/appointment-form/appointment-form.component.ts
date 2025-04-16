import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AppointmentService } from '../../../services/core/appointment.service';
import { PatientService } from '../../../services/core/patient.service';
import { StaffService } from '../../../services/core/staff.service'; // Use StaffService
import { AppointmentDTO } from '../../../models/appointment.dto'; // Import AppointmentStatus
import { PatientDTO } from '../../../models/patient.dto';
import { UserDTO } from '../../../models/user.dto'; // UserDTO is used for doctors list
import { AppointmentStatus, AppointmentStatusList } from '../../../models/appointment-status.enum';

@Component({
  selector: 'app-appointment-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './appointment-form.component.html',
  styleUrls: ['./appointment-form.component.css']
})
export class AppointmentFormComponent implements OnInit {
  appointmentForm!: FormGroup;
  isEditMode = false;
  appointmentId: number | null = null;
  isLoading = false;
  errorMessage: string | null = null;
  pageTitle = 'Schedule New Appointment';

  patients$: Observable<PatientDTO[]> | undefined;
  doctors$: Observable<UserDTO[]> | undefined;
  appointmentStatuses = AppointmentStatusList; // Expose enum values for template

  constructor(
    private fb: FormBuilder,
    private appointmentService: AppointmentService,
    private patientService: PatientService,
    private staffService: StaffService, // Inject StaffService
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Initialize form structure
    this.appointmentForm = this.fb.group({
      patientId: ['', Validators.required],
      doctorId: ['', Validators.required],
      startTime: ['', Validators.required],
      endTime: ['', Validators.required],
      room: [''],
      notes: [''],
      status: [AppointmentStatus.SCHEDULED] // Add status, default for new
      // TODO: Add custom validator to ensure endTime > startTime
    });

    // Load patients and doctors for dropdowns
    this.loadDropdownData();

    // Check for edit mode
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.appointmentId = +id;
        this.pageTitle = 'Edit Appointment';
        this.loadAppointmentData(this.appointmentId);
      }
    });
  }

  loadDropdownData(): void {
    this.patients$ = this.patientService.getPatients();
    this.doctors$ = this.staffService.getDoctors(); // Use StaffService
  }

  loadAppointmentData(id: number): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.appointmentService.getAppointmentById(id).subscribe({
      next: (appointment) => {
        this.isLoading = false;
        this.appointmentForm.patchValue({
          ...appointment,
          startTime: this.formatDateTimeForInput(appointment.startTime),
          endTime: this.formatDateTimeForInput(appointment.endTime)
        });
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to load appointment data.';
        console.error('Error loading appointment:', err);
      }
    });
  }

  onSubmit(): void {
    if (this.appointmentForm.invalid) {
      this.errorMessage = 'Please fill in all required fields correctly.';
      this.appointmentForm.markAllAsTouched();
      return;
    }

    // Ensure status is included, especially for create
    const formValue = this.appointmentForm.value;
    const appointmentData: AppointmentDTO = {
       ...formValue,
       patientId: +formValue.patientId,
       doctorId: +formValue.doctorId,
       status: this.isEditMode ? formValue.status : AppointmentStatus.SCHEDULED // Ensure SCHEDULED for create
    };

    this.isLoading = true;
    this.errorMessage = null;

    const operation = this.isEditMode && this.appointmentId
      ? this.appointmentService.updateAppointment(this.appointmentId, appointmentData)
      : this.appointmentService.createAppointment(appointmentData);

    operation.subscribe({
      next: () => {
        this.isLoading = false;
        console.log(`Appointment ${this.isEditMode ? 'updated' : 'created'} successfully`);
        this.router.navigate(['/appointments']); // Navigate back to calendar/list
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = this.extractBackendErrorMessage(err, `Failed to ${this.isEditMode ? 'update' : 'create'} appointment.`);
        console.error(`Error ${this.isEditMode ? 'updating' : 'creating'} appointment:`, err);
      }
    });
  }

  // Helper to format LocalDateTime string to datetime-local input format
  private formatDateTimeForInput(dateTime: string | Date | undefined): string {
     if (!dateTime) return '';
     try {
       const date = new Date(dateTime);
       const year = date.getFullYear();
       const month = ('0' + (date.getMonth() + 1)).slice(-2);
       const day = ('0' + date.getDate()).slice(-2);
       const hours = ('0' + date.getHours()).slice(-2);
       const minutes = ('0' + date.getMinutes()).slice(-2);
       return `${year}-${month}-${day}T${hours}:${minutes}`;
     } catch (e) {
       console.error("Error formatting date-time:", e);
       return '';
     }
   }

  // Helper to try and extract specific error messages from backend response
  private extractBackendErrorMessage(error: any, defaultMessage: string): string {
     if (error?.error?.message && error.status === 400) {
        return error.error.message;
     }
     if (error?.message) { return error.message; }
     if (error.status === 403) { return 'Access Denied.'; }
     if (error.status === 404) { return 'Resource not found.'; }
     return defaultMessage;
  }

  goBack(): void {
    this.router.navigate(['/appointments']);
  }
}
