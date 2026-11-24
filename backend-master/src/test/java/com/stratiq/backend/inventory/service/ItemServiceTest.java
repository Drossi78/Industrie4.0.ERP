package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.ItemCreateRequest;
import com.stratiq.backend.inventory.mapper.ItemMapper;
import com.stratiq.backend.inventory.model.Category;
import com.stratiq.backend.inventory.model.Item;
import com.stratiq.backend.inventory.model.ItemKind;
import com.stratiq.backend.inventory.repository.CategoryRepository;
import com.stratiq.backend.inventory.repository.ItemRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item testItem;
    private Category testCategory;
    private UUID testItemId;
    private UUID testCategoryId;

    @BeforeEach
    void setUp() {
        testItemId = UUID.randomUUID();
        testCategoryId = UUID.randomUUID();

        testCategory = new Category();
        testCategory.setId(testCategoryId);
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic items");

        testItem = new Item();
        testItem.setId(testItemId);
        testItem.setName("Laptop");
        testItem.setKind(ItemKind.OWN_PRODUCT);
        testItem.setCategory(testCategory);
        testItem.setUnit("piece");
        testItem.setPrice(new BigDecimal("999.99"));
        testItem.setDescription("High-performance laptop");
        testItem.setNotes("Model X200");
        testItem.setDeleted(false);
        testItem.setCreatedAt(LocalDateTime.now());
        testItem.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateItem_Success() {
        // Arrange
        ItemCreateRequest request = new ItemCreateRequest(
            "Laptop", ItemKind.OWN_PRODUCT, testCategoryId,
            "piece", new BigDecimal("999.99"), "High-performance laptop", "Model X200"
        );

        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(testCategory));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        // Act
        itemService.create(request);

        // Assert
        verify(categoryRepository, times(1)).findById(testCategoryId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void testCreateItem_CategoryNotFound() {
        // Arrange
        ItemCreateRequest request = new ItemCreateRequest(
            "Laptop", ItemKind.OWN_PRODUCT, testCategoryId,
            "piece", new BigDecimal("999.99"), "High-performance laptop", "Model X200"
        );

        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> itemService.create(request));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(itemRepository.findByIdAndIsDeletedFalse(testItemId)).thenReturn(Optional.of(testItem));

        // Act
        itemService.findById(testItemId);

        // Assert
        verify(itemRepository, times(1)).findByIdAndIsDeletedFalse(testItemId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(itemRepository.findByIdAndIsDeletedFalse(testItemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> itemService.findById(testItemId));
    }

    @Test
    void testSearchItems_ByName() {
        // Arrange
        List<Item> items = List.of(testItem);
        when(itemRepository.findByIsDeletedFalse()).thenReturn(items);

        // Act
        itemService.findAll();

        // Assert
        verify(itemRepository, times(1)).findByIsDeletedFalse();
    }

    @Test
    void testGetItemsByCategory_Success() {
        // Arrange
        List<Item> items = List.of(testItem);
        when(itemRepository.findByCategoryId(testCategoryId)).thenReturn(items);

        // Act
        itemService.findByCategory(testCategoryId);

        // Assert
        verify(itemRepository, times(1)).findByCategoryId(testCategoryId);
    }

    @Test
    void testSoftDeleteItem_Success() {
        // Arrange
        when(itemRepository.findByIdAndIsDeletedFalse(testItemId)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        // Act
        itemService.delete(testItemId);

        // Assert
        verify(itemRepository, times(1)).findByIdAndIsDeletedFalse(testItemId);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void testFindByKind_Success() {
        // Arrange
        List<Item> items = List.of(testItem);
        when(itemRepository.findByKind(ItemKind.OWN_PRODUCT)).thenReturn(items);

        // Act
        itemService.findByKind(ItemKind.OWN_PRODUCT);

        // Assert
        verify(itemRepository, times(1)).findByKind(ItemKind.OWN_PRODUCT);
    }
}
