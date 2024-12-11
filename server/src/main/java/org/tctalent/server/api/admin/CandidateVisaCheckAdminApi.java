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
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.CandidateVisaCheck;
import org.tctalent.server.request.candidate.visa.CandidateVisaCheckData;
import org.tctalent.server.request.candidate.visa.CreateCandidateVisaCheckRequest;
import org.tctalent.server.service.db.CandidateVisaService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate-visa-check")
public class CandidateVisaCheckAdminApi
        implements IJoinedTableApi<CreateCandidateVisaCheckRequest,
        CreateCandidateVisaCheckRequest, CreateCandidateVisaCheckRequest> {
    private final CandidateVisaService candidateVisaService;
    private final CountryService countryService;
    private final OccupationService occupationService;

    public CandidateVisaCheckAdminApi(
            CandidateVisaService candidateVisaService, CountryService countryService, OccupationService occupationService) {
        this.candidateVisaService = candidateVisaService;
        this.countryService = countryService;
        this.occupationService = occupationService;
    }

    /**
     * Get a new candidate visa check record from the data in the given request.
     * @param visaId ID of visa check
     * @return Created record - including database id of visa check record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no country with the id given in the request
     */
    @Override
    public @NotNull Map<String, Object> get(long visaId)
            throws NoSuchObjectException {
        CandidateVisaCheck candidateVisaCheck = this.candidateVisaService.getVisaCheck(visaId);
        return candidateVisaDto().build(candidateVisaCheck);
    }

    /**
     * List of visa check records belonging to the candidate passed in by the candidateId.
     *
     * @param candidateId ID of candidate whose visa checks we want
     * @return List of visa check records - can be empty if candidate has no visa checks associated.
     */
    @Override
    public @NotNull List<Map<String, Object>> list(long candidateId) {
        List<CandidateVisaCheck> candidateVisaChecks = this.candidateVisaService.listCandidateVisaChecks(candidateId);
        return candidateVisaDto().buildList(candidateVisaChecks);
    }

    /**
     * Populates the visa check object OR visa job check object using the visa data provided.
     * @param id ID of the visa check to be updated directly, or whose associated job check is to be updated.
     * @param data data of the visa intake, only updates the non null fields.
     */
    @PutMapping("{id}/intake")
    public void updateIntakeData(
            @PathVariable("id") long id, @RequestBody CandidateVisaCheckData data) {
        candidateVisaService.updateIntakeData(id, data);
    }

    /**
     * Creates a new candidate visa check record from the data in the given
     * request.
     * @param candidateId ID of candidate
     * @param request Request containing visa check details
     * @return Created record - including database id of visa check record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no country with the id given in the request
     */
    @Override
    public @NotNull Map<String, Object> create(
            long candidateId, @Valid CreateCandidateVisaCheckRequest request)
            throws NoSuchObjectException {
        CandidateVisaCheck candidateVisaCheck =
                this.candidateVisaService
                        .createVisaCheck(candidateId, request);
        return candidateVisaDto().build(candidateVisaCheck);
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
        return candidateVisaService.deleteVisaCheck(id);
    }

    private DtoBuilder candidateVisaDto() {
        return new DtoBuilder()
                .add("id")
                .add("candidateVisaJobChecks", visaJobCheckDto())
                .add("country", countryService.selectBuilder())
                .add("protection")
                .add("protectionGrounds")
                .add("englishThreshold")
                .add("englishThresholdNotes")
                .add("healthAssessment")
                .add("healthAssessmentNotes")
                .add("characterAssessment")
                .add("characterAssessmentNotes")
                .add("securityRisk")
                .add("securityRiskNotes")
                .add("overallRisk")
                .add("overallRiskNotes")
                .add("validTravelDocs")
                .add("validTravelDocsNotes")
                .add("pathwayAssessment")
                .add("pathwayAssessmentNotes")
                .add("destinationFamily")
                .add("destinationFamilyLocation")
                .add("createdBy", userDto())
                .add("createdDate")
                .add("updatedBy", userDto())
                .add("updatedDate")
                ;
    }

    private DtoBuilder visaJobCheckDto() {
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

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
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
