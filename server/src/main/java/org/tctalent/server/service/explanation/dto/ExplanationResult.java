package org.tctalent.server.service.explanation.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.Nullable;

/**
 * One candidate's explanation result.
 * <p>A successful result contains a summary and experience explanations, with {@code error} null.
 * A failed (item-level) result contains a non-null {@code error} instead, and callers must not
 * treat an otherwise-successful HTTP response as containing a usable explanation without checking
 * this field.</p>
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Value
@Builder
@Jacksonized
public class ExplanationResult {

    /** TC candidate ID, as a string, copied back unchanged from the request. */
    @NotBlank
    String candidateId;

    @Nullable
    String summary;

    @Nullable
    @Valid
    List<ExperienceExplanationItem> experienceExplanations;

    @Nullable
    List<String> limitations;

    /** Non-null when this candidate's explanation could not be generated. */
    @Nullable
    @Valid
    ExplanationError error;
}
