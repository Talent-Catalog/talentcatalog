/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.api.admin;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.request.candidate.HasSetOfSavedListsImpl;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.util.dto.DtoBuilder;

/**
 * Web API for managing the association between a Candidate and its associated
 * SavedLists's.
 * <p/>
 * This API:
 * <ul>
 *     <li>Updates the saved lists that a candidates can belong to</li>
 *     <li>Can request all saved lists or a subset of saved lists (based on
 *     search criteria) that a candidate belongs to</li>
 * </ul>
 * <p/>
 * For actually modifying candidate details - see {@link CandidateAdminApi}, 
 * or for modifying saved list details see {@link SavedListAdminApi}. 
 * <p/> 
 * See also {@link SavedListCandidateAdminApi} which is the mirror image,
 * managing the reverse association between a SavedList and its associated
 * Candidate's.
 *
 */
@RestController()
@RequestMapping("/api/admin/candidate-saved-list")
public class CandidateSavedListAdminApi implements IManyToManyApi<SearchSavedListRequest, HasSetOfSavedListsImpl> {

    private final CandidateService candidateService;
    private final SavedListService savedListService;
    private final SavedListBuilderSelector builderSelector = new SavedListBuilderSelector();

    @Autowired
    public CandidateSavedListAdminApi(
            CandidateService candidateService,
            SavedListService savedListService) {
        this.candidateService = candidateService;
        this.savedListService = savedListService;
    }

    @Override
    public void replace(long candidateId, @Valid HasSetOfSavedListsImpl request) 
            throws NoSuchObjectException {
        candidateService.replaceCandidateSavedLists(candidateId, request);
    }

    @Override
    public @NotNull List<Map<String, Object>> search(
            long candidateId, @Valid SearchSavedListRequest request) 
            throws NoSuchObjectException {
        List<SavedList> savedLists = savedListService.search(candidateId, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildList(savedLists);
    }
}
