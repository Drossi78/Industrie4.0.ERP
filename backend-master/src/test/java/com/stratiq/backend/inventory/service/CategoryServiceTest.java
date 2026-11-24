package com.stratiq.backend.inventory.service;

import com.stratiq.backend.inventory.dto.request.CategoryCreateRequest;
import com.stratiq.backend.inventory.mapper.CategoryMapper;
import com.stratiq.backend.inventory.model.Category;
import com.stratiq.backend.inventory.repository.CategoryRepository;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper mapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private UUID testCategoryId;

    @BeforeEach
    void setUp() {
        testCategoryId = UUID.randomUUID();

        testCategory = new Category();
        testCategory.setId(testCategoryId);
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic items");
    }

    @Test
    void testCreate_Success() {
        CategoryCreateRequest request = new CategoryCreateRequest("Electronics", "Electronic items");

        when(mapper.toEntity(request)).thenReturn(testCategory);
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);

        categoryService.create(request);

        verify(categoryRepository, times(1)).save(testCategory);
    }

    @Test
    void testGet_Success() {
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(testCategory));

        categoryService.get(testCategoryId);

        verify(categoryRepository, times(1)).findById(testCategoryId);
    }

    @Test
    void testGet_NotFound() {
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.get(testCategoryId));
    }

    @Test
    void testList_Success() {
        var pageable = PageRequest.of(0, 10);
        when(categoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(testCategory)));

        categoryService.list(pageable);

        verify(categoryRepository, times(1)).findAll(pageable);
    }

    @Test
    void testDelete_Success() {
        when(categoryRepository.existsById(testCategoryId)).thenReturn(true);

        categoryService.delete(testCategoryId);

        verify(categoryRepository, times(1)).deleteById(testCategoryId);
    }

    @Test
    void testDelete_NotFound() {
        when(categoryRepository.existsById(testCategoryId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> categoryService.delete(testCategoryId));
        verify(categoryRepository, never()).deleteById(any());
    }
}
