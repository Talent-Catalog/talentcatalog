/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import static org.tctalent.server.configuration.SystemAdminConfiguration.PENDING_TERMS_ACCEPTANCE_LIST_ID;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.mapper.CandidateAttachmentMapper;
import org.tctalent.server.repository.db.read.dto.CandidateAttachmentReadDto;
import org.tctalent.server.repository.db.read.dto.CandidateDependantReadDto;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.service.db.CandidateDtoService;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.SavedListService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateDtoServiceImpl implements CandidateDtoService {
    private final CandidateSavedListService candidateSavedListService;
    private final SavedListService savedListService;
    private final CandidateAttachmentMapper attachmentMapper;

    public void populateComputedFields(
        Iterable<CandidateReadDto> candidates, @Nullable Long savedListId) {

        //Get all CandidateSavedList objects in a single db access.
        Set<Long> ids = new HashSet<>();
        for (CandidateReadDto candidate : candidates) {
            ids.add(candidate.getId());
        }
        Map<Long, CandidateSavedList> listContexts = savedListId == null ? Map.of() :
            candidateSavedListService.findByCandidateIds(ids, savedListId);

        //Load all candidates in the pending terms list
        Set<Long> pendingTermsIds = savedListService.fetchCandidateIds(PENDING_TERMS_ACCEPTANCE_LIST_ID);

        //Populate the computed fields
        for (CandidateReadDto candidate : candidates) {
            //Retrieve list context for this candidate
            CandidateSavedList listContext = listContexts.get(candidate.getId());

            //If the candidate is in the list then we need to retrieve the list dependant context.
            if (listContext != null) {
                //Set any context note
                candidate.setContextNote(listContext.getContextNote());

                //Need to retrieve the list dependent attachments if they exist
                candidate.setListShareableCv(createDto(listContext.getShareableCv()));
                candidate.setListShareableDoc(createDto(listContext.getShareableDoc()));
            }

            //Check whether the candidate is in the pending terms list
            candidate.setPendingTerms(pendingTermsIds.contains(candidate.getId()));

            //Compute the number of dependants
            final List<CandidateDependantReadDto> dependants = candidate.getCandidateDependants();
            candidate.setNumberDependants(dependants == null ? 0 : dependants.size());
        }
    }

    @Nullable
    private CandidateAttachmentReadDto createDto(@Nullable CandidateAttachment attachment) {
        //TODO JC Will this copy the createdBy user?
        return attachment == null ? null : attachmentMapper.toDto(attachment);
    }
}
