package com.stratiq.backend.inventory.mapper;

import com.stratiq.backend.inventory.dto.request.SupplierCreateRequest;
import com.stratiq.backend.inventory.dto.request.SupplierUpdateRequest;
import com.stratiq.backend.inventory.dto.response.SupplierResponse;
import com.stratiq.backend.inventory.model.Supplier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SupplierMapperTest {

    private final SupplierMapper mapper = new SupplierMapper();

    @Test
    void toEntity_mapsAllFields() {
        SupplierCreateRequest req = new SupplierCreateRequest(
                "Acme", "acme@example.com", "123", "TAX01",
                "Main St", "1A", "10001", "Berlin", "BE", "DE", "Good supplier"
        );

        Supplier result = mapper.toEntity(req);

        assertThat(result.getName()).isEqualTo("Acme");
        assertThat(result.getEmail()).isEqualTo("acme@example.com");
        assertThat(result.getPhone()).isEqualTo("123");
        assertThat(result.getTaxId()).isEqualTo("TAX01");
        assertThat(result.getCity()).isEqualTo("Berlin");
        assertThat(result.getCountry()).isEqualTo("DE");
        assertThat(result.getNotes()).isEqualTo("Good supplier");
    }

    @Test
    void update_withNonNullFields_overwritesExisting() {
        Supplier supplier = new Supplier();
        supplier.setName("Old Name");
        supplier.setCity("Hamburg");
        SupplierUpdateRequest req = new SupplierUpdateRequest(
                "New Name", null, null, null, null, null, null, "Munich", null, null, null
        );

        mapper.update(supplier, req);

        assertThat(supplier.getName()).isEqualTo("New Name");
        assertThat(supplier.getCity()).isEqualTo("Munich");
    }

    @Test
    void update_withNullFields_keepsExistingValues() {
        Supplier supplier = new Supplier();
        supplier.setName("Keep Me");
        supplier.setEmail("keep@example.com");
        SupplierUpdateRequest req = new SupplierUpdateRequest(
                null, null, null, null, null, null, null, null, null, null, null
        );

        mapper.update(supplier, req);

        assertThat(supplier.getName()).isEqualTo("Keep Me");
        assertThat(supplier.getEmail()).isEqualTo("keep@example.com");
    }

    @Test
    void toResponse_mapsAllFields() {
        Supplier s = new Supplier();
        s.setId(UUID.randomUUID());
        s.setName("Acme");
        s.setEmail("acme@example.com");

        SupplierResponse response = mapper.toResponse(s);

        assertThat(response.id()).isEqualTo(s.getId());
        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.email()).isEqualTo("acme@example.com");
    }
}
