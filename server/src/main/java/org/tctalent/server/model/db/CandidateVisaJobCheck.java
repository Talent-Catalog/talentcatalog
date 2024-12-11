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

package org.tctalent.server.model.db;

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.tctalent.server.request.candidate.visa.CandidateVisaCheckData;

@Getter
@Setter
@Entity
@Table(name = "candidate_visa_job_check")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_visa_job_check_id_seq", allocationSize = 1)
public class CandidateVisaJobCheck extends CandidateVisaJobCheckBase {

    public void populateIntakeData(
            @Nullable Occupation occupation, CandidateVisaCheckData data) {
        if (data.getVisaJobOccupationId() != null) {
            setOccupation(occupation);
        }
        if (data.getVisaJobOccupationNotes() != null) {
            setOccupationNotes(data.getVisaJobOccupationNotes());
        }
        if (data.getVisaJobQualification() != null) {
            setQualification(data.getVisaJobQualification());
        }
        if (data.getVisaJobQualificationNotes() != null) {
            setQualificationNotes(data.getVisaJobQualificationNotes());
        }
        if (data.getVisaJobSalaryTsmit() != null) {
            setSalaryTsmit(data.getVisaJobSalaryTsmit());
        }
        if (data.getVisaJobRegional() != null) {
            setRegional(data.getVisaJobRegional());
        }
        if (data.getVisaJobInterest() != null) {
            setInterest(data.getVisaJobInterest());
        }
        if (data.getVisaJobInterestNotes() != null) {
            setInterestNotes(data.getVisaJobInterestNotes());
        }
        if (data.getVisaJobEligible494() != null) {
            setEligible_494(data.getVisaJobEligible494());
        }
        if (data.getVisaJobEligible494Notes() != null) {
            setEligible_494_Notes(data.getVisaJobEligible494Notes());
        }
        if (data.getVisaJobEligible186() != null) {
            setEligible_186(data.getVisaJobEligible186());
        }
        if (data.getVisaJobEligible186Notes() != null) {
            setEligible_186_Notes(data.getVisaJobEligible186Notes());
        }
        if (data.getVisaJobEligibleOther() != null) {
            setEligibleOther(data.getVisaJobEligibleOther());
        }
        if (data.getVisaJobEligibleOtherNotes() != null) {
            setEligibleOtherNotes(data.getVisaJobEligibleOtherNotes());
        }
        if (data.getVisaJobPutForward() != null) {
            setPutForward(data.getVisaJobPutForward());
        }
        if (data.getVisaJobNotes() != null) {
            setNotes(data.getVisaJobNotes());
        }
        if (data.getVisaJobTbbEligibility() != null) {
            setTbbEligibility(data.getVisaJobTbbEligibility());
        }
        if (data.getVisaJobRelevantWorkExp() != null) {
            setRelevantWorkExp(data.getVisaJobRelevantWorkExp());
        }
        if (data.getVisaJobAgeRequirement() != null) {
            setAgeRequirement(data.getVisaJobAgeRequirement());
        }
        if (data.getVisaJobPreferredPathways() != null) {
            setPreferredPathways(data.getVisaJobPreferredPathways());
        }
        if (data.getVisaJobIneligiblePathways() != null) {
            setIneligiblePathways(data.getVisaJobIneligiblePathways());
        }
        if (data.getVisaJobEligiblePathways() != null) {
            setEligiblePathways(data.getVisaJobEligiblePathways());
        }
        if (data.getVisaJobOccupationCategory() != null) {
            setOccupationCategory(data.getVisaJobOccupationCategory());
        }
        if (data.getVisaJobOccupationSubCategory() != null) {
            setOccupationSubCategory(data.getVisaJobOccupationSubCategory());
        }
        if (data.getVisaJobEnglishThreshold() != null) {
            setEnglishThreshold(data.getVisaJobEnglishThreshold());
        }
        if (data.getVisaJobLanguagesRequired() != null) {
            setLanguagesRequired(data.getVisaJobLanguagesRequired());
        }
        if (data.getVisaJobLanguagesThresholdMet() != null) {
            setLanguagesThresholdMet(data.getVisaJobLanguagesThresholdMet());
        }
        if (data.getVisaJobLanguagesThresholdNotes() != null) {
            setLanguagesThresholdNotes(data.getVisaJobLanguagesThresholdNotes());
        }

    }

}
