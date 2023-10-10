/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
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

    /**
     * No longer populating these two fields (name & sfJobLink) we used these prior to adding the SF job opps onto the TC,
     * and when Australia was the only country doing their visa checks on the TC.
     * Now we can just use the jobOppId above to retrieve and set job data.
     */
    private String name;
    private String sfJobLink;

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

    @Enumerated(EnumType.STRING)
    private YesNo englishThreshold;

    private String englishThresholdNotes;
}
