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

package org.tctalent.server.response;

import lombok.Builder;
import lombok.Value;
import org.springframework.lang.Nullable;

/** Explanation of how a single candidate job experience relates to a supplied opportunity. */
@Value
@Builder
public class ExperienceMatchExplanation {

    Long experienceId;

    /**
     * Job title of the experience, as returned by the explanation service. May be null, including
     * for explanations persisted before this was recorded.
     */
    @Nullable
    String jobTitle;

    String explanation;
}
