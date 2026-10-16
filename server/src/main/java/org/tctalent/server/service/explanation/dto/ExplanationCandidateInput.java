package org.tctalent.server.service.explanation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

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
