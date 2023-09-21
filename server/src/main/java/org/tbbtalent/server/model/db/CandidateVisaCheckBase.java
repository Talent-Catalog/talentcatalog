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

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@MappedSuperclass
public class CandidateVisaCheckBase extends AbstractAuditableDomainObject<Long> implements Comparable<CandidateVisaCheck> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Enumerated(EnumType.STRING)
    private YesNo protection;

    private String protectionGrounds;

    @Enumerated(EnumType.STRING)
    private YesNo englishThreshold;

    private String englishThresholdNotes;

    @Enumerated(EnumType.STRING)
    private YesNo healthAssessment;

    private String healthAssessmentNotes;

    @Enumerated(EnumType.STRING)
    private YesNo characterAssessment;

    private String characterAssessmentNotes;

    @Enumerated(EnumType.STRING)
    private YesNo securityRisk;

    private String securityRiskNotes;

    @Enumerated(EnumType.STRING)
    private RiskLevel overallRisk;

    private String overallRiskNotes;

    @Enumerated(EnumType.STRING)
    private DocumentStatus validTravelDocs;

    private String validTravelDocsNotes;

    private String assessmentNotes;

    @Enumerated(EnumType.STRING)
    private YesNoUnsure pathwayAssessment;

    private String pathwayAssessmentNotes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidateVisaCheck", cascade = CascadeType.MERGE)
    private Set<CandidateVisaJobCheck> candidateVisaJobChecks = new HashSet<>();

    public int compareTo(CandidateVisaCheck o) {
        if (country == null) {
            return o.getCountry() == null ? 0 : -1;
        }
        return country.compareTo(o.getCountry());
    }
}
