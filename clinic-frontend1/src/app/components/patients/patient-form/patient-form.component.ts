import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PatientService } from '../../../services/core/patient.service';
import { PatientDTO } from '../../../models/patient.dto';

@Component({
  selector: 'app-patient-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // Import ReactiveFormsModule
  templateUrl: './patient-form.component.html',
  styleUrls: ['./patient-form.component.css']
})
export class PatientFormComponent implements OnInit {
  patientForm!: FormGroup;
  isEditMode = false;
  patientId: number | null = null;
  isLoading = false;
  errorMessage: string | null = null;
  pageTitle = 'Add New Patient'; // Dynamic title

  constructor(
    private fb: FormBuilder,
    private patientService: PatientService,
    private router: Router,
    private route: ActivatedRoute // To get patient ID from route params for editing
  ) {}

  ngOnInit(): void {
    this.patientForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      dateOfBirth: ['', Validators.required], // Consider adding date validation
      gender: [''], // Optional
      address: [''], // Optional
      phoneNumber: [''], // Optional, consider pattern validation
      email: ['', Validators.email], // Optional, email validation
      medicalHistory: [''], // Optional
      allergies: [''] // Optional
    });

    // Check if we are in edit mode by looking for an 'id' parameter in the route
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.patientId = +id; // Convert string ID to number
        this.pageTitle = 'Edit Patient';
        this.loadPatientData(this.patientId);
      }
    });
  }

  loadPatientData(id: number): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.patientService.getPatientById(id).subscribe({
      next: (patient) => {
        this.isLoading = false;
        // Patch the form with existing patient data
        // Ensure dateOfBirth is formatted correctly if needed (e.g., 'YYYY-MM-DD')
        this.patientForm.patchValue(patient);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to load patient data. Please try again.';
        console.error('Error loading patient:', err);
        // Optionally navigate back or show a persistent error
      }
    });
  }

  onSubmit(): void {
    if (this.patientForm.invalid) {
      this.errorMessage = 'Please fill in all required fields correctly.';
      // Mark all fields as touched to show validation errors
      this.patientForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    const patientData: PatientDTO = this.patientForm.value;

    if (this.isEditMode && this.patientId) {
      // --- Update Existing Patient ---
      this.patientService.updatePatient(this.patientId, patientData).subscribe({
        next: () => {
          this.isLoading = false;
          console.log('Patient updated successfully');
          this.router.navigate(['/patients']); // Navigate back to the list after update
          // Optionally show a success message
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to update patient. Please try again.';
          console.error('Error updating patient:', err);
        }
      });
    } else {
      // --- Create New Patient ---
      this.patientService.createPatient(patientData).subscribe({
        next: () => {
          this.isLoading = false;
          console.log('Patient created successfully');
          this.router.navigate(['/patients']); // Navigate back to the list after creation
          // Optionally show a success message
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to create patient. Please try again.';
          console.error('Error creating patient:', err);
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/patients']); // Navigate back to the patient list
  }
}
