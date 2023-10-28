/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.request.opportunity;

import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tctalent.server.request.PagedSearchRequest;

/**
 * Base class for search requests of either Job or Candidate opportunities
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class SearchOpportunityRequest extends PagedSearchRequest {

    /**
     * If specified, match opportunities in active stages
     */
    @Nullable
    private Boolean activeStages;

    /**
     * If specified, match opportunities whose names are like this keyword
     */
    @Nullable
    private String keyword;

    /**
     * If specified, match opportunities which are owned by me
     */
    @Nullable
    private Boolean ownedByMe;

    /**
     * If specified, match opportunities which are owned by any user working for the same partner
     * as I do.
     * eg if I work for TBB, setting this true means that I want to see all TBB opportunities,
     * not just the ones that I own.
     */
    @Nullable
    private Boolean ownedByMyPartner;

    /**
     * If specified, match opportunities based on whether the opportunity is closed.
     */
    @Nullable
    private Boolean sfOppClosed;

}
