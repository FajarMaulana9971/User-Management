package com.unictive.usermanagement.controllers;

import com.unictive.usermanagement.dto.requests.RegisterRequest;
import com.unictive.usermanagement.dto.requests.UpdateUserRequest;
import com.unictive.usermanagement.dto.responses.AuditHistoryResponse;
import com.unictive.usermanagement.dto.responses.UserResponse;
import com.unictive.usermanagement.dto.responses.base.ApiResponse;
import com.unictive.usermanagement.dto.responses.base.PagedResponse;
import com.unictive.usermanagement.services.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create user (Admin only)",
            description = "Creates a new user. Profile picture upload is optional. " +
                    "If provided, the image will be validated and compressed automatically. "
    )
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @RequestPart(value = "user")
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {@Encoding(name = "user", contentType = "application/json")}
                    )
            )
            @Valid RegisterRequest request,
            @RequestPart(value = "profilePicture", required = false)
            MultipartFile profilePicture) {

        UserResponse response = userService.createUser(request, profilePicture);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "List all users (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sort));
        PagedResponse<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @userSecurityService.isOwner(#id, authentication.name))")
    @Operation(summary = "Get user by ID", description = "ADMIN can access any user; USER can only access their own profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id)
    {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @userSecurityService.isOwner(#id, authentication.name))")
    @Operation(summary = "Update user", description = "ADMIN can update any user; USER can only update their own data")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @PatchMapping(value = "/{id}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @userSecurityService.isOwner(#id, authentication.name))")
    @Operation(
            summary = "Upload profile picture",
            description = "Upload or replace user's profile picture. " +
                    "Accepts JPG/PNG only, max 2MB. Image is auto-compressed before storage."
    )
    public ResponseEntity<ApiResponse<UserResponse>> uploadProfilePicture(
            @PathVariable Long id,
            @RequestPart("file")
            @Parameter(description = "Profile picture (JPG/PNG, max 2MB)")
            MultipartFile file) {

        UserResponse response = userService.updateProfilePicture(id, file);
        return ResponseEntity.ok(ApiResponse.success("Profile picture updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user audit history (Admin only)",
            description = "Returns the full Envers audit trail for a user: every INSERT, UPDATE, and DELETE, " +
                    "with who made the change and a data snapshot at that revision."
    )
    public ResponseEntity<ApiResponse<List<AuditHistoryResponse>>> getUserHistory(@PathVariable Long id) {
        List<AuditHistoryResponse> history = userService.getUserHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}