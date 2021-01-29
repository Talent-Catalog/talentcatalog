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
        candidateService.clearCandidateSavedLists(candidateId);
        candidateService.mergeCandidateSavedLists(candidateId, request);
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
