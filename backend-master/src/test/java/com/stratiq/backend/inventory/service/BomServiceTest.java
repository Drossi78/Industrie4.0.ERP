package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.BomCreateRequest;
import com.stratiq.backend.inventory.mapper.BomMapper;
import com.stratiq.backend.inventory.model.BillOfMaterial;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.model.ItemKind;
import com.stratiq.backend.inventory.repository.BillOfMaterialRepository;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BomServiceTest {

    @Mock
    private BillOfMaterialRepository bomRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BomMapper mapper;

    @InjectMocks
    private BomServiceImpl bomService;

    private BillOfMaterial testBom;
    private Item testItem;
    private Item testComponent;
    private UUID testBomId;
    private UUID testItemId;
    private UUID testComponentId;

    @BeforeEach
    void setUp() {
        testBomId = UUID.randomUUID();
        testItemId = UUID.randomUUID();
        testComponentId = UUID.randomUUID();

        testItem = new Item();
        testItem.setId(testItemId);
        testItem.setName("Finished Product");
        testItem.setKind(ItemKind.OWN_PRODUCT);

        testComponent = new Item();
        testComponent.setId(testComponentId);
        testComponent.setName("Raw Material");
        testComponent.setKind(ItemKind.PURCHASED);

        testBom = new BillOfMaterial();
        testBom.setId(testBomId);
        testBom.setItem(testItem);
        testBom.setComponent(testComponent);
        testBom.setQuantity(new BigDecimal("2.0"));
    }

    @Test
    void testFindByItem_Success() {
        when(bomRepository.findByItemId(testItemId)).thenReturn(List.of(testBom));

        bomService.findByItem(testItemId);

        verify(bomRepository, times(1)).findByItemId(testItemId);
    }

    @Test
    void testFindById_Success() {
        when(bomRepository.findById(testBomId)).thenReturn(Optional.of(testBom));

        bomService.findById(testBomId);

        verify(bomRepository, times(1)).findById(testBomId);
    }

    @Test
    void testFindById_NotFound() {
        when(bomRepository.findById(testBomId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bomService.findById(testBomId));
    }

    @Test
    void testAddComponent_Success() {
        BomCreateRequest request = new BomCreateRequest(
            testItemId, testComponentId, new BigDecimal("2.0"), "piece", "notes"
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(itemRepository.findById(testComponentId)).thenReturn(Optional.of(testComponent));
        when(bomRepository.save(any(BillOfMaterial.class))).thenReturn(testBom);

        bomService.addComponent(request);

        verify(itemRepository, times(1)).findById(testItemId);
        verify(itemRepository, times(1)).findById(testComponentId);
        verify(bomRepository, times(1)).save(any(BillOfMaterial.class));
    }

    @Test
    void testAddComponent_ItemNotFound() {
        BomCreateRequest request = new BomCreateRequest(
            testItemId, testComponentId, new BigDecimal("2.0"), "piece", null
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bomService.addComponent(request));
        verify(bomRepository, never()).save(any());
    }

    @Test
    void testAddComponent_ComponentNotFound() {
        BomCreateRequest request = new BomCreateRequest(
            testItemId, testComponentId, new BigDecimal("2.0"), "piece", null
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(itemRepository.findById(testComponentId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bomService.addComponent(request));
        verify(bomRepository, never()).save(any());
    }

    @Test
    void testAddComponent_ItemNotOwnProduct_throws() {
        testItem.setKind(ItemKind.PURCHASED);
        BomCreateRequest request = new BomCreateRequest(
            testItemId, testComponentId, new BigDecimal("2.0"), "piece", null
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(itemRepository.findById(testComponentId)).thenReturn(Optional.of(testComponent));

        assertThrows(IllegalArgumentException.class, () -> bomService.addComponent(request));
        verify(bomRepository, never()).save(any());
    }

    @Test
    void testAddComponent_ComponentNotPurchased_throws() {
        testComponent.setKind(ItemKind.OWN_PRODUCT);
        BomCreateRequest request = new BomCreateRequest(
            testItemId, testComponentId, new BigDecimal("2.0"), "piece", null
        );

        when(itemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(itemRepository.findById(testComponentId)).thenReturn(Optional.of(testComponent));

        assertThrows(IllegalArgumentException.class, () -> bomService.addComponent(request));
        verify(bomRepository, never()).save(any());
    }

    @Test
    void testDelete_Success() {
        when(bomRepository.existsById(testBomId)).thenReturn(true);

        bomService.delete(testBomId);

        verify(bomRepository, times(1)).deleteById(testBomId);
    }

    @Test
    void testDelete_NotFound() {
        when(bomRepository.existsById(testBomId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> bomService.delete(testBomId));
        verify(bomRepository, never()).deleteById(any());
    }
}
