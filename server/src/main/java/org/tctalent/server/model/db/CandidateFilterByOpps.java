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

package org.tctalent.server.model.db;

import org.springframework.lang.Nullable;

/**
 * Filter used in candidate searches based on each candidate's opportunities.
 * <p/>
 * For display convenience this enum is used and sent to the front end to display to users
 * selecting the type of filter they want.
 * However, on the server side these filters are most simply implemented as a number of Boolean
 * filters anyOpps, closesOpps and relocatedOpps - which is what is stored with the SavedSearches.
 * <p/>
 * This enum provides the code for mapping between enum values and combinations of the boolean
 * values.
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

    /**
     * Set of Boolean values corresponding to enum values.
     * <p/>
     * Underscores to avoid clash between closedOpps enum name and Boolean variable name.
     */
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

    /**
     * Maps the given Boolean opps filters on to a CandidateFilterByOpps enum value, or null
     * if all filters are null.
     * @param anyOpps See SavedSearch.anyOpps
     * @param closedOpps_ See SavedSearch.closedOpps (added underscore to avoid clash with enum name)
     * @param relocatedOpps See SavedSearch.relocatedOpps
     * @return Enum value corresponding to the given Boolean values, or null if all values are null.
     */
    @Nullable
    public static CandidateFilterByOpps mapToEnum(
        Boolean anyOpps, Boolean closedOpps_, Boolean relocatedOpps) {

        CandidateFilterByOpps val;
        if (anyOpps != null) {
            val = anyOpps ? someOpps : noOpps;
        } else if (closedOpps_ != null) {
            val = closedOpps_ ? closedOpps : openOpps;
        } else if (relocatedOpps != null) {
            val = relocatedOpps ? postRelocationOpps : preRelocationOpps;
        } else {
            val = null;
        }
        return val;
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
