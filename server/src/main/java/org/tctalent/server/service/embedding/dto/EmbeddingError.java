package org.tctalent.server.service.embedding.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

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
