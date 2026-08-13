/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.api;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Information used to match candidates to a job
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class JobMatchingInfo {

    /**
     * A textual description of a job, outlining key details such as responsibilities,
     * requirements, and other relevant information that helps in assessing a match
     * with candidates.
     */
    String description;

    /**
     * The name of the job being considered for matching.
     */
    String jobName;

    /**
     * A list of skill names associated with the job.
     * Each skill name represents a specific skill in a given language.
     * <p>
     * This list is typically extracted from the above jobDescription.
     */
    List<SkillName> skillNames;
}
