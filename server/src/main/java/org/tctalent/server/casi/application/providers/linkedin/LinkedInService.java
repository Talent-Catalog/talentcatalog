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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.importers.FileInventoryImporter;
import org.tctalent.server.casi.core.services.AbstractCandidateAssistanceService;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedListService;

@Service
public class LinkedInService extends AbstractCandidateAssistanceService {
    private final FileInventoryImporter linkedInImporter;
    private final ResourceAllocator linkedInAllocator;
    private final CandidateService candidateService;

    private static final Set<Long> LINKEDIN_ELIGIBLE_LIST_IDS = Set.of(12608L, 12609L, 12610L,
        12611L, 12612L, 12613L, 12614L, 12615L, 12616L, 12617L, 12618L, 12619L, 12620L, 12621L,12622L);
    private static final Long LINKEDIN_ISSUE_REPORT_LIST_ID = 12623L;
    private static final Long LINKEDIN_ASSIGNMENT_FAILURE_LIST_ID = 12625L;

    public LinkedInService(
        ServiceAssignmentRepository aRepo,
        ServiceResourceRepository rRepo,
        AssignmentEngine engine,
        SavedListService lists,
        CandidateService candidateService,
        @Qualifier("linkedInCouponImporter") FileInventoryImporter importer,
        @Qualifier("linkedInPremiumMembershipAllocator") ResourceAllocator allocator
    ) {
        super(aRepo, rRepo, engine, lists);
        this.linkedInAllocator = allocator;
        this.linkedInImporter = importer;
        this.candidateService = candidateService;
    }

    /**
     * Checks if a candidate is eligible for the LinkedIn Premium membership upgrade offer by
     * comparing IDs of their associated Lists against Lists used to tag eligible candidates.
     *
     * @param candidateId
     * @return true if the candidate is eligible
     */
    public Boolean isEligible(Long candidateId) {
        SearchSavedListRequest request = new SearchSavedListRequest();
        List<SavedList> candidateLists = savedListService.search(candidateId, request);

        return candidateLists.stream()
            .anyMatch(list -> LINKEDIN_ELIGIBLE_LIST_IDS.contains(list.getId()));
    }

    /**
     * Returns the candidate's assignment to an RESERVED resource, or a REDEEMED resource
     * if no RESERVED one exists, or null if neither is found.
     *
     * @param candidateId
     * @return {@link ServiceAssignment} or null if none found
     */
    @Transactional(readOnly = true)
    public ServiceAssignment findAssignmentWithReservedOrRedeemedResource(Long candidateId) {
        List<ServiceAssignment> assignments = getAssignmentsForCandidate(candidateId);

        return assignments.stream()
            .filter(a -> a.getResource().getStatus() == ResourceStatus.RESERVED)
            .findFirst()
            .orElseGet(() -> assignments.stream()
                .filter(a -> a.getResource().getStatus() == ResourceStatus.REDEEMED)
                .findFirst()
                .orElse(null)
            );
    }

    /**
     * Adds the candidate associated with the given {@link ServiceAssignment} to the
     * #LinkedInIssueReport List, along with a note containing the coupon code, assignment status,
     * assignment date, and the candidate's description of the issue.
     *
     * @param assignment   the {@link ServiceAssignment} containing candidate and coupon details
     * @param issueComment free-text description of the issue provided by the candidate
     */
    public void addCandidateToIssueReportList(ServiceAssignment assignment, String issueComment) {
        SavedList list = savedListService.get(LINKEDIN_ISSUE_REPORT_LIST_ID);
        Candidate candidate = candidateService.getCandidate(assignment.getCandidateId());
        savedListService.addCandidateToList(
            list,
            candidate,
            "Coupon code: %s | Assignment status: %s | First assigned at: %s | Comment: %s".formatted(
                assignment.getResource().getResourceCode(),
                assignment.getStatus(),
                assignment.getAssignedAt(),
                issueComment
            )
        );
        savedListService.saveIt(list);
    }

    /**
     * Adds the given candidate to the #LinkedInAssignmentFailure List, recording as a context note
     * the exception that caused the failure. Runs in a separate transaction to ensure the failure
     * is saved even if the main assignment transaction rolls back.
     *
     * @param candidateId
     * @param exception - exception that caused the assignment to fail
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addCandidateToAssignmentFailureList(Long candidateId, Exception exception) {
        SavedList list = savedListService.get(LINKEDIN_ASSIGNMENT_FAILURE_LIST_ID);
        Candidate candidate = candidateService.getCandidate(candidateId);

        savedListService.addCandidateToList(list, candidate, exception.toString());
        savedListService.saveIt(list);
    }

    /**
     * Checks if a candidate is on the #LinkedInIssueReport List.
     *
     * @param candidateId
     * @return true if the candidate is on the List
     */
    public Boolean isOnIssueReportList(Long candidateId) {
        SearchSavedListRequest request = new SearchSavedListRequest();
        List<SavedList> candidateLists = savedListService.search(candidateId, request);

        return candidateLists.stream()
            .anyMatch(list -> list.getId().equals(LINKEDIN_ISSUE_REPORT_LIST_ID));
    }

    /**
     * Checks if a candidate is on the #LinkedInAssignmentFailure List.
     *
     * @param candidateId
     * @return true if the candidate is on the List
     */
    public Boolean isOnAssignmentFailureList(Long candidateId) {
        SearchSavedListRequest request = new SearchSavedListRequest();
        List<SavedList> candidateLists = savedListService.search(candidateId, request);

        return candidateLists.stream()
            .anyMatch(list -> list.getId().equals(LINKEDIN_ASSIGNMENT_FAILURE_LIST_ID));
    }

    @Override protected ServiceProvider provider() { return ServiceProvider.LINKEDIN; }
    @Override protected ServiceCode serviceCode() { return ServiceCode.PREMIUM_MEMBERSHIP; }
    @Override protected ResourceAllocator allocator() { return linkedInAllocator; }
    @Override protected FileInventoryImporter importer() { return linkedInImporter; }
}
