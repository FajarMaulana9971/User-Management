package com.unictive.usermanagement.controllers;

import com.unictive.usermanagement.dto.requests.HobbyRequest;
import com.unictive.usermanagement.dto.responses.HobbyResponse;
import com.unictive.usermanagement.dto.responses.base.ApiResponse;
import com.unictive.usermanagement.dto.responses.base.PagedResponse;
import com.unictive.usermanagement.services.interfaces.HobbyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hobbies")
@RequiredArgsConstructor
@Tag(name = "Hobbies", description = "Hobby management endpoints (ADMIN write, USER read)")
public class HobbyController {

    private final HobbyService hobbyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create hobby (Admin only)")
    public ResponseEntity<ApiResponse<HobbyResponse>> createHobby(
            @Valid @RequestBody HobbyRequest request) {
        HobbyResponse response = hobbyService.createHobby(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "List all hobbies (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<HobbyResponse>>> getAllHobbies(
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size,
            @RequestParam(defaultValue = "name") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sort));
        PagedResponse<HobbyResponse> response = hobbyService.getAllHobbies(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get hobby by ID")
    public ResponseEntity<ApiResponse<HobbyResponse>> getHobbyById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(hobbyService.getHobbyById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update hobby (Admin only)")
    public ResponseEntity<ApiResponse<HobbyResponse>> updateHobby(
            @PathVariable Long id,
            @Valid @RequestBody HobbyRequest request) {
        HobbyResponse response = hobbyService.updateHobby(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hobby updated", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete hobby (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteHobby(@PathVariable Long id) {
        hobbyService.deleteHobby(id);
        return ResponseEntity.ok(ApiResponse.success("Hobby deleted successfully", null));
    }
}
