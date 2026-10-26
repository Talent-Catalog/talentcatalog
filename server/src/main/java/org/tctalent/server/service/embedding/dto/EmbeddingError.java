package org.tctalent.server.service.embedding.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** Describes why an individual embedding could not be generated. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Value
@Builder
@Jacksonized
public class EmbeddingError {

    @NotNull
    EmbeddingErrorCode code;

    @NotNull
    @Size(min = 1)
    String message;
}
