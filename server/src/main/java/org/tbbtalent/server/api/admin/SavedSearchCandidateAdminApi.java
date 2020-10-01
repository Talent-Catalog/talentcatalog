/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.api.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.ExportFailedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.request.candidate.SavedSearchGetRequest;
import org.tbbtalent.server.request.list.HasSetOfCandidatesImpl;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.SavedSearchService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;

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
    private final UserContext userContext;
    private final CandidateBuilderSelector builderSelector;

    private static final Logger log = LoggerFactory.getLogger(SavedSearchCandidateAdminApi.class);

    @Autowired
    public SavedSearchCandidateAdminApi(
            SavedSearchService savedSearchService,
            CandidateService candidateService, 
            UserContext userContext) {
        this.savedSearchService = savedSearchService;
        this.candidateService = candidateService;
        this.userContext = userContext;
        builderSelector = new CandidateBuilderSelector(userContext);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(
            long savedSearchId, @Valid SavedSearchGetRequest request) 
            throws NoSuchObjectException {
        
        Page<Candidate> candidates =
                candidateService.searchCandidates(savedSearchId, request);

        candidateService.setCandidateContext(savedSearchId, candidates);
        
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }

    @PostMapping(value = "{id}/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void export(
            @PathVariable("id") long savedSearchId, 
            @Valid  @RequestBody SavedSearchGetRequest request,
            HttpServletResponse response) throws IOException, ExportFailedException {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "candidates.csv\"");
        response.setContentType("text/csv; charset=utf-8");
        candidateService.exportToCsv(savedSearchId, request, response.getWriter());
    }
    
}
