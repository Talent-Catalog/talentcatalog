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
import java.util.Map;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateVisaJobCheck;
import org.tctalent.server.request.candidate.visa.job.CreateCandidateVisaJobCheckRequest;
import org.tctalent.server.service.db.CandidateVisaJobCheckService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate-visa-job")
public class CandidateVisaJobCheckAdminApi
        implements IJoinedTableApi<CreateCandidateVisaJobCheckRequest,
        CreateCandidateVisaJobCheckRequest, CreateCandidateVisaJobCheckRequest> {
    private final CandidateVisaJobCheckService candidateVisaJobCheckService;
    private final OccupationService occupationService;

    public CandidateVisaJobCheckAdminApi(CandidateVisaJobCheckService candidateVisaJobCheckService,
        OccupationService occupationService) {
        this.candidateVisaJobCheckService = candidateVisaJobCheckService;
        this.occupationService = occupationService;
    }

    /**
     * Gets visa job check record using the visa job check ID
     * @param visaJobId ID of visa job check
     * @return Desired record
     * @throws NoSuchObjectException if if the there is no visa job check record with that id
     */
    @Override
    public @NotNull Map<String, Object> get(long visaJobId)
            throws NoSuchObjectException {
        CandidateVisaJobCheck candidateVisaJobCheck = this.candidateVisaJobCheckService.getVisaJobCheck(visaJobId);
        return candidateVisaJobDto().build(candidateVisaJobCheck);
    }

    /**
     * Creates a new candidate visa check record from the data in the given
     * request.
     * @param visaId ID of visa
     * @param request Request containing visa check details
     * @return Created record - including database id of visa check record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no country with the id given in the request
     */
    @Override
    public @NotNull Map<String, Object> create(
            long visaId, @Valid CreateCandidateVisaJobCheckRequest request)
            throws NoSuchObjectException {
        CandidateVisaJobCheck candidateVisaJobCheck = this.candidateVisaJobCheckService.createVisaJobCheck(visaId, request);
        return candidateVisaJobDto().build(candidateVisaJobCheck);
    }

    /**
     * Delete the candidate visa check with the given id.
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    @Override
    public boolean delete(long id)
            throws EntityReferencedException, InvalidRequestException {
        return candidateVisaJobCheckService.deleteVisaJobCheck(id);
    }

    private DtoBuilder candidateVisaJobDto() {
        return new DtoBuilder()
                .add("id")
                .add("jobOpp", jobOppDto())
                .add("interest")
                .add("interestNotes")
                .add("regional")
                .add("salaryTsmit")
                .add("interest")
                .add("interestNotes")
                .add("qualification")
                .add("eligible_494")
                .add("eligible_494_Notes")
                .add("eligible_186")
                .add("eligible_186_Notes")
                .add("eligibleOther")
                .add("eligibleOtherNotes")
                .add("putForward")
                .add("tbbEligibility")
                .add("notes")
                .add("occupation", occupationService.selectBuilder())
                .add("occupationNotes")
                .add("qualificationNotes")
                .add("relevantWorkExp")
                .add("ageRequirement")
                .add("preferredPathways")
                .add("ineligiblePathways")
                .add("eligiblePathways")
                .add("occupationCategory")
                .add("occupationSubCategory")
                .add("englishThreshold")
                .add("languagesRequired")
                .add("languagesThresholdMet")
                .add("languagesThresholdNotes")
                ;
    }

    private DtoBuilder jobOppDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("sfId")
                .add("jobOppIntake", joiDto())
                ;
    }

    private DtoBuilder joiDto() {
        return new DtoBuilder()
                .add("id")
                .add("location")
                .add("locationDetails")
                ;
    }

}
