/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.api.admin;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.ExportFailedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.request.candidate.SavedListGetRequest;
import org.tbbtalent.server.request.list.HasSetOfCandidatesImpl;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.util.dto.DtoBuilder;

/**
 * Web API for managing the association between a SavedList and its associated
 * Candidate's.
 * <p/>
 * This API:
 * <ul>
 *     <li>Updates the candidates which belong to a saved list</li>
 *     <li>Can request all candidates or a subset of candidates (based on
 *     search criteria) belonging to a saved list</li>
 * </ul>
 * <p/>
 * For actually modifying candidate details - see {@link CandidateAdminApi}, 
 * or for modifying saved list details see {@link SavedListAdminApi}. 
 * <p/> 
 * See also {@link CandidateSavedListAdminApi} which is the mirror image,
 * managing the reverse association between a Candidate and its associated
 * SavedList's (that it belongs to).
 * 
 */
@RestController()
@RequestMapping("/api/admin/saved-list-candidate")
public class SavedListCandidateAdminApi implements IManyToManyApi<SavedListGetRequest, HasSetOfCandidatesImpl> {

    private final CandidateService candidateService;
    private final SavedListService savedListService;
    private final CandidateBuilderSelector builderSelector;

    @Autowired
    public SavedListCandidateAdminApi(
            CandidateService candidateService, SavedListService savedListService, 
            UserContext userContext) {
        this.candidateService = candidateService;
        this.savedListService = savedListService;
        builderSelector = new CandidateBuilderSelector(userContext);
    }

    @Override
    public void merge(long savedListId, @Valid HasSetOfCandidatesImpl request) 
            throws NoSuchObjectException {
        savedListService.mergeSavedList(savedListId, request);
    }

    @Override
    public void remove(long savedListId, @Valid HasSetOfCandidatesImpl request) 
            throws NoSuchObjectException {
        savedListService.removeFromSavedList(savedListId, request);
    }

    @Override
    public void replace(long savedListId, @Valid HasSetOfCandidatesImpl request) 
            throws NoSuchObjectException {
        savedListService.clearSavedList(savedListId);
        savedListService.mergeSavedList(savedListId, request);
    }

    @Override
    public @NotNull Map<String, Object> searchPaged(
            long savedListId, @Valid SavedListGetRequest request) 
            throws NoSuchObjectException {
        Page<Candidate> candidates = this.candidateService
                .getSavedListCandidates(savedListId, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildPage(candidates);
    }

    @PostMapping(value = "{id}/export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void export(
            @PathVariable("id") long savedListId,
            @Valid  @RequestBody SavedListGetRequest request,
            HttpServletResponse response) throws IOException, ExportFailedException {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "candidates.csv\"");
        response.setContentType("text/csv; charset=utf-8");
        candidateService.exportToCsv(savedListId, request, response.getWriter());
    }

    @PutMapping(value = "{id}/update-sf")
    public void createUpdateSalesforce(
            @PathVariable("id") long savedListId) 
            throws NoSuchObjectException, GeneralSecurityException {
        savedListService.createUpdateSalesforce(savedListId);
    }
    
}
