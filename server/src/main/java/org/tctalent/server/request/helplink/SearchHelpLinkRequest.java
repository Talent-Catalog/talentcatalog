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

package org.tctalent.server.request.helplink;

import lombok.Getter;
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
public class SearchHelpLinkRequest extends PagedSearchRequest {

    /**
     * Used to select country specific help. Different countries can have different processes and
     * therefore different help documentation.
     */
    @Nullable
    private Long countryId;

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

}
