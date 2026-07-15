package org.tctalent.server.service.embedding.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Reports the model used and one result for every requested input. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Value
@Builder
@Jacksonized
public class GenerateEmbeddingsResponse {

    @NotNull
    @Valid
    EmbeddingModelDetails model;

    @Min(0)
    int requested;

    @Min(0)
    int succeeded;

    @Min(0)
    int failed;

    @NotNull
    @Valid
    List<EmbeddingResult> results;

    /** Ensures that the summary counts agree with the item-level results. */
    @AssertTrue(message = "Embedding response counts do not match the results")
    @JsonIgnore
    public boolean areCountsValid() {
        if (results == null) {
            // @NotNull reports the missing-results validation error.
            return true;
        }

        long actualSucceeded = results.stream()
            .filter(result -> result != null && result.getEmbedding() != null)
            .count();
        long actualFailed = results.stream()
            .filter(result -> result != null && result.getError() != null)
            .count();

        return requested == results.size()
            && succeeded == actualSucceeded
            && failed == actualFailed
            && requested == succeeded + failed;
    }
}
