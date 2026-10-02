/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db.explanation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.Nullable;

/**
 * Persisted (jsonb) representation of a generated candidate/job match explanation.
 * <p>Stored as one {@code jsonb} column on {@link CandidateJobMatchExplanation} - the individual
 * experience explanations are not normalized into their own table.</p>
 * <p>This is a database/domain value class, independent of the {@code response} package's REST
 * DTOs - the two happen to look similar today but are separate contracts that may diverge.</p>
 */
@Value
@Builder
@Jacksonized
public class CandidateJobMatchExplanationData {

    /**
     * When the explanation was generated, as reported by the explanation service - not to be
     * confused with the entity's own created/updated audit dates.
     * <p>Null for explanations persisted before this was recorded.</p>
     * <p>Explicitly stored as an ISO-8601 string: the jsonb ObjectMapper would otherwise write it
     * as a numeric epoch timestamp.</p>
     */
    @Nullable
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    OffsetDateTime generatedAt;

    /**
     * Name of the LLM model that generated the explanation, as reported by the explanation
     * service. Null for explanations persisted before this was recorded.
     */
    @Nullable
    String modelName;

    String summary;

    List<ExperienceMatchExplanationData> experienceExplanations;

    List<String> limitations;
}
