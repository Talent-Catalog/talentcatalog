package org.tctalent.server.service.explanation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Requests explanations of how a batch of candidates relate to a single opportunity description.
 * <p>The TC caller currently only ever populates a single-candidate batch, but the Python service
 * exposes a batch API, so the request shape supports more than one candidate.</p>
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Value
@Builder
@Jacksonized
public class ExplanationsRequest {

    @NotBlank
    String opportunityDescription;

    @NotNull
    @Valid
    @Size(min = 1)
    List<ExplanationCandidateInput> candidates;
}
