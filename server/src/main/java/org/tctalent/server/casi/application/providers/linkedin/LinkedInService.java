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

package org.tctalent.server.casi.application.providers.linkedin;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.importers.FileInventoryImporter;
import org.tctalent.server.casi.core.services.AbstractCandidateAssistanceService;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.service.db.SavedListService;

@Service
public class LinkedInService extends AbstractCandidateAssistanceService {
    private final FileInventoryImporter linkedInImporter;
    private final ResourceAllocator linkedInAllocator;

    // TODO update for prod:
    private static final Set<Long> LINKEDIN_ELIGIBLE_LIST_IDS = Set.of(641L, 642L);

    public LinkedInService(
        ServiceAssignmentRepository aRepo,
        ServiceResourceRepository rRepo,
        AssignmentEngine engine,
        SavedListService lists,
        @Qualifier("linkedInCouponImporter") FileInventoryImporter importer,
        @Qualifier("linkedInPremiumMembershipAllocator") ResourceAllocator allocator
    ) {
        super(aRepo, rRepo, engine, lists);
        this.linkedInAllocator = allocator;
        this.linkedInImporter = importer;
    }

    /**
     * Checks if a candidate is eligible for the LinkedIn Premium membership upgrade offer by
     * comparing IDs of their associated Lists against Lists used to tag eligible candidates.
     * @param candidateId - ID of candidate
     * @return true if the candidate is eligible
     */
    public Boolean isEligible(Long candidateId) {
        SearchSavedListRequest request = new SearchSavedListRequest();
        List<SavedList> candidateLists = savedListService.search(candidateId, request);

        return candidateLists.stream()
            .anyMatch(list -> LINKEDIN_ELIGIBLE_LIST_IDS.contains(list.getId()));
    }

    /**
     * Checks if a candidate has already redeemed the LinkedIn Premium membership upgrade offer.
     * @param candidateId - ID of candidate
     * @return true if the candidate has already redeemed the offer
     */
    @Transactional(readOnly = true)
    public boolean hasRedeemed(Long candidateId) {
        return getAssignmentsForCandidate(candidateId)
            .stream()
            .anyMatch(assignment -> assignment.getStatus() == AssignmentStatus.REDEEMED);
    }

    @Override protected ServiceProvider provider() { return ServiceProvider.LINKEDIN; }
    @Override protected ServiceCode serviceCode() { return ServiceCode.PREMIUM_MEMBERSHIP; }
    @Override protected ResourceAllocator allocator() { return linkedInAllocator; }
    @Override protected FileInventoryImporter importer() { return linkedInImporter; }
}
