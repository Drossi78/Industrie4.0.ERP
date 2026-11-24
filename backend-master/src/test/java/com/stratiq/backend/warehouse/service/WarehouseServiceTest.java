package com.stratiq.backend.warehouse.service;

import com.stratiq.backend.warehouse.dto.request.WarehouseCreateRequest;
import com.stratiq.backend.warehouse.mapper.WarehouseMapper;
import com.stratiq.backend.warehouse.model.Warehouse;
import com.stratiq.backend.warehouse.repository.WarehouseRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private WarehouseMapper warehouseMapper;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    private Warehouse testWarehouse;
    private UUID testWarehouseId;

    @BeforeEach
    void setUp() {
        testWarehouseId = UUID.randomUUID();

        testWarehouse = new Warehouse();
        testWarehouse.setId(testWarehouseId);
        testWarehouse.setName("Central Warehouse");
        testWarehouse.setCity("New York");
        testWarehouse.setCountry("USA");
        testWarehouse.setActive(true);
    }

    @Test
    void testCreateWarehouse_Success() {
        // Arrange
        WarehouseCreateRequest request = new WarehouseCreateRequest(
            "Central Warehouse", "Main facility", "123 Main St", "456",
            "10001", "New York", "NY", "USA", true
        );

        when(warehouseMapper.toEntity(request)).thenReturn(testWarehouse);
        when(warehouseRepository.save(testWarehouse)).thenReturn(testWarehouse);

        // Act
        warehouseService.create(request);

        // Assert
        verify(warehouseRepository, times(1)).save(testWarehouse);
    }

    @Test
    void testGetWarehouse_Success() {
        // Arrange
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.of(testWarehouse));

        // Act
        warehouseService.get(testWarehouseId);

        // Assert
        verify(warehouseRepository, times(1)).findById(testWarehouseId);
    }

    @Test
    void testGetWarehouse_NotFound() {
        // Arrange
        when(warehouseRepository.findById(testWarehouseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> warehouseService.get(testWarehouseId));
    }

    @Test
    void testGetWarehousesByCity_Success() {
        // Arrange
        List<Warehouse> warehouses = List.of(testWarehouse);
        when(warehouseRepository.findByCity("New York")).thenReturn(warehouses);

        // Act
        warehouseService.findByCity("New York");

        // Assert
        verify(warehouseRepository, times(1)).findByCity("New York");
    }

    @Test
    void testGetActiveWarehouses_Success() {
        // Arrange
        List<Warehouse> warehouses = List.of(testWarehouse);
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(warehouses);

        // Act
        warehouseService.findActive();

        // Assert
        verify(warehouseRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void testListWarehouses_Success() {
        // Arrange
        var pageable = PageRequest.of(0, 10);
        when(warehouseRepository.findAll(pageable)).thenReturn(mock(org.springframework.data.domain.Page.class));

        // Act
        warehouseService.list(pageable);

        // Assert
        verify(warehouseRepository, times(1)).findAll(pageable);
    }

    @Test
    void testDeleteWarehouse_Success() {
        // Arrange
        when(warehouseRepository.existsById(testWarehouseId)).thenReturn(true);

        // Act
        warehouseService.delete(testWarehouseId);

        // Assert
        verify(warehouseRepository, times(1)).existsById(testWarehouseId);
        verify(warehouseRepository, times(1)).deleteById(testWarehouseId);
    }

    @Test
    void testDeleteWarehouse_NotFound() {
        // Arrange
        when(warehouseRepository.existsById(testWarehouseId)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> warehouseService.delete(testWarehouseId));
        verify(warehouseRepository, never()).deleteById(any(UUID.class));
    }
}
