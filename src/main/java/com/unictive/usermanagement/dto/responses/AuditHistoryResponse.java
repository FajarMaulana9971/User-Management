package com.unictive.usermanagement.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Audit history record")
public class AuditHistoryResponse {

    @Schema(description = "Revision number")
    private Integer revision;

    @Schema(description = "Type of operation: ADD, MOD, DEL")
    private String revisionType;

    @Schema(description = "Who made the change")
    private String changedBy;

    @Schema(description = "When the change was made")
    private Instant changedAt;

    @Schema(description = "Snapshot of data at this revision")
    private UserResponse data;
}
