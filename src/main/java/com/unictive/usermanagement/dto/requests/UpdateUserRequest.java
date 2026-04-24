package com.unictive.usermanagement.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Update user payload")
public class UpdateUserRequest {

    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, digits, and underscores")
    @Schema(description = "New username", example = "fajar_updated")
    private String username;

    @Email(message = "Invalid email format")
    @Schema(description = "New email", example = "fajar.updated@gmail.com")
    private String email;

    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Schema(description = "New password")
    private String password;

    @Size(max = 255)
    @Schema(description = "Full name")
    private String fullName;

    @Schema(description = "Set of hobby IDs")
    private Set<Long> hobbyIds;
}
