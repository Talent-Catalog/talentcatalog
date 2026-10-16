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

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.Nullable;

/**
 * Persisted (jsonb) representation of the explanation for one candidate job experience.
 * <p>This is a database/domain value class, independent of the {@code response} package's REST
 * DTOs - the two happen to look similar today but are separate contracts that may diverge.</p>
 */
@Value
@Builder
@Jacksonized
public class ExperienceMatchExplanationData {

    Long experienceId;

    /** Null when not supplied, including for explanations persisted before this was recorded. */
    @Nullable
    String jobTitle;

    String explanation;
}
