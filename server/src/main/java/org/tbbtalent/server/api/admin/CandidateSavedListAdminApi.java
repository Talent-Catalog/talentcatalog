/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.api.admin;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.request.candidate.IHasSetOfSavedLists;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
import org.tbbtalent.server.service.SavedListService;
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
public class CandidateSavedListAdminApi implements IManyToManyApi<SearchSavedListRequest, IHasSetOfSavedLists> {

    private final SavedListService savedListService;
    private final SavedListBuilderSelector builderSelector = new SavedListBuilderSelector();

    @Autowired
    public CandidateSavedListAdminApi(
            SavedListService savedListService) {
        this.savedListService = savedListService;
    }

    @Override
    public List<Map<String, Object>> search(
            long masterId, @Valid SearchSavedListRequest request) {
        List<SavedList> savedLists = savedListService.search(masterId, request);
        DtoBuilder builder = builderSelector.selectBuilder();
        return builder.buildList(savedLists);
    }
}
