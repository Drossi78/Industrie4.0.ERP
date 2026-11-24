package com.stratiq.backend.inventory.repository;

import com.stratiq.backend.inventory.model.BillOfMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterial, UUID> {
    // Get all components that make up a specific item (BOM for manufacturing).
    List<BillOfMaterial> findByItemId(UUID itemId);
}
