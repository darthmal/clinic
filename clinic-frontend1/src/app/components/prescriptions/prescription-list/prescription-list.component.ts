import { Component, OnInit, Input } from '@angular/core'; // Add Input
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { PrescriptionDTO } from '../../../models/prescription.dto';
import { PrescriptionService } from '../../../services/core/prescription.service';
import { saveAs } from 'file-saver'; // Import file-saver for downloads

@Component({
  selector: 'app-prescription-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './prescription-list.component.html',
  styleUrls: ['./prescription-list.component.css']
})
export class PrescriptionListComponent implements OnInit {

  // Optional inputs to filter prescriptions shown
  @Input() patientId: number | undefined;
  @Input() doctorId: number | undefined;

  prescriptions$: Observable<PrescriptionDTO[]> | undefined;
  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private prescriptionService: PrescriptionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPrescriptions();
  }

  loadPrescriptions(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.prescriptions$ = this.prescriptionService.getPrescriptions(this.patientId, this.doctorId);
    this.prescriptions$.subscribe({
       next: () => this.isLoading = false,
       error: (err) => {
         this.isLoading = false;
         this.errorMessage = 'Failed to load prescriptions. Please try again.';
         console.error(err);
       }
    });
  }

  // Navigate to edit form (if editing is allowed)
  editPrescription(id: number | undefined): void {
    if (id) {
      // Adjust route if needed, e.g., '/prescriptions/edit/:id'
      this.router.navigate(['/prescriptions/edit', id]);
    }
  }

  // Delete prescription (use with caution)
  deletePrescription(id: number | undefined): void {
    if (id && confirm('Are you sure you want to delete this prescription?')) {
      this.isLoading = true;
      this.prescriptionService.deletePrescription(id).subscribe({
        next: () => {
          console.log(`Prescription ${id} deleted successfully.`);
          this.loadPrescriptions(); // Refresh list
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to delete prescription.';
          console.error('Error deleting prescription:', err);
        }
      });
    }
  }

  // Download PDF
  downloadPdf(id: number | undefined, patientLastName: string | undefined): void {
    if (!id) return;
    this.prescriptionService.downloadPrescriptionPdf(id).subscribe({
      next: (blob) => {
        // Use file-saver to trigger download
        const filename = `prescription_${id}_${patientLastName || 'patient'}.pdf`;
        saveAs(blob, filename);
      },
      error: (err) => {
        this.errorMessage = 'Failed to download PDF.';
        console.error('Error downloading prescription PDF:', err);
      }
    });
  }
}
