package com.stratiq.backend.production.controller;

import com.stratiq.backend.production.dto.request.ProductionStepCreateRequest;
import com.stratiq.backend.production.dto.request.ProductionStepUpdateRequest;
import com.stratiq.backend.production.dto.response.ProductionStepResponse;
import com.stratiq.backend.production.service.ProductionStepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.ASC;

@RestController
@RequestMapping("/api/v1/production-steps")
@RequiredArgsConstructor
@Tag(name = "Production Steps (Templates)")
public class ProductionStepController {

    private final ProductionStepService service;

    @GetMapping
    @PreAuthorize("hasAuthority('production:read')")
    @Operation(summary = "List production steps — filter by itemId (optional)")
    public Object list(
            @RequestParam(required = false) UUID itemId,
            @PageableDefault(size = 20, sort = "stepOrder", direction = ASC) Pageable pageable) {
        if (itemId != null) return service.findByItem(itemId);
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('production:read')")
    public ProductionStepResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('production:write')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductionStepResponse create(@Valid @RequestBody ProductionStepCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('production:write')")
    public ProductionStepResponse update(@PathVariable UUID id,
                                          @Valid @RequestBody ProductionStepUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('production:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
