package com.stratiq.backend.user.service;

import com.stratiq.backend.user.dto.request.ChangePasswordRequest;
import com.stratiq.backend.user.dto.request.UserCreateRequest;
import com.stratiq.backend.user.mapper.UserMapper;
import com.stratiq.backend.user.model.AppUser;
import com.stratiq.backend.user.model.Role;
import com.stratiq.backend.user.repository.AppUserRepository;
import com.stratiq.backend.user.repository.RoleRepository;
import com.stratiq.backend.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private AppUser testUser;
    private Role testRole;
    private UUID testUserId;
    private UUID testRoleId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testRoleId = UUID.randomUUID();

        testRole = new Role();
        testRole.setId(testRoleId);
        testRole.setName("ADMIN");

        testUser = new AppUser();
        testUser.setId(testUserId);
        testUser.setEmail("admin@test.com");
        testUser.setPassword("encoded_password");
        testUser.setActive(true);
        testUser.setRole(testRole);
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
            "admin@test.com", "password123", testRoleId, "en"
        );

        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);

        // Act
        userService.create(request);

        // Assert
        verify(passwordEncoder, times(1)).encode("password123");
        verify(roleRepository, times(1)).findById(testRoleId);
        verify(userRepository, times(1)).save(any(AppUser.class));
    }

    @Test
    void testCreateUser_RoleNotFound() {
        // Arrange
        UserCreateRequest request = new UserCreateRequest(
            "admin@test.com", "password123", testRoleId, "en"
        );

        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void testFindById_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        userService.findById(testUserId);

        // Assert
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    void testFindById_NotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> userService.findById(testUserId));
    }

    @Test
    void testChangePassword_Success() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest(
            "password123", "newPassword456"
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(passwordEncoder.encode("newPassword456")).thenReturn("encoded_new_password");
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);

        // Act
        userService.changePassword(testUserId, request);

        // Assert
        verify(passwordEncoder, times(1)).matches("password123", "encoded_password");
        verify(passwordEncoder, times(1)).encode("newPassword456");
        verify(userRepository, times(1)).save(any(AppUser.class));
    }

    @Test
    void testChangePassword_InvalidOldPassword() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest(
            "wrongPassword", "newPassword456"
        );

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encoded_password")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            userService.changePassword(testUserId, request));
        verify(passwordEncoder, never()).encode("newPassword456");
    }

    @Test
    void testFindAll_Success() {
        // Arrange
        var pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(mock(org.springframework.data.domain.Page.class));

        // Act
        userService.findAll(pageable);

        // Assert
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(testUser));

        // Act
        userService.findByEmail("admin@test.com");

        // Assert
        verify(userRepository, times(1)).findByEmail("admin@test.com");
    }

    @Test
    void testDeleteUser_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(AppUser.class))).thenReturn(testUser);

        // Act
        userService.delete(testUserId);

        // Assert
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).save(any(AppUser.class));
    }
}
