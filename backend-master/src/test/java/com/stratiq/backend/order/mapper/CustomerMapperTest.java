package com.stratiq.backend.order.mapper;

import com.stratiq.backend.order.dto.request.CustomerCreateRequest;
import com.stratiq.backend.order.dto.request.CustomerUpdateRequest;
import com.stratiq.backend.order.dto.response.CustomerResponse;
import com.stratiq.backend.order.model.Customer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private final CustomerMapper mapper = new CustomerMapper();

    @Test
    void toEntity_mapsAllFields() {
        CustomerCreateRequest req = new CustomerCreateRequest(
                "John Doe", "john@example.com", "555-1234", "VAT123",
                "Oak Street", "5B", "20099", "Hamburg", "HH", "DE", "VIP"
        );

        Customer result = mapper.toEntity(req);

        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPhone()).isEqualTo("555-1234");
        assertThat(result.getTaxId()).isEqualTo("VAT123");
        assertThat(result.getCity()).isEqualTo("Hamburg");
        assertThat(result.getCountry()).isEqualTo("DE");
        assertThat(result.getNotes()).isEqualTo("VIP");
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void update_withNonNullFields_overwritesExisting() {
        Customer customer = new Customer();
        customer.setName("Old Name");
        customer.setCity("Berlin");
        CustomerUpdateRequest req = new CustomerUpdateRequest(
                "New Name", null, null, null, null, null, null, "Munich", null, null, null
        );

        mapper.update(customer, req);

        assertThat(customer.getName()).isEqualTo("New Name");
        assertThat(customer.getCity()).isEqualTo("Munich");
        assertThat(customer.getUpdatedAt()).isNotNull();
    }

    @Test
    void update_withNullFields_keepsExistingValues() {
        Customer customer = new Customer();
        customer.setName("Keep Me");
        customer.setEmail("keep@example.com");
        CustomerUpdateRequest req = new CustomerUpdateRequest(
                null, null, null, null, null, null, null, null, null, null, null
        );

        mapper.update(customer, req);

        assertThat(customer.getName()).isEqualTo("Keep Me");
        assertThat(customer.getEmail()).isEqualTo("keep@example.com");
    }

    @Test
    void toResponse_mapsAllFields() {
        Customer c = new Customer();
        c.setId(UUID.randomUUID());
        c.setName("John Doe");
        c.setEmail("john@example.com");

        CustomerResponse response = mapper.toResponse(c);

        assertThat(response.id()).isEqualTo(c.getId());
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john@example.com");
    }
}
