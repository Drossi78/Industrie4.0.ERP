package com.stratiq.backend.warehouse.repository;

import com.stratiq.backend.warehouse.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    // Find all warehouses located in a specific city (location filtering).
    List<Warehouse> findByCity(String city);
    // Get only active warehouses (operational facilities; inactive ones excluded).
    List<Warehouse> findByIsActiveTrue();
    // Soft-delete filter: returns only non-deleted warehouses (deletedAt is null).
    List<Warehouse> findByDeletedAtIsNull();
    // Case-insensitive name search (partial substring match for warehouse lookup).
    List<Warehouse> findByNameContainingIgnoreCase(String name);
}
