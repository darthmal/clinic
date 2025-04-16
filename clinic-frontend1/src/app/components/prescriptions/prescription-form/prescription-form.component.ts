import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router'; // Import ActivatedRoute
import { PrescriptionService } from '../../../services/core/prescription.service';
import { AuthService } from '../../../services/auth/auth.service'; // To get current doctor ID
import { PrescriptionDTO } from '../../../models/prescription.dto';
// Import PatientService if needed to fetch patient list for dropdown
// import { PatientService } from '../../../services/core/patient.service';
// import { PatientDTO } from '../../../models/patient.dto';
// import { Observable } from 'rxjs';

@Component({
  selector: 'app-prescription-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './prescription-form.component.html',
  styleUrls: ['./prescription-form.component.css']
})
export class PrescriptionFormComponent implements OnInit {
  prescriptionForm!: FormGroup;
  isLoading = false;
  errorMessage: string | null = null;
  pageTitle = 'Create New Prescription';
  currentDoctorId: number | null = null;

  // If creating from a patient's page, patientId might be passed via route
  patientIdFromRoute: number | null = null;

  // Placeholder for patient list if using a dropdown
  // patients$: Observable<PatientDTO[]> | undefined;

  constructor(
    private fb: FormBuilder,
    private prescriptionService: PrescriptionService,
    private authService: AuthService, // Inject AuthService
    private router: Router,
    private route: ActivatedRoute // Inject ActivatedRoute
    // private patientService: PatientService // Inject if needed
  ) {}

  ngOnInit(): void {
    // Get current doctor's ID from AuthService using the new getter
    const currentUser = this.authService.getCurrentUserValue();
    if (currentUser && currentUser.role === 'DOCTOR') {
      this.currentDoctorId = currentUser.id;
    } else {
      // Handle case where user is not a doctor or not logged in (though guards should prevent this)
      this.errorMessage = "Error: Cannot determine prescribing doctor.";
      console.error("User is not a doctor or doctor ID not found.");
      // Potentially disable the form or redirect
    }

    // Check for patientId from route parameters (e.g., if navigating from patient detail)
    this.route.paramMap.subscribe(params => {
      const id = params.get('patientId'); // Assuming route is like '/patients/:patientId/prescriptions/new'
      if (id) {
        this.patientIdFromRoute = +id;
      }
      this.initializeForm(); // Initialize form after getting potential patientId
    });

    // Load patients if using a dropdown
    // this.patients$ = this.patientService.getPatients();
  }

  initializeForm(): void {
     this.prescriptionForm = this.fb.group({
       // If patientId comes from route, pre-fill and make read-only/hidden
       // Otherwise, this should be a dropdown/search input
       patientId: [this.patientIdFromRoute, Validators.required],
       doctorId: [this.currentDoctorId, Validators.required], // Pre-fill doctor ID
       medicationName: ['', Validators.required],
       dosage: ['', Validators.required],
       frequency: ['', Validators.required],
       instructions: [''], // Optional
       prescriptionDate: [this.getTodayDateString(), Validators.required], // Default to today
       endDate: [''] // Optional
     });

     // Disable doctorId field as it's pre-filled
     if (this.currentDoctorId) {
       this.prescriptionForm.get('doctorId')?.disable();
     }
     // Disable patientId if it came from the route
     if (this.patientIdFromRoute) {
        this.prescriptionForm.get('patientId')?.disable();
     }
  }

  getTodayDateString(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = ('0' + (today.getMonth() + 1)).slice(-2); // Add leading zero
    const day = ('0' + today.getDate()).slice(-2); // Add leading zero
    return `${year}-${month}-${day}`;
  }


  onSubmit(): void {
    // Re-enable disabled fields to include them in the value
    this.prescriptionForm.get('doctorId')?.enable();
    if (this.patientIdFromRoute) {
        this.prescriptionForm.get('patientId')?.enable();
    }


    if (this.prescriptionForm.invalid) {
      this.errorMessage = 'Please fill in all required fields correctly.';
      this.prescriptionForm.markAllAsTouched();
       // Disable fields again after checking validity if needed
       if (this.currentDoctorId) this.prescriptionForm.get('doctorId')?.disable();
       if (this.patientIdFromRoute) this.prescriptionForm.get('patientId')?.disable();
      return;
    }

    // Disable fields again before submitting if they were disabled initially
    if (this.currentDoctorId) this.prescriptionForm.get('doctorId')?.disable();
    if (this.patientIdFromRoute) this.prescriptionForm.get('patientId')?.disable();


    this.isLoading = true;
    this.errorMessage = null;

    // Get raw value including disabled controls
    const prescriptionData: PrescriptionDTO = this.prescriptionForm.getRawValue();

    this.prescriptionService.createPrescription(prescriptionData).subscribe({
      next: (newPrescription) => {
        this.isLoading = false;
        console.log('Prescription created successfully', newPrescription);
        // Navigate back or to prescription list/patient detail
        // Consider where to navigate: patient detail, prescription list?
        this.router.navigate(['/prescriptions']); // Example: navigate to general list
        // Optionally show a success message
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to create prescription. Please try again.';
        console.error('Error creating prescription:', err);
         // Re-enable fields after error if needed for correction
         this.prescriptionForm.get('doctorId')?.enable();
         if (this.patientIdFromRoute) this.prescriptionForm.get('patientId')?.enable();
      }
    });
  }

  goBack(): void {
    // Navigate back intelligently, perhaps to patient detail if patientId exists
    if (this.patientIdFromRoute) {
       this.router.navigate(['/patients', this.patientIdFromRoute]); // Example back to patient detail
    } else {
       this.router.navigate(['/prescriptions']); // Or back to general list
    }
  }
}
