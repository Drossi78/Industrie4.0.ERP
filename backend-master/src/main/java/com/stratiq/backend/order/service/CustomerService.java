package com.stratiq.backend.order.service;

import com.stratiq.backend.order.dto.request.CustomerCreateRequest;
import com.stratiq.backend.order.dto.request.CustomerUpdateRequest;
import com.stratiq.backend.order.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CustomerService {
    CustomerResponse create(CustomerCreateRequest req);
    CustomerResponse update(UUID id, CustomerUpdateRequest req);
    CustomerResponse get(UUID id);
    Page<CustomerResponse> list(Pageable pageable);
    Optional<CustomerResponse> findByName(String name);
    void delete(UUID id);
}
