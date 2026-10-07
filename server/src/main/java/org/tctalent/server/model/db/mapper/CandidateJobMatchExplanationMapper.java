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

package org.tctalent.server.model.db.mapper;

import org.mapstruct.Mapper;
import org.tctalent.server.model.db.explanation.CandidateJobMatchExplanationData;
import org.tctalent.server.model.db.explanation.ExperienceMatchExplanationData;
import org.tctalent.server.response.CandidateMatchExplanation;
import org.tctalent.server.response.ExperienceMatchExplanation;

/**
 * Maps between the persisted (jsonb) {@link CandidateJobMatchExplanationData} representation of a
 * candidate/job match explanation and the {@link CandidateMatchExplanation} REST response
 * representation.
 * <p/>
 * These are kept as separate types (see the doc on {@link CandidateJobMatchExplanationData}) even
 * though their fields currently align one-to-one.
 */
@Mapper
public interface CandidateJobMatchExplanationMapper {

    CandidateMatchExplanation toResponse(CandidateJobMatchExplanationData data);

    CandidateJobMatchExplanationData toData(CandidateMatchExplanation response);

    ExperienceMatchExplanation toResponse(ExperienceMatchExplanationData data);

    ExperienceMatchExplanationData toData(ExperienceMatchExplanation response);
}
