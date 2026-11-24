package com.stratiq.backend.order.repository;

import com.stratiq.backend.order.model.Invoice;
import com.stratiq.backend.order.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    // Get all invoices for a specific order.
    List<Invoice> findByOrderId(UUID orderId);
    // Filter invoices by status (ISSUED, PAID, CANCELLED).
    List<Invoice> findByStatus(InvoiceStatus status);
    // Lookup by unique invoice number (case-sensitive, typically "INV-XXXX" format).
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    // Highest sequence number already used for "INV-<year>-####" (null if none yet).
    // Numeric CAST avoids the lexicographic-sort bug of comparing zero-padded strings once counts hit 5 digits.
    @Query(value = """
            SELECT MAX(CAST(SUBSTRING(invoice_number FROM 10) AS INTEGER))
            FROM invoices
            WHERE invoice_number LIKE CONCAT('INV-', CAST(:year AS varchar), '-%')
            """, nativeQuery = true)
    Integer findMaxSequenceForYear(@Param("year") int year);
}
