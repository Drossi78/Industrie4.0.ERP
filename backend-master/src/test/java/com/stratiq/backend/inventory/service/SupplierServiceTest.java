package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.SupplierCreateRequest;
import com.stratiq.backend.inventory.dto.request.SupplierUpdateRequest;
import com.stratiq.backend.inventory.mapper.SupplierMapper;
import com.stratiq.backend.inventory.model.Supplier;
import com.stratiq.backend.inventory.repository.SupplierRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper mapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier testSupplier;
    private UUID testSupplierId;

    @BeforeEach
    void setUp() {
        testSupplierId = UUID.randomUUID();

        testSupplier = new Supplier();
        testSupplier.setId(testSupplierId);
        testSupplier.setName("Acme Supplies Ltd.");
        testSupplier.setEmail("contact@acme.com");
    }

    @Test
    void testCreate_Success() {
        SupplierCreateRequest request = new SupplierCreateRequest(
            "Acme Supplies Ltd.", "contact@acme.com", null, null,
            null, null, null, null, null, null, null
        );

        when(mapper.toEntity(request)).thenReturn(testSupplier);
        when(supplierRepository.save(testSupplier)).thenReturn(testSupplier);

        supplierService.create(request);

        verify(supplierRepository, times(1)).save(testSupplier);
    }

    @Test
    void testGet_Success() {
        when(supplierRepository.findById(testSupplierId)).thenReturn(Optional.of(testSupplier));

        supplierService.get(testSupplierId);

        verify(supplierRepository, times(1)).findById(testSupplierId);
    }

    @Test
    void testGet_NotFound() {
        when(supplierRepository.findById(testSupplierId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> supplierService.get(testSupplierId));
    }

    @Test
    void testList_Success() {
        var pageable = PageRequest.of(0, 10);
        when(supplierRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testSupplier)));

        supplierService.list(pageable);

        verify(supplierRepository, times(1)).findAll(pageable);
    }

    @Test
    void testSearch_Success() {
        when(supplierRepository.findByNameContainingIgnoreCase("Acme")).thenReturn(List.of(testSupplier));

        supplierService.search("Acme");

        verify(supplierRepository, times(1)).findByNameContainingIgnoreCase("Acme");
    }

    @Test
    void testUpdate_Success() {
        SupplierUpdateRequest request = new SupplierUpdateRequest(
            "Updated Acme", null, null, null, null, null, null, null, null, null, null
        );

        when(supplierRepository.findById(testSupplierId)).thenReturn(Optional.of(testSupplier));

        supplierService.update(testSupplierId, request);

        verify(supplierRepository, times(1)).findById(testSupplierId);
        verify(mapper, times(1)).update(testSupplier, request);
    }

    @Test
    void testUpdate_NotFound() {
        SupplierUpdateRequest request = new SupplierUpdateRequest(
            "Updated Acme", null, null, null, null, null, null, null, null, null, null
        );

        when(supplierRepository.findById(testSupplierId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> supplierService.update(testSupplierId, request));
    }

    @Test
    void testDelete_Success() {
        when(supplierRepository.existsById(testSupplierId)).thenReturn(true);

        supplierService.delete(testSupplierId);

        verify(supplierRepository, times(1)).deleteById(testSupplierId);
    }

    @Test
    void testDelete_NotFound() {
        when(supplierRepository.existsById(testSupplierId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> supplierService.delete(testSupplierId));
        verify(supplierRepository, never()).deleteById(any());
    }
}
