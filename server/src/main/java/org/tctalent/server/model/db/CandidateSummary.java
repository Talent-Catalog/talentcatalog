/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db;

import lombok.Getter;
import lombok.Setter;

/**
 * This is the data that is used to display candidate summaries.
 * <p/>
 * It is the DTO for candidates in response to API calls requesting a set of candidates
 * eg Searches or SavedList contents.
 *
 * @author John Cameron
 */
@Getter
@Setter
public class CandidateSummary {
    Long id;
    String candidateNumber;
    String firstName;
}
