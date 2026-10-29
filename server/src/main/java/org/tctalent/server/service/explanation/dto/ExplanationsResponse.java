package org.tctalent.server.service.explanation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** Reports counts and one result per requested candidate. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Value
@Builder
@Jacksonized
public class ExplanationsResponse {

    @Min(0)
    int requested;

    @Min(0)
    int succeeded;

    @Min(0)
    int failed;

    @NotNull
    @Valid
    List<ExplanationResult> results;
}
