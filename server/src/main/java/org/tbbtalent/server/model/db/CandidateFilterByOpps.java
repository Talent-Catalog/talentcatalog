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
    someOpps(true, null, null),

    /**
     * The candidate has no candidate opportunities.
     */
    noOpps(false, null, null),

    /**
     * The candidate has at least one opportunity that is not closed
     */
    openOpps(null, false, null),

    /**
     * The candidate has at least one opportunity that is closed
     */
    closedOpps(null, true, null),

    /**
     * The candidate has at least one opportunity whose stage is before
     * CandidateOpportunityStage.relocated
     */
    preRelocationOpps(null, null, false),

    /**
     * The candidate has at least one open or won opportunity whose stage is
     * CandidateOpportunityStage.relocated or later
     */
    postRelocationOpps(null, null, true);

    private final Boolean anyOpps_;
    private final Boolean closedOpps_;
    private final Boolean relocatedOpps_;

    /**
     * Each enum value corresponds to a combination of Boolean flags which are used to direct the
     * search.
     * @param anyOpps If not null, true means search for candidates with at least one opp, false
     *                means search for candidates with no opps.
     * @param closedOpps If not null, true means search for candidates with at least one closed opp,
     *                   false means search for candidates with at least one unclosed opp.
     * @param relocatedOpps If not null, true means search for candidates with at least one opp at
     *                      relocated stage or later, false true means search for candidates with
     *                      at least one opp whose stage is earlier than relocated
     */
    CandidateFilterByOpps(Boolean anyOpps, Boolean closedOpps, Boolean relocatedOpps) {
        this.anyOpps_ = anyOpps;
        this.closedOpps_ = closedOpps;
        this.relocatedOpps_ = relocatedOpps;
    }

    public Boolean getAnyOpps() {
      return anyOpps_;
    }

    public Boolean getClosedOpps() {
      return closedOpps_;
    }

    public Boolean getRelocatedOpps() {
      return relocatedOpps_;
    }
}
