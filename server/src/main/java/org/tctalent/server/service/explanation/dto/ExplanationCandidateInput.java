package org.tctalent.server.service.explanation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** One candidate, with the job experiences to compare against the opportunity description. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Value
@Builder
@Jacksonized
public class ExplanationCandidateInput {

    /** TC candidate ID, as a string. */
    @NotBlank
    String candidateId;

    @NotNull
    @Valid
    List<ExplanationExperienceInput> experiences;
}
