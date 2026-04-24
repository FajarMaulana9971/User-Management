package com.unictive.usermanagement.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "User registration payload")
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, digits, and underscores")
    @Schema(description = "Unique username", example = "fajar")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "fajar@gmail.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "Password (min 8 chars)", example = "R4hasia")
    private String password;

    @Size(max = 255, message = "Full name cannot exceed 255 characters")
    @Schema(description = "Full name", example = "Fajar Anwari Maulana")
    private String fullName;

    @Schema(description = "Set of hobby IDs to assign", example = "[1, 2]")
    private Set<Long> hobbyIds;
}
