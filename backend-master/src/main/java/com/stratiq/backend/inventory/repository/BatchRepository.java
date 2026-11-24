package com.stratiq.backend.inventory.repository;

import com.stratiq.backend.inventory.model.Batch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BatchRepository extends JpaRepository<Batch, UUID> {
    // Get all batches for a specific item across all warehouses.
    List<Batch> findByItemId(UUID itemId);
    // Get all batches stored in a specific warehouse (inventory view per location).
    List<Batch> findByWarehouseId(UUID warehouseId);
    // Paginated query for item batches (supports sorting and pagination for UI lists).
    Page<Batch> findByItemId(UUID itemId, Pageable pageable);
    // Resolve an existing lot by its number within an item+warehouse (used by goods receipt
    // to append to an existing batch instead of creating a duplicate).
    Optional<Batch> findByBatchNumberAndItemIdAndWarehouseId(String batchNumber, UUID itemId, UUID warehouseId);
}
