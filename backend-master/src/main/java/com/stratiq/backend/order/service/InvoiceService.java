package com.stratiq.backend.order.service;

import com.stratiq.backend.order.dto.request.InvoiceCreateRequest;
import com.stratiq.backend.order.dto.request.InvoiceUpdateRequest;
import com.stratiq.backend.order.dto.response.InvoiceResponse;
import com.stratiq.backend.order.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {
    InvoiceResponse create(InvoiceCreateRequest req);
    InvoiceResponse update(UUID id, InvoiceUpdateRequest req);
    InvoiceResponse get(UUID id);
    Page<InvoiceResponse> list(Pageable pageable);
    List<InvoiceResponse> findByOrder(UUID orderId);
    List<InvoiceResponse> findByStatus(InvoiceStatus status);
    void delete(UUID id);
}
