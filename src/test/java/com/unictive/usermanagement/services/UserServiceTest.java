package com.unictive.usermanagement.services;

import com.unictive.usermanagement.dto.requests.RegisterRequest;
import com.unictive.usermanagement.dto.requests.UpdateUserRequest;
import com.unictive.usermanagement.dto.responses.UserResponse;
import com.unictive.usermanagement.dto.responses.base.PagedResponse;
import com.unictive.usermanagement.entities.Hobby;
import com.unictive.usermanagement.entities.Role;
import com.unictive.usermanagement.entities.User;
import com.unictive.usermanagement.enums.RoleName;
import com.unictive.usermanagement.exceptions.types.ConflictException;
import com.unictive.usermanagement.exceptions.types.ResourceNotFoundException;
import com.unictive.usermanagement.mappers.UserMapper;
import com.unictive.usermanagement.repositories.HobbyRepository;
import com.unictive.usermanagement.repositories.RoleRepository;
import com.unictive.usermanagement.repositories.UserRepository;
import com.unictive.usermanagement.services.implementations.UserServiceImpl;
import com.unictive.usermanagement.services.interfaces.ImageProcessingService;
import com.unictive.usermanagement.services.interfaces.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private HobbyRepository hobbyRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private StorageService storageService;
    @Mock private ImageProcessingService imageProcessingService;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private Role userRole;
    private User sampleUser;
    private UserResponse sampleUserResponse;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name(RoleName.USER).build();

        sampleUser = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("hashed_password")
                .fullName("John Doe")
                .role(userRole)
                .active(true)
                .build();

        sampleUserResponse = UserResponse.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .fullName("John Doe")
                .role("USER")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("should create user successfully without profile picture")
        void shouldCreateUserSuccessfully() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("john_doe");
            request.setEmail("john@example.com");
            request.setPassword("SecretPass123!");
            request.setFullName("John Doe");

            when(userRepository.existsByUsername("john_doe")).thenReturn(false);
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));
            when(userMapper.toEntity(request)).thenReturn(sampleUser);
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(sampleUser);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            UserResponse result = userService.createUser(request, null);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("john_doe");
            verify(userRepository).save(any(User.class));
            verify(storageService, never()).store(any(), any());
        }

        @Test
        @DisplayName("should throw ConflictException when username already exists")
        void shouldThrowWhenUsernameExists() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("john_doe");
            request.setEmail("new@example.com");

            when(userRepository.existsByUsername("john_doe")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request, null))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("john_doe");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ConflictException when email already exists")
        void shouldThrowWhenEmailExists() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("new_user");
            request.setEmail("john@example.com");

            when(userRepository.existsByUsername("new_user")).thenReturn(false);
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request, null))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("john@example.com");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when USER role not found")
        void shouldThrowWhenRoleNotFound() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("john_doe");
            request.setEmail("john@example.com");

            when(userRepository.existsByUsername(any())).thenReturn(false);
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.createUser(request, null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should assign hobbies when hobbyIds are provided")
        void shouldAssignHobbies() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("john_doe");
            request.setEmail("john@example.com");
            request.setPassword("pass");
            request.setHobbyIds(Set.of(1L, 2L));

            Hobby hobby1 = Hobby.builder().id(1L).name("Reading").build();
            Hobby hobby2 = Hobby.builder().id(2L).name("Coding").build();

            when(userRepository.existsByUsername(any())).thenReturn(false);
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));
            when(userMapper.toEntity(request)).thenReturn(sampleUser);
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(hobbyRepository.findByIdIn(Set.of(1L, 2L))).thenReturn(List.of(hobby1, hobby2));
            when(userRepository.save(any())).thenReturn(sampleUser);
            when(userMapper.toResponse(any())).thenReturn(sampleUserResponse);

            userService.createUser(request, null);

            verify(hobbyRepository).findByIdIn(Set.of(1L, 2L));
        }
    }

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUser() {
            when(userRepository.findByIdWithRole(1L)).thenReturn(Optional.of(sampleUser));
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            UserResponse result = userService.getUserById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findByIdWithRole(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("should return paged list of users")
        void shouldReturnPagedUsers() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(sampleUser), pageable, 1);

            when(userRepository.findAllWithRole(pageable)).thenReturn(userPage);
            when(userMapper.toResponse(sampleUser)).thenReturn(sampleUserResponse);

            PagedResponse<UserResponse> result = userService.getAllUsers(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return empty paged result when no users")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(userRepository.findAllWithRole(pageable)).thenReturn(emptyPage);

            PagedResponse<UserResponse> result = userService.getAllUsers(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("updateUser()")
    class UpdateUser {

        @Test
        @DisplayName("should update user fields successfully")
        void shouldUpdateUser() {
            UpdateUserRequest request = new UpdateUserRequest();
            request.setFullName("Jane Doe");
            request.setEmail("jane@example.com");

            when(userRepository.findByIdWithRole(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmailAndIdNot("jane@example.com", 1L)).thenReturn(false);
            when(userRepository.save(any())).thenReturn(sampleUser);
            when(userMapper.toResponse(any())).thenReturn(sampleUserResponse);

            UserResponse result = userService.updateUser(1L, request);

            assertThat(result).isNotNull();
            verify(userRepository).save(any());
        }

        @Test
        @DisplayName("should throw ConflictException when new email already taken")
        void shouldThrowWhenEmailAlreadyTaken() {
            UpdateUserRequest request = new UpdateUserRequest();
            request.setEmail("taken@example.com");

            when(userRepository.findByIdWithRole(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmailAndIdNot("taken@example.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUser(1L, request))
                    .isInstanceOf(ConflictException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("should delete user and their profile picture")
        void shouldDeleteUser() {
            sampleUser.setProfilePicture("/uploads/profile-pictures/test.jpg");
            when(userRepository.findByIdWithRole(1L)).thenReturn(Optional.of(sampleUser));

            userService.deleteUser(1L);

            verify(storageService).delete("/uploads/profile-pictures/test.jpg");
            verify(userRepository).delete(sampleUser);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findByIdWithRole(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).delete(any());
        }
    }
}