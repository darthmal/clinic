import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InvoiceService } from '../../../services/core/invoice.service';
import { InvoiceDTO } from '../../../models/invoice.dto';
// Import PatientService if needed for patient selection dropdown
// import { PatientService } from '../../../services/core/patient.service';
// import { PatientDTO } from '../../../models/patient.dto';
// import { Observable } from 'rxjs';

@Component({
  selector: 'app-invoice-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './invoice-form.component.html',
  styleUrls: ['./invoice-form.component.css']
})
export class InvoiceFormComponent implements OnInit {
  invoiceForm!: FormGroup;
  isEditMode = false;
  invoiceId: number | null = null;
  isLoading = false;
  errorMessage: string | null = null;
  pageTitle = 'Create New Invoice';
  invoiceStatuses: InvoiceDTO['status'][] = ['DRAFT', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED']; // For status dropdown

  // Placeholder for patient list if using a dropdown
  // patients$: Observable<PatientDTO[]> | undefined;

  constructor(
    private fb: FormBuilder,
    private invoiceService: InvoiceService,
    private router: Router,
    private route: ActivatedRoute
    // private patientService: PatientService // Inject if needed
  ) {}

  ngOnInit(): void {
    this.invoiceForm = this.fb.group({
      patientId: ['', Validators.required], // TODO: Replace with dropdown/search
      appointmentId: [null], // Optional link to appointment
      issueDate: [this.getTodayDateString(), Validators.required],
      dueDate: ['', Validators.required], // Consider logic for default due date
      totalAmount: [0, [Validators.required, Validators.min(0.01)]],
      status: ['DRAFT', Validators.required], // Default status
      notes: [''] // Optional
    });

    // Load patients if using a dropdown
    // this.patients$ = this.patientService.getPatients();

    // Check for edit mode
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.invoiceId = +id;
        this.pageTitle = 'Edit Invoice';
        this.loadInvoiceData(this.invoiceId);
      }
    });
  }

  loadInvoiceData(id: number): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.invoiceService.getInvoiceById(id).subscribe({
      next: (invoice) => {
        this.isLoading = false;
        // Patch form, ensuring dates are in 'YYYY-MM-DD' format for input type="date"
        invoice.issueDate = this.formatDateForInput(invoice.issueDate);
        invoice.dueDate = this.formatDateForInput(invoice.dueDate);
        this.invoiceForm.patchValue(invoice);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to load invoice data.';
        console.error('Error loading invoice:', err);
      }
    });
  }

  onSubmit(): void {
    if (this.invoiceForm.invalid) {
      this.errorMessage = 'Please fill in all required fields correctly.';
      this.invoiceForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    const invoiceData: InvoiceDTO = this.invoiceForm.value;

    // Ensure dates are correctly formatted if necessary before sending
    // (Backend should handle ISO strings, usually no conversion needed here if form uses 'YYYY-MM-DD')

    if (this.isEditMode && this.invoiceId) {
      // --- Update Invoice ---
      this.invoiceService.updateInvoice(this.invoiceId, invoiceData).subscribe({
        next: () => {
          this.isLoading = false;
          console.log('Invoice updated successfully');
          this.router.navigate(['/invoices']); // Navigate back to list
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to update invoice.';
          console.error('Error updating invoice:', err);
        }
      });
    } else {
      // --- Create Invoice ---
      this.invoiceService.createInvoice(invoiceData).subscribe({
        next: () => {
          this.isLoading = false;
          console.log('Invoice created successfully');
          this.router.navigate(['/invoices']); // Navigate back to list
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to create invoice.';
          console.error('Error creating invoice:', err);
        }
      });
    }
  }

  getTodayDateString(): string {
    return new Date().toISOString().split('T')[0];
  }

  formatDateForInput(dateStr: string | undefined): string {
    if (!dateStr) return '';
    try {
      // Assuming dateStr is an ISO string or similar from backend
      return new Date(dateStr).toISOString().split('T')[0];
    } catch (e) {
      console.error("Error formatting date for input:", e);
      return ''; // Return empty or handle error
    }
  }

  goBack(): void {
    this.router.navigate(['/invoices']);
  }
}
