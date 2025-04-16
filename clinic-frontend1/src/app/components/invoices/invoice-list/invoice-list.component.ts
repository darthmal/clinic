import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { InvoiceDTO } from '../../../models/invoice.dto';
import { InvoiceService } from '../../../services/core/invoice.service';
import { saveAs } from 'file-saver'; // For PDF download

@Component({
  selector: 'app-invoice-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './invoice-list.component.html',
  styleUrls: ['./invoice-list.component.css']
})
export class InvoiceListComponent implements OnInit {

  // Optional inputs for filtering
  @Input() patientId: number | undefined;
  @Input() status: InvoiceDTO['status'] | undefined; // e.g., 'OVERDUE', 'SENT'

  invoices$: Observable<InvoiceDTO[]> | undefined;
  isLoading = false;
  errorMessage: string | null = null;

  constructor(
    private invoiceService: InvoiceService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadInvoices();
  }

  loadInvoices(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.invoices$ = this.invoiceService.getInvoices(this.patientId, this.status);
    this.invoices$.subscribe({
       next: () => this.isLoading = false,
       error: (err) => {
         this.isLoading = false;
         this.errorMessage = 'Failed to load invoices. Please try again.';
         console.error(err);
       }
    });
  }

  // Navigate to edit form (if editing is allowed)
  editInvoice(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/invoices/edit', id]); // Adjust route as needed
    }
  }

  // Mark invoice as paid
  markAsPaid(id: number | undefined): void {
     if (!id) return;
     this.isLoading = true; // Indicate loading during action
     this.invoiceService.markAsPaid(id).subscribe({
       next: () => {
         console.log(`Invoice ${id} marked as paid.`);
         this.loadInvoices(); // Refresh the list
       },
       error: (err) => {
         this.isLoading = false;
         this.errorMessage = 'Failed to mark invoice as paid.';
         console.error('Error marking invoice as paid:', err);
       }
     });
  }

   // Mark invoice as sent
   markAsSent(id: number | undefined): void {
      if (!id) return;
      this.isLoading = true;
      this.invoiceService.markAsSent(id).subscribe({
        next: () => {
          console.log(`Invoice ${id} marked as sent.`);
          this.loadInvoices(); // Refresh the list
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to mark invoice as sent.';
          console.error('Error marking invoice as sent:', err);
        }
      });
   }

  // Download PDF
  downloadPdf(id: number | undefined, patientLastName: string | undefined): void {
    if (!id) return;
    this.invoiceService.downloadInvoicePdf(id).subscribe({
      next: (blob) => {
        const filename = `invoice_${id}_${patientLastName || 'patient'}.pdf`;
        saveAs(blob, filename);
      },
      error: (err) => {
        this.errorMessage = 'Failed to download PDF.';
        console.error('Error downloading invoice PDF:', err);
      }
    });
  }

  // Delete invoice (use with caution)
  deleteInvoice(id: number | undefined): void {
    if (id && confirm('Are you sure you want to delete this invoice?')) {
      this.isLoading = true;
      this.invoiceService.deleteInvoice(id).subscribe({
        next: () => {
          console.log(`Invoice ${id} deleted successfully.`);
          this.loadInvoices(); // Refresh list
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to delete invoice.';
          console.error('Error deleting invoice:', err);
        }
      });
    }
  }
}
