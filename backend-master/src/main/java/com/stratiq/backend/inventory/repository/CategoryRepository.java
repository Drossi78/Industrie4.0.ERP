package com.stratiq.backend.inventory.repository;

import com.stratiq.backend.inventory.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    // Lookup category by exact name (used for deduplication and quick reference).
    Optional<Category> findByName(String name);
}
