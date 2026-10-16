package org.tctalent.server.service.explanation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

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
