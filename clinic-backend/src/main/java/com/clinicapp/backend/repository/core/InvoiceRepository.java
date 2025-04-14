package com.clinicapp.backend.repository.core;

import com.clinicapp.backend.model.core.Invoice;
import com.clinicapp.backend.model.core.InvoiceStatus;
import com.clinicapp.backend.model.core.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import Query
import org.springframework.data.repository.query.Param; // Import Param
import org.springframework.stereotype.Repository;

import java.math.BigDecimal; // Import BigDecimal if needed for sums (though not directly here)
import java.time.LocalDate;
import java.util.Collection; // Import Collection for In clause
import java.util.List;
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Find invoices for a specific patient
    List<Invoice> findByPatientOrderByIssueDateDesc(Patient patient);

    // Find invoices by status
    List<Invoice> findByStatusOrderByDueDateAsc(InvoiceStatus status);

    // Find invoices by a list of statuses
    List<Invoice> findByStatusInOrderByDueDateAsc(List<InvoiceStatus> statuses);

    // Find invoices for a patient by status
    List<Invoice> findByPatientAndStatusOrderByDueDateAsc(Patient patient, InvoiceStatus status);

    // Find overdue invoices (due date is in the past and status is not PAID or CANCELLED)
    List<Invoice> findByDueDateBeforeAndStatusNotIn(LocalDate today, List<InvoiceStatus> excludedStatuses);

    // Find invoices issued within a date range
    List<Invoice> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);

    // Find PAID invoices issued within a date range (Needed for DashboardService)
    // Using @Query for clarity on the status check
    @Query("SELECT i FROM Invoice i WHERE i.status = 'PAID' AND i.issueDate BETWEEN :startDate AND :endDate")
    List<Invoice> findPaidInvoicesIssuedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Count invoices by a list of statuses (Needed for DashboardService)
    long countByStatusIn(Collection<InvoiceStatus> statuses);
}