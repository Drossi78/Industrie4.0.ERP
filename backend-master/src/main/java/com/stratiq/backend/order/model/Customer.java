package com.stratiq.backend.order.model;

import com.stratiq.backend.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "kunde")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends BaseEntity {

    @Column(length = 255)
    private String name;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    // VAT/Tax ID for business customers. Used for invoice generation.
    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(length = 255)
    private String street;

    @Column(name = "house_number", length = 20)
    private String houseNumber;

    @Column(name = "zip_code", length = 20)
    private String zipCode;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Soft-delete: deletedAt null = active, non-null = deleted (not removed from DB).
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
