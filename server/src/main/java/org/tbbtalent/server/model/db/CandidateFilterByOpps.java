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

package org.tbbtalent.server.model.db;

/**
 * Filter used in candidate searches based on each candidate's opportunities
 *
 * @author John Cameron
 */
public enum CandidateFilterByOpps {
    /**
     * The candidate has at least one candidate opportunity.
     */
    someOpps,

    /**
     * The candidate has no candidate opportunities.
     */
    noOpps,

    /**
     * The candidate has at least one opportunity that is not closed
     */
    openOpps,

    /**
     * The candidate has at least one opportunity that is closed
     */
    closedOpps,

    /**
     * The candidate has at least one opportunity whose stage is before
     * CandidateOpportunityStage.relocated
     */
    preRelocationOpps,

    /**
     * The candidate has at least one open or won opportunity whose stage is
     * CandidateOpportunityStage.relocated or later
     */
    postRelocationOpps

}
