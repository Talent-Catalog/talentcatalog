/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    //todo remove english threshold field, replaced with languagesThresholdMet field
    @Enumerated(EnumType.STRING)
    private YesNo englishThreshold;

    @Convert(converter = DelimitedIdConverter.class)
    private List<Long> languagesRequired;

    @Enumerated(EnumType.STRING)
    private YesNo languagesThresholdMet;

    private String languagesThresholdNotes;

    /**
     * String of the ids of the candidate dependants that are relocating as part of the visa job check.
     * This is a simple string of ids to avoid a lengthy and unnecessary many-to-many relationship,
     * as we don't need to track the inverse relationship of dependants and their associated visa job checks.
     * It is a string as opposed to a List of ids due to the error: ''Basic' attribute type should not be a container'
     */
    private String relocatingDependantIds;

    /**
     * Get the string of relocating dependant ids and convert to a comma separated list of ids(long).
     * @return List of candidate dependant ids
     */
    public List<Long> getRelocatingDependantIds() {
        return relocatingDependantIds != null ?
            Stream.of(relocatingDependantIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList()) : null;
    }

    /**
     * Set the list of ids (long) to a string of ids comma separated to save to database.
     */
    public void setRelocatingDependantIds(List<Long> relocatingDependantIds) {
        this.relocatingDependantIds = !CollectionUtils.isEmpty(relocatingDependantIds) ?
            relocatingDependantIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) : null;
    }
}
