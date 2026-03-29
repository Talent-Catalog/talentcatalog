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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.casi.core.allocators.ResourceAllocator;
import org.tctalent.server.casi.core.importers.FileInventoryImporter;
import org.tctalent.server.casi.core.services.AbstractCandidateAssistanceService;
import org.tctalent.server.casi.core.services.AssignmentEngine;
import org.tctalent.server.casi.core.services.ServiceListSpec;
import org.tctalent.server.casi.domain.model.ListAction;
import org.tctalent.server.casi.domain.model.ListRole;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceAssignment;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.casi.domain.persistence.ServiceAssignmentRepository;
import org.tctalent.server.casi.domain.persistence.ServiceListEntity;
import org.tctalent.server.casi.domain.persistence.ServiceListRepository;
import org.tctalent.server.casi.domain.persistence.ServiceResourceRepository;
import org.tctalent.server.exception.NoSuchObjectException;
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
    private final ServiceListRepository serviceListRepository;

    private static final int ELIGIBILITY_LIST_COUNT = 15;

    public LinkedInService(
        ServiceAssignmentRepository aRepo,
        ServiceResourceRepository rRepo,
        AssignmentEngine engine,
        SavedListService lists,
        CandidateService candidateService,
        ServiceListRepository serviceListRepository,
        @Qualifier("linkedInCouponImporter") FileInventoryImporter importer,
        @Qualifier("linkedInPremiumMembershipAllocator") ResourceAllocator allocator
    ) {
        super(aRepo, rRepo, engine, lists);
        this.linkedInAllocator = allocator;
        this.linkedInImporter = importer;
        this.candidateService = candidateService;
        this.serviceListRepository = serviceListRepository;
    }

    @Override
    public List<ServiceListSpec> serviceListSpecs() {
        return Stream.concat(
            Stream.of(
                new ServiceListSpec(
                    "#LinkedInIssueReports",
                    ListRole.USER_ISSUE_REPORT,
                    Set.of(ListAction.REASSIGN)
                ),
                new ServiceListSpec(
                    "#LinkedInAssignmentFailures",
                    ListRole.ASSIGNMENT_FAILURE,
                    Set.of(ListAction.REASSIGN)
                )
            ),
            IntStream.rangeClosed(1, ELIGIBILITY_LIST_COUNT)
                .mapToObj(i -> new ServiceListSpec(
                    "#LinkedInEligible" + i,
                    ListRole.SERVICE_ELIGIBILITY,
                    Set.of()
                ))
        ).toList();
    }

    /**
     * Checks if a candidate is eligible for the LinkedIn Premium membership upgrade offer by
     * comparing IDs of their associated Lists against Lists used to tag eligible candidates.
     *
     * @param candidateId
     * @return true if the candidate is eligible
     */
    public Boolean isEligible(Long candidateId) {
        Set<Long> eligibilityListIds = serviceListRepository
            .findByProviderAndServiceCodeAndRole(
                ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP, ListRole.SERVICE_ELIGIBILITY)
            .stream()
            .map(e -> e.getSavedList().getId())
            .collect(Collectors.toSet());

        List<SavedList> candidateLists = savedListService.search(candidateId, new SearchSavedListRequest());
        return candidateLists.stream().anyMatch(list -> eligibilityListIds.contains(list.getId()));
    }

    /**
     * Returns the candidate's assignment to a RESERVED resource, or a REDEEMED resource
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
     * and assignment date.
     *
     * @param assignment the {@link ServiceAssignment} containing candidate and coupon details
     */
    public void addCandidateToIssueReportList(ServiceAssignment assignment) {
        SavedList list = getServiceList(ListRole.USER_ISSUE_REPORT);
        Candidate candidate = candidateService.getCandidate(assignment.getCandidateId());
        savedListService.addCandidateToList(
            list,
            candidate,
            "Coupon code: %s | Assignment status: %s | First assigned at: %s".formatted(
                assignment.getResource().getResourceCode(),
                assignment.getStatus(),
                assignment.getAssignedAt()
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
        SavedList list = getServiceList(ListRole.ASSIGNMENT_FAILURE);
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
        long listId = getServiceList(ListRole.USER_ISSUE_REPORT).getId();
        List<SavedList> candidateLists = savedListService.search(candidateId, new SearchSavedListRequest());
        return candidateLists.stream().anyMatch(list -> list.getId().equals(listId));
    }

    /**
     * Checks if a candidate is on the LinkedIn Assignment Failure list.
     *
     * @param candidateId
     * @return true if the candidate is on the list
     */
    public Boolean isOnAssignmentFailureList(Long candidateId) {
        long listId = getServiceList(ListRole.ASSIGNMENT_FAILURE).getId();
        List<SavedList> candidateLists = savedListService.search(candidateId, new SearchSavedListRequest());
        return candidateLists.stream().anyMatch(list -> list.getId().equals(listId));
    }

    @Override public ServiceProvider provider() { return ServiceProvider.LINKEDIN; }
    @Override public ServiceCode serviceCode() { return ServiceCode.PREMIUM_MEMBERSHIP; }
    @Override protected ResourceAllocator allocator() { return linkedInAllocator; }
    @Override protected FileInventoryImporter importer() { return linkedInImporter; }

    private SavedList getServiceList(ListRole role) {
        return serviceListRepository
            .findFirstByProviderAndServiceCodeAndRole(
                ServiceProvider.LINKEDIN, ServiceCode.PREMIUM_MEMBERSHIP, role)
            .map(ServiceListEntity::getSavedList)
            .orElseThrow(() -> new NoSuchObjectException(
                "No service list registered for LinkedIn role: " + role));
    }
}
