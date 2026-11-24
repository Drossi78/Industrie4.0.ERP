package com.stratiq.backend.user.service;

import com.stratiq.backend.user.dto.request.PermissionCreateRequest;
import com.stratiq.backend.user.model.Permission;
import com.stratiq.backend.user.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private Permission testPermission;
    private UUID testPermissionId;

    @BeforeEach
    void setUp() {
        testPermissionId = UUID.randomUUID();

        testPermission = new Permission();
        testPermission.setId(testPermissionId);
        testPermission.setName("Read Inventory");
        testPermission.setCodename("inventory:read");
    }

    @Test
    void testFindAll_Success() {
        when(permissionRepository.findAll()).thenReturn(List.of(testPermission));

        var result = permissionService.findAll();

        verify(permissionRepository, times(1)).findAll();
        assertEquals(1, result.size());
    }

    @Test
    void testCreate_Success() {
        PermissionCreateRequest request = new PermissionCreateRequest("Read Inventory", "inventory:read");
        Permission savedPermission = new Permission();
        savedPermission.setId(testPermissionId);
        savedPermission.setName("Read Inventory");
        savedPermission.setCodename("inventory:read");

        when(permissionRepository.save(any(Permission.class))).thenReturn(savedPermission);

        var result = permissionService.create(request);

        verify(permissionRepository, times(1)).save(any(Permission.class));
        assertEquals("inventory:read", result.codename());
    }
}
