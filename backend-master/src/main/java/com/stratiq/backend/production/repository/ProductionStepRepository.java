package com.stratiq.backend.production.repository;

import com.stratiq.backend.production.model.ProductionStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductionStepRepository extends JpaRepository<ProductionStep, UUID> {
    // Get active production steps for an item, ordered by sequence (respects manufacturing workflow).
    List<ProductionStep> findByItemIdAndIsDeletedFalseOrderByStepOrderAsc(UUID itemId);
    // Get all production steps (including deleted) for an item.
    List<ProductionStep> findByItemId(UUID itemId);
}
