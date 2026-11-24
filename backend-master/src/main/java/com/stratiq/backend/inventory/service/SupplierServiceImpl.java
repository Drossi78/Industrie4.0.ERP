package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.SupplierCreateRequest;
import com.stratiq.backend.inventory.dto.request.SupplierUpdateRequest;
import com.stratiq.backend.inventory.dto.response.SupplierResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.inventory.mapper.SupplierMapper;
import com.stratiq.backend.inventory.model.Supplier;
import com.stratiq.backend.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repo;
    private final SupplierMapper mapper;

    @Override
    public SupplierResponse create(SupplierCreateRequest req) {
        return mapper.toResponse(repo.save(mapper.toEntity(req)));
    }

    @Override
    public SupplierResponse update(UUID id, SupplierUpdateRequest req) {
        Supplier s = find(id);
        mapper.update(s, req);
        return mapper.toResponse(s);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> search(String name) {
        // Case-insensitive substring search for supplier names.
        return repo.findByNameContainingIgnoreCase(name).stream().map(mapper::toResponse).toList();
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("Supplier", id);
        repo.deleteById(id);
    }

    private Supplier find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Supplier", id));
    }
}
