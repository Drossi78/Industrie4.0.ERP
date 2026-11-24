package com.stratiq.backend.inventory.repository;

import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.model.ItemKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    // Soft-delete query: returns only active items (is_deleted=false).
    List<Item> findByIsDeletedFalse();
    // Filter by category. Note: doesn't exclude deleted items (caller must check if needed).
    List<Item> findByCategoryId(UUID categoryId);
    // Filter by item kind (OWN_PRODUCT, PURCHASED).
    List<Item> findByKind(ItemKind kind);
    // Single item lookup with soft-delete filter.
    Optional<Item> findByIdAndIsDeletedFalse(UUID id);
}
