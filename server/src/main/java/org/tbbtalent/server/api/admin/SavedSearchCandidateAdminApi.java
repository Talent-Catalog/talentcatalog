/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.api.admin;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.request.candidate.SavedSearchGetRequest;
import org.tbbtalent.server.request.list.HasSetOfCandidatesImpl;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateService;
import org.tbbtalent.server.service.SavedSearchService;
import org.tbbtalent.server.util.dto.DtoBuilder;

/**
 * Web API for retrieving the candidates resulting from a SavedSearch.
 * <p/>
 * For actually modifying candidate details - see {@link CandidateAdminApi}, 
 * or for modifying saved search details see {@link SavedSearchAdminApi}. 
 */
@RestController()
@RequestMapping("/api/admin/saved-search-candidate")
public class SavedSearchCandidateAdminApi implements IManyToManyApi<SavedSearchGetRequest, HasSetOfCandidatesImpl> {

    private final CandidateService candidateService;
    private final SavedSearchService savedSearchService;
    private final CandidateBuilderSelector builderSelector;

    @Autowired
    public SavedSearchCandidateAdminApi(
            SavedSearchService savedSearchService,
            CandidateService candidateService, 
            UserContext userContext) {
        this.savedSearchService = savedSearchService;
        this.candidateService = candidateService;
        builderSelector = new CandidateBuilderSelector(userContext);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(
            long savedSearchId, @Valid SavedSearchGetRequest request) 
            throws NoSuchObjectException {
        Page<Candidate> candidates =
                this.candidateService.searchCandidates(savedSearchId, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }
}
