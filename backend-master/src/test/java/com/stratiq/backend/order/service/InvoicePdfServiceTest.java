package com.stratiq.backend.order.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicePdfServiceTest {

    private final InvoicePdfService service = new InvoicePdfServiceImpl();

    @Test
    void export_returnsEmptyByteArray() {
        byte[] result = service.export(UUID.randomUUID());

        assertThat(result).isNotNull().isEmpty();
    }
}
