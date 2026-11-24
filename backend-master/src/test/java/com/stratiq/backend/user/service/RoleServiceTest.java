package com.stratiq.backend.user.service;

import com.stratiq.backend.shared.exception.NotFoundException;
import com.stratiq.backend.user.dto.request.PermissionAssignRequest;
import com.stratiq.backend.user.dto.request.RoleCreateRequest;
import com.stratiq.backend.user.dto.request.RoleUpdateRequest;
import com.stratiq.backend.user.mapper.RoleMapper;
import com.stratiq.backend.user.model.Permission;
import com.stratiq.backend.user.model.Role;
import com.stratiq.backend.user.repository.PermissionRepository;
import com.stratiq.backend.user.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleMapper mapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;
    private Permission testPermission;
    private UUID testRoleId;
    private UUID testPermissionId;

    @BeforeEach
    void setUp() {
        testRoleId = UUID.randomUUID();
        testPermissionId = UUID.randomUUID();

        testPermission = new Permission();
        testPermission.setId(testPermissionId);
        testPermission.setName("Read Inventory");
        testPermission.setCodename("inventory:read");

        testRole = new Role();
        testRole.setId(testRoleId);
        testRole.setName("MANAGER");
        testRole.setDescription("Manager role");
        testRole.setPermissions(new HashSet<>());
    }

    @Test
    void testFindAll_Success() {
        when(roleRepository.findAll()).thenReturn(List.of(testRole));

        roleService.findAll();

        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Success() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));

        roleService.findById(testRoleId);

        verify(roleRepository, times(1)).findById(testRoleId);
    }

    @Test
    void testFindById_NotFound() {
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roleService.findById(testRoleId));
    }

    @Test
    void testCreate_Success() {
        RoleCreateRequest request = new RoleCreateRequest("MANAGER", "Manager role");

        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        roleService.create(request);

        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void testUpdate_Success() {
        RoleUpdateRequest request = new RoleUpdateRequest("UPDATED_MANAGER", null);

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(testRole)).thenReturn(testRole);

        roleService.update(testRoleId, request);

        verify(roleRepository, times(1)).findById(testRoleId);
        verify(roleRepository, times(1)).save(testRole);
    }

    @Test
    void testUpdate_NotFound() {
        RoleUpdateRequest request = new RoleUpdateRequest("UPDATED_MANAGER", null);

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roleService.update(testRoleId, request));
    }

    @Test
    void testAssignPermissions_Success() {
        PermissionAssignRequest request = new PermissionAssignRequest(List.of(testPermissionId));

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(testPermissionId)).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(testRole)).thenReturn(testRole);

        roleService.assignPermissions(testRoleId, request);

        verify(roleRepository, times(1)).findById(testRoleId);
        verify(permissionRepository, times(1)).findById(testPermissionId);
        verify(roleRepository, times(1)).save(testRole);
    }

    @Test
    void testAssignPermissions_PermissionNotFound() {
        PermissionAssignRequest request = new PermissionAssignRequest(List.of(testPermissionId));

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(testPermissionId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> roleService.assignPermissions(testRoleId, request));
    }

    @Test
    void testRevokePermissions_Success() {
        testRole.getPermissions().add(testPermission);
        PermissionAssignRequest request = new PermissionAssignRequest(List.of(testPermissionId));

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.save(testRole)).thenReturn(testRole);

        roleService.revokePermissions(testRoleId, request);

        verify(roleRepository, times(1)).findById(testRoleId);
        verify(roleRepository, times(1)).save(testRole);
    }

    @Test
    void testDelete_Success() {
        when(roleRepository.existsById(testRoleId)).thenReturn(true);

        roleService.delete(testRoleId);

        verify(roleRepository, times(1)).deleteById(testRoleId);
    }

    @Test
    void testDelete_NotFound() {
        when(roleRepository.existsById(testRoleId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> roleService.delete(testRoleId));
        verify(roleRepository, never()).deleteById(any());
    }
}
