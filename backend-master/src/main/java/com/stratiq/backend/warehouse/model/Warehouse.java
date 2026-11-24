package com.stratiq.backend.warehouse.model;

import com.stratiq.backend.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "lager")
@Getter
@Setter
@NoArgsConstructor
public class Warehouse extends BaseEntity {

    @Column(length = 255)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

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

    // isActive=true: warehouse is operational. false: warehouse is deactivated.
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Soft-delete: deletedAt null = active, non-null = deleted (not removed from DB).
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
