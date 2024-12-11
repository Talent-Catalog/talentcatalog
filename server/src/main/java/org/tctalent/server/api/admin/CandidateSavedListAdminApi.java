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

package org.tctalent.server.api.admin;

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.candidate.HasSetOfSavedListsImpl;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.util.dto.DtoBuilder;

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
@RequiredArgsConstructor
public class CandidateSavedListAdminApi implements IManyToManyApi<SearchSavedListRequest, HasSetOfSavedListsImpl> {

    private final CandidateSavedListService candidateSavedListService;
    private final SavedListService savedListService;
    private final SavedListBuilderSelector builderSelector = new SavedListBuilderSelector();

    @Override
    public void replace(long candidateId, @Valid HasSetOfSavedListsImpl request)
            throws NoSuchObjectException {
        candidateSavedListService.clearCandidateSavedLists(candidateId);
        candidateSavedListService.mergeCandidateSavedLists(candidateId, request);
    }

    @Override
    public @NotNull List<Map<String, Object>> search(
            long candidateId, @Valid SearchSavedListRequest request)
            throws NoSuchObjectException {
        List<SavedList> savedLists = savedListService.search(candidateId, request);
        DtoBuilder builder = builderSelector.selectBuilder(request.getDtoType());
        return builder.buildList(savedLists);
    }
}
