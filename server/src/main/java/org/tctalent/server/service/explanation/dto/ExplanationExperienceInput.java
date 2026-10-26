package org.tctalent.server.service.explanation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** One candidate job experience, as sent to the Python match explanation service. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Value
@Builder
@Jacksonized
public class ExplanationExperienceInput {

    /** TC candidate job experience ID, as a string. */
    @NotBlank
    String experienceId;

    /** Job experience's role/title. */
    String jobTitle;

    /** Job experience's free text description. */
    String description;
}
