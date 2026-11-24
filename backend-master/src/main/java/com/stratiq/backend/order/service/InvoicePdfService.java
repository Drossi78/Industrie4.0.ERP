package com.stratiq.backend.order.service;

import java.util.UUID;

public interface InvoicePdfService {
    byte[] export(UUID invoiceId);
}
