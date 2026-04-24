package com.unictive.usermanagement.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create/Update hobby payload")
public class HobbyRequest {

    @NotBlank(message = "Hobby name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Hobby name", example = "Photography")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Schema(description = "Hobby description", example = "Capturing moments with a camera")
    private String description;
}
