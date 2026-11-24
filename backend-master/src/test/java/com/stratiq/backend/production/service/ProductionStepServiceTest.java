package com.stratiq.backend.production.service;

import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.production.dto.request.ProductionStepCreateRequest;
import com.stratiq.backend.production.mapper.ProductionStepMapper;
import com.stratiq.backend.production.model.ProductionStep;
import com.stratiq.backend.production.repository.ProductionStepRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionStepServiceTest {

    @Mock
    private ProductionStepRepository productionStepRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProductionStepMapper productionStepMapper;

    @InjectMocks
    private ProductionStepServiceImpl productionStepService;

    private ProductionStep testProductionStep;
    private Item testItem;
    private UUID testProductionStepId;
    private UUID testItemId;

    @BeforeEach
    void setUp() {
        testProductionStepId = UUID.randomUUID();
        testItemId = UUID.randomUUID();

        testItem = new Item();
        testItem.setId(testItemId);
        testItem.setName("Production Machine A");

        testProductionStep = new ProductionStep();
        testProductionStep.setId(testProductionStepId);
        testProductionStep.setItem(testItem);
        testProductionStep.setStepOrder(1);
        testProductionStep.setName("Assembly");
        testProductionStep.setDeleted(false);
    }

    @Test
    void testCreateProductionStep_Success() {
        // Arrange
        ProductionStepCreateRequest request = new ProductionStepCreateRequest(
            testItemId, "Assembly", "Assembly step", 1, "Standard assembly"
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(productionStepMapper.toEntity(request, testItem)).thenReturn(testProductionStep);
        when(productionStepRepository.save(testProductionStep)).thenReturn(testProductionStep);

        // Act
        productionStepService.create(request);

        // Assert
        verify(itemRepository, times(1)).findById(testItemId);
        verify(productionStepRepository, times(1)).save(testProductionStep);
    }

    @Test
    void testCreateProductionStep_ItemNotFound() {
        // Arrange
        ProductionStepCreateRequest request = new ProductionStepCreateRequest(
            testItemId, "Assembly", "Assembly step", 1, "Standard assembly"
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productionStepService.create(request));
        verify(productionStepRepository, never()).save(any(ProductionStep.class));
    }

    @Test
    void testGetProductionStep_Success() {
        // Arrange
        when(productionStepRepository.findById(testProductionStepId))
            .thenReturn(Optional.of(testProductionStep));

        // Act
        productionStepService.get(testProductionStepId);

        // Assert
        verify(productionStepRepository, times(1)).findById(testProductionStepId);
    }

    @Test
    void testGetProductionStep_NotFound() {
        // Arrange
        when(productionStepRepository.findById(testProductionStepId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productionStepService.get(testProductionStepId));
    }

    @Test
    void testGetProductionStepsByItem_Success() {
        // Arrange
        List<ProductionStep> steps = List.of(testProductionStep);
        when(productionStepRepository.findByItemIdAndIsDeletedFalseOrderByStepOrderAsc(testItemId))
            .thenReturn(steps);

        // Act
        productionStepService.findByItem(testItemId);

        // Assert
        verify(productionStepRepository, times(1))
            .findByItemIdAndIsDeletedFalseOrderByStepOrderAsc(testItemId);
    }

    @Test
    void testListProductionSteps_Success() {
        // Arrange
        var pageable = PageRequest.of(0, 10);
        when(productionStepRepository.findAll(pageable)).thenReturn(mock(org.springframework.data.domain.Page.class));

        // Act
        productionStepService.list(pageable);

        // Assert
        verify(productionStepRepository, times(1)).findAll(pageable);
    }

    @Test
    void testDeleteProductionStep_Success() {
        // Arrange
        when(productionStepRepository.existsById(testProductionStepId)).thenReturn(true);

        // Act
        productionStepService.delete(testProductionStepId);

        // Assert
        verify(productionStepRepository, times(1)).existsById(testProductionStepId);
        verify(productionStepRepository, times(1)).deleteById(testProductionStepId);
    }

    @Test
    void testDeleteProductionStep_NotFound() {
        // Arrange
        when(productionStepRepository.existsById(testProductionStepId)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productionStepService.delete(testProductionStepId));
        verify(productionStepRepository, never()).deleteById(any(UUID.class));
    }
}
