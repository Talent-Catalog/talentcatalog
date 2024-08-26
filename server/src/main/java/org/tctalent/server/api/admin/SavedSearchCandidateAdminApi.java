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

package org.tctalent.server.api.admin;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.ExportFailedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.request.candidate.SavedSearchGetRequest;
import org.tctalent.server.request.list.UpdateSavedListContentsRequest;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Web API for retrieving the candidates resulting from a SavedSearch.
 * <p/>
 * For actually modifying candidate details - see {@link CandidateAdminApi},
 * or for modifying saved search details see {@link SavedSearchAdminApi}.
 */
@RestController()
@RequestMapping("/api/admin/saved-search-candidate")
@Slf4j
public class SavedSearchCandidateAdminApi implements
    IManyToManyApi<SavedSearchGetRequest, UpdateSavedListContentsRequest> {

    private final SavedSearchService savedSearchService;
    private final CandidateBuilderSelector builderSelector;

    @Autowired
    public SavedSearchCandidateAdminApi(
        CandidateOpportunityService candidateOpportunityService, SavedSearchService savedSearchService,
            UserService userService) {
        this.savedSearchService = savedSearchService;
        builderSelector = new CandidateBuilderSelector(candidateOpportunityService, userService);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(
            long savedSearchId, @Valid SavedSearchGetRequest request)
            throws NoSuchObjectException {

        Page<Candidate> candidates =
                savedSearchService.searchCandidates(savedSearchId, request);

        savedSearchService.setCandidateContext(savedSearchId, candidates);

        DtoBuilder builder = builderSelector.selectBuilder(request.getDtoType());
        return builder.buildPage(candidates);
    }

    @GetMapping(value = "{id}/is-empty")
    public boolean isEmpty(@PathVariable("id") long savedSearchId) throws NoSuchObjectException {
        return savedSearchService.isEmpty(savedSearchId);
    }

    @PostMapping(value = "{id}/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void export(
            @PathVariable("id") long savedSearchId,
            @Valid  @RequestBody SavedSearchGetRequest request,
            HttpServletResponse response) throws IOException, ExportFailedException {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "candidates.csv\"");
        response.setContentType("text/csv; charset=utf-8");
        savedSearchService.exportToCsv(savedSearchId, request, response.getWriter());
    }

}
