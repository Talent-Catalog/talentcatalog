package org.tctalent.server.service.embedding.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Requests embeddings for multiple inputs using one fixed model configuration.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Value
@Builder
@Jacksonized
public class GenerateEmbeddingsRequest {

    @NotNull
    @Valid
    EmbeddingModelDetails model;

    @NotNull
    @Size(min = 1, max = 500)
    @Valid
    @Singular
    List<EmbeddingInput> inputs;

    /**
     * Rejects duplicate IDs because each result must map unambiguously back to
     * one source record.
     */
    @AssertTrue(message = "Input IDs must be unique within the request")
    @JsonIgnore
    public boolean isInputIdsUnique() {
        if (inputs == null) {
            // @NotNull reports the missing-list validation error.
            return true;
        }

        var ids = new HashSet<String>();
        return inputs.stream().allMatch(input -> input == null || ids.add(input.getId()));
    }
}
