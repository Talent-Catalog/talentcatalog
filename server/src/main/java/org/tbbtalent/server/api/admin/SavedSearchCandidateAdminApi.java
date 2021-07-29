/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import org.tbbtalent.server.request.list.UpdateSavedListContentsRequest;
import org.tbbtalent.server.security.AuthService;
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
public class SavedSearchCandidateAdminApi implements 
    IManyToManyApi<SavedSearchGetRequest, UpdateSavedListContentsRequest> {

    private final CandidateService candidateService;
    private final SavedSearchService savedSearchService;
    private final AuthService authService;
    private final CandidateBuilderSelector builderSelector;

    private static final Logger log = LoggerFactory.getLogger(SavedSearchCandidateAdminApi.class);

    @Autowired
    public SavedSearchCandidateAdminApi(
            SavedSearchService savedSearchService,
            CandidateService candidateService,
            AuthService authService) {
        this.savedSearchService = savedSearchService;
        this.candidateService = candidateService;
        this.authService = authService;
        builderSelector = new CandidateBuilderSelector(authService);
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
