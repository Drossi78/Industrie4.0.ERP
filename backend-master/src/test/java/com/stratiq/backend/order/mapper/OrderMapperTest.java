package com.stratiq.backend.order.mapper;

import com.stratiq.backend.order.dto.request.OrderCreateRequest;
import com.stratiq.backend.order.dto.request.OrderUpdateRequest;
import com.stratiq.backend.order.dto.response.OrderResponse;
import com.stratiq.backend.order.model.Customer;
import com.stratiq.backend.order.model.Order;
import com.stratiq.backend.order.model.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    void toEntity_withExplicitStatus_usesProvidedStatus() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        OrderCreateRequest req = new OrderCreateRequest(
                customer.getId(), "ORD-001", OrderStatus.CONFIRMED, null,
                LocalDateTime.now(), null, new BigDecimal("500"), "Rush"
        );

        Order result = mapper.toEntity(req, customer);

        assertThat(result.getOrderNumber()).isEqualTo("ORD-001");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("500");
        assertThat(result.getNotes()).isEqualTo("Rush");
    }

    @Test
    void toEntity_withNullStatus_defaultsToDraft() {
        Customer customer = new Customer();
        OrderCreateRequest req = new OrderCreateRequest(
                null, "ORD-002", null, null, null, null, null, null
        );

        Order result = mapper.toEntity(req, customer);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.DRAFT);
    }

    @Test
    void toEntity_withNullAmountAndDate_defaultsToZeroAndNow() {
        Customer customer = new Customer();
        OrderCreateRequest req = new OrderCreateRequest(
                null, "ORD-003", null, null, null, null, null, null
        );

        Order result = mapper.toEntity(req, customer);

        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getOrderDate()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void update_withNonNullFields_overwritesExisting() {
        Customer newCustomer = new Customer();
        newCustomer.setId(UUID.randomUUID());
        Order order = new Order();
        order.setStatus(OrderStatus.DRAFT);
        order.setNotes("Old notes");
        OrderUpdateRequest req = new OrderUpdateRequest(
                newCustomer.getId(), "ORD-NEW", OrderStatus.CONFIRMED, null, null, "New notes"
        );

        mapper.update(order, req, newCustomer);

        assertThat(order.getCustomer()).isEqualTo(newCustomer);
        assertThat(order.getOrderNumber()).isEqualTo("ORD-NEW");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getNotes()).isEqualTo("New notes");
    }

    @Test
    void update_withNullFields_keepsExistingValues() {
        Order order = new Order();
        order.setStatus(OrderStatus.DRAFT);
        order.setNotes("Keep me");
        OrderUpdateRequest req = new OrderUpdateRequest(
                null, null, null, null, null, null
        );

        mapper.update(order, req, null);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(order.getNotes()).isEqualTo("Keep me");
    }

    @Test
    void toResponse_withCustomer_includesCustomerId() {
        Customer customer = new Customer();
        UUID customerId = UUID.randomUUID();
        customer.setId(customerId);

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.DRAFT);
        order.setTotalAmount(BigDecimal.ZERO);

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.status()).isEqualTo(OrderStatus.DRAFT);
    }

    @Test
    void toResponse_withoutCustomer_customerIdIsNull() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus(OrderStatus.DRAFT);
        order.setTotalAmount(BigDecimal.ZERO);

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.customerId()).isNull();
    }
}
