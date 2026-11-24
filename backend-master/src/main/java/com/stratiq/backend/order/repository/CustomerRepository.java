package com.stratiq.backend.order.repository;

import com.stratiq.backend.order.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    // Exact name match lookup (case-sensitive) for customer deduplication and reference.
    Optional<Customer> findByName(String name);
}
