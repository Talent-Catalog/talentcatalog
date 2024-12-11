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

import java.util.List;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class CandidateVisaJobCheckBase extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_visa_check_id")
    private CandidateVisaCheck candidateVisaCheck;

    /**
     * Associated job opportunity
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opp_id")
    SalesforceJobOpp jobOpp;

    @Enumerated(EnumType.STRING)
    private YesNo interest;

    private String interestNotes;

    @Enumerated(EnumType.STRING)
    private YesNo qualification;

    private String qualificationNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_id")
    private Occupation occupation;

    private String occupationNotes;

    @Enumerated(EnumType.STRING)
    private YesNo salaryTsmit;

    @Enumerated(EnumType.STRING)
    private YesNo regional;

    @Enumerated(EnumType.STRING)
    private YesNo eligible_494;

    private String eligible_494_Notes;

    @Enumerated(EnumType.STRING)
    private YesNo eligible_186;

    private String eligible_186_Notes;

    @Enumerated(EnumType.STRING)
    private OtherVisas eligibleOther;

    private String eligibleOtherNotes;

    @Enumerated(EnumType.STRING)
    private VisaEligibility putForward;

    @Enumerated(EnumType.STRING)
    private TBBEligibilityAssessment tbbEligibility;

    private String notes;

    private String relevantWorkExp;

    private String ageRequirement;

    private String preferredPathways;

    private String ineligiblePathways;

    private String eligiblePathways;

    private String occupationCategory;

    private String occupationSubCategory;

    //todo remove english threshold field, replaced with languagesThresholdMet field
    @Enumerated(EnumType.STRING)
    private YesNo englishThreshold;

    @Convert(converter = DelimitedIdConverter.class)
    private List<Long> languagesRequired;

    @Enumerated(EnumType.STRING)
    private YesNo languagesThresholdMet;

    private String languagesThresholdNotes;

    /**
     * Gets the candidate to whom the given instance of candidate job visa assessment refers, by
     * querying the parent candidate visa check.
     * @return candidate associated with given candidate job visa check
     */
    public Candidate getCandidate() {
        Candidate candidate = this.getCandidateVisaCheck().getCandidate();
        return candidate;
    }
}
