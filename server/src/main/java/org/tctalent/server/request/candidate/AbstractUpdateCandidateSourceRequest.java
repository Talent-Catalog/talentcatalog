/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.AbstractCandidateSource;
import org.tctalent.server.model.db.SalesforceJobOpp;

/**
 * Base class for any Update/Create requests on candidate sources
 * (sublasses of {@link AbstractCandidateSource}).
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public abstract class AbstractUpdateCandidateSourceRequest {

    /**
     * @see AbstractCandidateSource
     */
    @Nullable
    private String name;

    /**
     * @see AbstractCandidateSource
     */
    @Nullable
    private Boolean fixed;

    /**
     * @see AbstractCandidateSource
     */
    @Nullable
    private Boolean global;

    /**
     * @see AbstractCandidateSource
     */
    @Nullable
    private SalesforceJobOpp sfJobOpp;

    /**
     * If non null and > 0, this is the id of job associated with candidate source.
     * If non null and <= 0, this indicates that the candidate source is not associated with a job.
     * If null, this field is ignored.
     */
    @Nullable
    private Long jobId;

    public void populateFromRequest(AbstractCandidateSource candidateSource) {
        candidateSource.setName(name);
        candidateSource.setFixed(fixed);
        candidateSource.setGlobal(global);
    }
}
