package com.unictive.usermanagement.dto.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User response payload")
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String profilePictureUrl;
    private boolean isActive;
    private String role;
    private Set<HobbyResponse> hobbies;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
