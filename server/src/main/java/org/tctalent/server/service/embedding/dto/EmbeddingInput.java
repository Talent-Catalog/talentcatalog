package org.tctalent.server.service.embedding.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

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

    /** Text from which an embedding should be generated. */
    @NotNull
    String text;
}
