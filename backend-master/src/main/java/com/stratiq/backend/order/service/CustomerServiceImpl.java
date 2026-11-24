package com.stratiq.backend.order.service;

import com.stratiq.backend.order.dto.request.CustomerCreateRequest;
import com.stratiq.backend.order.dto.request.CustomerUpdateRequest;
import com.stratiq.backend.order.dto.response.CustomerResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.order.mapper.CustomerMapper;
import com.stratiq.backend.order.model.Customer;
import com.stratiq.backend.order.repository.CustomerRepository;
import com.stratiq.backend.order.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;
    private final CustomerMapper mapper;

    @Override
    public CustomerResponse create(CustomerCreateRequest req) {
        return mapper.toResponse(repo.save(mapper.toEntity(req)));
    }

    @Override
    public CustomerResponse update(UUID id, CustomerUpdateRequest req) {
        Customer c = find(id);
        mapper.update(c, req);
        return mapper.toResponse(c);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerResponse> findByName(String name) {
        // Exact name match (case-sensitive) for customer lookup.
        return repo.findByName(name).map(mapper::toResponse);
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("Customer", id);
        repo.deleteById(id);
    }

    private Customer find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Customer", id));
    }
}
