import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router'; // Import RouterLink
import { Observable } from 'rxjs';
import { PatientDTO } from '../../../models/patient.dto';
import { PatientService } from '../../../services/core/patient.service';

@Component({
  selector: 'app-patient-list',
  standalone: true,
  imports: [CommonModule, RouterLink], // Add RouterLink
  templateUrl: './patient-list.component.html',
  styleUrls: ['./patient-list.component.css']
})
export class PatientListComponent implements OnInit {

  patients$: Observable<PatientDTO[]> | undefined;
  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private patientService: PatientService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPatients();
  }

  loadPatients(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.patients$ = this.patientService.getPatients();
    // Note: In a real app, handle loading/error states more robustly within the template or service
    this.patients$.subscribe({
       next: () => this.isLoading = false,
       error: (err) => {
         this.isLoading = false;
         this.errorMessage = 'Failed to load patients. Please try again.';
         console.error(err);
       }
    });
  }

  editPatient(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/patients/edit', id]); // Navigate to edit form
    }
  }

  deletePatient(id: number | undefined): void {
    if (id && confirm('Are you sure you want to delete this patient? This action cannot be undone.')) {
      this.isLoading = true; // Show loading indicator during delete
      this.patientService.deletePatient(id).subscribe({
        next: () => {
          console.log(`Patient ${id} deleted successfully.`);
          this.loadPatients(); // Refresh the list after deletion
          // Optionally show a success message
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to delete patient. Please try again.';
          console.error('Error deleting patient:', err);
          // Optionally show a more specific error message
        }
      });
    }
  }
}
