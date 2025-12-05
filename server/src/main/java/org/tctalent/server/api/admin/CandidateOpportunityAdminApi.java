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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.api.dto.DtoType;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.JobChatUserInfo;
import org.tctalent.server.request.candidate.dependant.UpdateRelocatingDependantIds;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/admin/opp")
@RequiredArgsConstructor
public class CandidateOpportunityAdminApi implements ITableApi<SearchCandidateOpportunityRequest,
                                                               CandidateOpportunityParams,
                                                               CandidateOpportunityParams> {

    private final CandidateOpportunityService candidateOpportunityService;
    private final CountryService countryService;
    private final SalesforceService salesforceService;

    @Override
    public @NotNull Map<String, Object> get(long id, DtoType dtoType) throws NoSuchObjectException {
        CandidateOpportunity opp = candidateOpportunityService.getCandidateOpportunity(id);
        return candidateOpportunityDto().build(opp);
    }

    @PostMapping("check-unread-chats")
    public @NotNull JobChatUserInfo checkUnreadChats(
        @Valid @RequestBody SearchCandidateOpportunityRequest request) {
        List<Long> oppIds = candidateOpportunityService.findUnreadChatsInOpps(request);
        JobChatUserInfo info = new JobChatUserInfo();
        info.setNumberUnreadChats(oppIds.size());
        return info;
    }

    @Override
    @PostMapping("search-paged")
    public @NotNull Map<String, Object> searchPaged(@Valid SearchCandidateOpportunityRequest request) {
        Page<CandidateOpportunity> opps = candidateOpportunityService.searchCandidateOpportunities(request);
        final Map<String, Object> objectMap = candidateOpportunityDto().buildPage(opps);
        return objectMap;
    }

    @Override
    @PutMapping("{id}")
    public @NotNull Map<String, Object> update(@PathVariable("id") long id, CandidateOpportunityParams request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        CandidateOpportunity opp = candidateOpportunityService.updateCandidateOpportunity(id, request);
        return candidateOpportunityDto().build(opp);
    }

    @PostMapping("{id}/upload-offer")
    public @NotNull Map<String, Object> uploadOffer(
        @PathVariable("id") long id, @RequestParam("file") MultipartFile file)
        throws InvalidRequestException, IOException, NoSuchObjectException {
        CandidateOpportunity opp = candidateOpportunityService.uploadOffer(id, file);
        return candidateOpportunityDto().build(opp);
    }

    @PutMapping("{id}/update-sf-case-relocation-info")
    public void updateSfCaseRelocationInfo(@PathVariable("id") long id)
        throws NoSuchObjectException, SalesforceException, WebClientException {
        CandidateOpportunity candidateOpportunity = candidateOpportunityService.getCandidateOpportunity(id);
        salesforceService.updateSfCaseRelocationInfo(candidateOpportunity);
    }

    @PutMapping("{id}/relocating-dependants")
    public void updateRelocatingDependants(@PathVariable("id") long id,
        @RequestBody UpdateRelocatingDependantIds request)
        throws NoSuchObjectException, SalesforceException, WebClientException {
        CandidateOpportunity candidateOpportunity =
            candidateOpportunityService.updateRelocatingDependants(id, request);
    }

    private DtoBuilder candidateOpportunityDto() {
        return new DtoBuilder()
            .add("id")
            .add("sfId")
            .add("candidate", shortCandidateDto())
            .add("closed")
            .add("closingComments")
            .add("closingCommentsForCandidate")
            .add("employerFeedback")
            .add("jobOpp", shortJobDto())
            .add("name")
            .add("nextStep")
            .add("nextStepDueDate")
            .add("lastActiveStage")
            .add("stage")
            .add("createdBy", shortUserDto())
            .add("createdDate")
            .add("updatedBy", shortUserDto())
            .add("updatedDate")
            .add("fileOfferLink")
            .add("fileOfferName")
            .add("relocatingDependantIds")
            ;
    }

    private DtoBuilder shortUserDto() {
        return new DtoBuilder()
            .add("username")
            .add("email")
            .add("firstName")
            .add("lastName")
            .add("partner", shortPartnerDto())
            ;
    }

    private DtoBuilder shortPartnerDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("abbreviation")
            .add("websiteUrl")
            ;
    }

    private DtoBuilder shortCandidateDto() {
        return new DtoBuilder()
            .add("id")
            .add("candidateNumber")
            .add("publicId")
            .add("user", shortUserDto())
            ;
    }

    private DtoBuilder shortJobDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("country", countryService.selectBuilder())
            .add("submissionList", shortSavedListDto())
            .add("jobCreator", shortPartnerDto())
            ;
    }

    private DtoBuilder shortSavedListDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }
}
