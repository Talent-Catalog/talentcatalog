package org.tctalent.server.service.embedding.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.Nullable;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Contains one item whose text should be embedded.
 *
 * <p>Invalid text is handled as an item-level failure so that the rest of the
 * batch can continue.</p>
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Value
@Builder
@Jacksonized
public class EmbeddingInput {

    /**
     * Caller-supplied identifier used to correlate the result with the source
     * record. The embedding service does not interpret this value.
     */
    @NotNull
    @Size(min = 1)
    String id;

    /** Optional context to be used when generating the embedding. */
    @Nullable
    String context;

    /** Text from which an embedding should be generated. */
    @Nullable
    String text;
}
