package com.stratiq.backend.inventory.repository;

import com.stratiq.backend.inventory.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    // Case-insensitive substring search for supplier name filtering.
    List<Supplier> findByNameContainingIgnoreCase(String name);
    // Soft-delete filter: returns only active (non-deleted) suppliers.
    List<Supplier> findByDeletedAtIsNull();
}
