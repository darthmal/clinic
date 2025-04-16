import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { InvoiceDTO } from '../../models/invoice.dto'; // Import the DTO
import { AuthService } from '../auth/auth.service'; // To get auth headers

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private apiUrl = `${environment.apiUrl}/invoices`; // Base URL for invoice endpoints
  private docApiUrl = `${environment.apiUrl}/documents`; // Base URL for document endpoints

  constructor(
    private http: HttpClient,
    private authService: AuthService // Inject AuthService
  ) {}

  // Get invoices with optional filtering
  getInvoices(patientId?: number, status?: InvoiceDTO['status']): Observable<InvoiceDTO[]> {
    let params = new HttpParams();
    if (patientId) {
      params = params.set('patientId', patientId.toString());
    }
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<InvoiceDTO[]>(this.apiUrl, { headers: this.authService.getAuthHeaders(), params });
  }

  // Get a single invoice by ID
  getInvoiceById(id: number): Observable<InvoiceDTO> {
    return this.http.get<InvoiceDTO>(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }

  // Create a new invoice
  createInvoice(invoice: InvoiceDTO): Observable<InvoiceDTO> {
    // Remove read-only fields before sending
    const { id, patientFirstName, patientLastName, createdAt, updatedAt, ...createData } = invoice;
    return this.http.post<InvoiceDTO>(this.apiUrl, createData, { headers: this.authService.getAuthHeaders() });
  }

  // Update an existing invoice
  updateInvoice(id: number, invoice: InvoiceDTO): Observable<InvoiceDTO> {
     // Remove read-only fields before sending
     const { patientFirstName, patientLastName, createdAt, updatedAt, ...updateData } = invoice;
    return this.http.put<InvoiceDTO>(`${this.apiUrl}/${id}`, updateData, { headers: this.authService.getAuthHeaders() });
  }

  // Delete an invoice (use with caution)
  deleteInvoice(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }

  // Download Invoice PDF
  downloadInvoicePdf(id: number): Observable<Blob> {
    // Request the PDF as a Blob
    return this.http.get(`${this.docApiUrl}/invoices/${id}/pdf`, {
      headers: this.authService.getAuthHeaders(),
      responseType: 'blob' // Important: response type is blob for files
    });
  }

  // Mark an invoice as paid (example custom action)
  markAsPaid(id: number): Observable<InvoiceDTO> {
     // Assuming backend has an endpoint like POST /api/v1/invoices/{id}/pay
     return this.http.post<InvoiceDTO>(`${this.apiUrl}/${id}/pay`, {}, { headers: this.authService.getAuthHeaders() });
  }

   // Mark an invoice as sent (example custom action)
   markAsSent(id: number): Observable<InvoiceDTO> {
      // Assuming backend has an endpoint like POST /api/v1/invoices/{id}/send
      return this.http.post<InvoiceDTO>(`${this.apiUrl}/${id}/send`, {}, { headers: this.authService.getAuthHeaders() });
   }
}
