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

package org.tctalent.server.request.helplink;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.HelpFocus;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.request.PagedSearchRequest;

/**
 * Used to search for {@link HelpLink}s.
 * <p/>
 * Typically this is used to fetch help links appropriate to a user's context.
 * <p/>
 * For example, if a TC destination user operating in a particular country is changing
 * the stage of a job opportunity, the TC will use this search request to fetch links to help
 * for that country (setting countryId) and stage (setting jobStage).
 * <p/>
 * The other kind of search is where keyword is set. That is only used in the usual way for
 * managing the HelpLink table entries defined in TC settings.
 */
@Getter
@Setter
@NoArgsConstructor
public class SearchHelpLinkRequest extends PagedSearchRequest {

    /**
     * Copy constructor is useful for generating multiple requests based on a single original.
     * See code in HelpLinkHelper
     */
    public SearchHelpLinkRequest(SearchHelpLinkRequest request) {
        this.countryId = request.countryId;
        this.caseOppId = request.caseOppId;
        this.caseStage = request.caseStage;
        this.focus = request.focus;
        this.jobOppId = request.jobOppId;
        this.jobStage = request.jobStage;
        this.keyword = request.keyword;
        this.nextStepName = request.nextStepName;
        this.userId = request.userId;
    }

    /**
     * Used to select country specific help. Different countries can have different processes and
     * therefore different help documentation.
     */
    @Nullable
    private Long countryId;

    /**
     * Context field which can be used to populate, search terms like countryId if they are
     * not explicitly supplied. For example the country of the job location associated with an
     * opportunity can be used to set countryId.
     */
    @Nullable
    private Long caseOppId;

    /**
     * It only makes sense to specify at most one non-null stage in a request: caseStage or jobStage
     */
    @Nullable
    private CandidateOpportunityStage caseStage;

    /**
     * Current user focus
     */
    @Nullable
    private HelpFocus focus;

    /**
     * Context field which can be used to populate, search terms like countryId if they are
     * not explicitly supplied. For example the country of the job location associated with an
     * opportunity can be used to set countryId.
     */
    @Nullable
    private Long jobOppId;

    /**
     * It only makes sense to specify at most one non-null stage in a request: caseStage or jobStage
     */
    @Nullable
    private JobOpportunityStage jobStage;

    /**
     * This search term is only used to filter HelpLinks in the TC settings.
     * It is not used to select help links based on a user's current context.
     * <p/>
     * If keyword is specified, other fields should be null (they are ignored anyway).
     */
    @Nullable
    private String keyword;

    /**
     * This is only used in the context of a non-null stage: caseStage or jobStage.
     */
    @Nullable
    private String nextStepName;

    /**
     * Can be used to provide user related context (eg source or destination, jobCreator etc)
     * for other than the currently logged in user (which can be supplied automatically).
     * <p/>
     * Probably only used to help with testing.
     */
    @Nullable
    private Long userId;
}
