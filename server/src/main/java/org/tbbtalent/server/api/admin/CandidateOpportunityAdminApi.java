/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.CandidateOpportunity;
import org.tbbtalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tbbtalent.server.request.candidate.opportunity.SearchCandidateOpportunityRequest;
import org.tbbtalent.server.service.db.CandidateOpportunityService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/opp")
public class CandidateOpportunityAdminApi implements
    ITableApi<SearchCandidateOpportunityRequest, CandidateOpportunityParams, CandidateOpportunityParams> {

    private final CandidateOpportunityService candidateOpportunityService;
    public CandidateOpportunityAdminApi(CandidateOpportunityService candidateOpportunityService) {
        this.candidateOpportunityService = candidateOpportunityService;
    }

    @Override
    @GetMapping("{id}")
    public @NotNull Map<String, Object> get(long id) throws NoSuchObjectException {
        CandidateOpportunity opp = candidateOpportunityService.getCandidateOpportunity(id);
        return candidateOpportunityDto().build(opp);
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
    public @NotNull Map<String, Object> update(long id, CandidateOpportunityParams request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        CandidateOpportunity opp = candidateOpportunityService.updateCandidateOpportunity(id, request);
        return candidateOpportunityDto().build(opp);
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
            .add("stage")
            .add("createdBy", shortUserDto())
            .add("createdDate")
            .add("updatedBy", shortUserDto())
            .add("updatedDate")
            ;
    }
    
    private DtoBuilder shortUserDto() {
        return new DtoBuilder()
            .add("username")
            .add("email")
            .add("firstName")
            .add("lastName")
            ;
    }

    private DtoBuilder shortCandidateDto() {
        return new DtoBuilder()
            .add("candidateNumber")
            ;
    }

    private DtoBuilder shortJobDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("country")
            .add("submissionList", shortSavedListDto())
            ;
    }

    private DtoBuilder shortSavedListDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }

}
