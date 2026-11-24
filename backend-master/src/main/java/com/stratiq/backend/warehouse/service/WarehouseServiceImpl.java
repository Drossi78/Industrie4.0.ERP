package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.warehouse.dto.request.WarehouseCreateRequest;
import com.stratiq.backend.warehouse.dto.request.WarehouseUpdateRequest;
import com.stratiq.backend.warehouse.dto.response.WarehouseResponse;
import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.warehouse.mapper.WarehouseMapper;
import com.stratiq.backend.warehouse.model.Warehouse;
import com.stratiq.backend.warehouse.repository.WarehouseRepository;
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
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repo;
    private final WarehouseMapper mapper;

    @Override
    public WarehouseResponse create(WarehouseCreateRequest req) {
        return mapper.toResponse(repo.save(mapper.toEntity(req)));
    }

    @Override
    public WarehouseResponse update(UUID id, WarehouseUpdateRequest req) {
        Warehouse w = find(id);
        mapper.update(w, req);
        return mapper.toResponse(w);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse get(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseResponse> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> findByCity(String city) {
        return repo.findByCity(city).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> findActive() {
        // Returns only warehouses where isActive=true (operational warehouses).
        return repo.findByIsActiveTrue().stream().map(mapper::toResponse).toList();
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id)) throw new NotFoundException("Warehouse", id);
        repo.deleteById(id);
    }

    private Warehouse find(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Warehouse", id));
    }
}
