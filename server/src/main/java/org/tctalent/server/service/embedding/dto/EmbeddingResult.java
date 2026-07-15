package org.tctalent.server.service.embedding.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Contains the embedding result for one input item.
 *
 * <p>A successful result contains an embedding and no error. A failed result
 * contains an error and no embedding.</p>
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Value
@Builder
@Jacksonized
public class EmbeddingResult {

    /** Caller-supplied identifier copied unchanged from the input item. */
    @NotNull
    @Size(min = 1)
    String id;

    /** Generated vector, or {@code null} when generation failed. */
    List<Double> embedding;

    /** Item-level failure details, or {@code null} on success. */
    @Valid
    EmbeddingError error;

    /** Ensures that the result contains exactly one outcome. */
    @AssertTrue(message = "Exactly one of embedding or error must be supplied")
    @JsonIgnore
    public boolean isOutcomeValid() {
        return (embedding == null) == (error != null);
    }

    /** Returns whether this item was embedded successfully. */
    @JsonIgnore
    public boolean isSuccessful() {
        return embedding != null;
    }
}
