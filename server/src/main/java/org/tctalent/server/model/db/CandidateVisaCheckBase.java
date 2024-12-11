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

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

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

    @Enumerated(EnumType.STRING)
    private YesNoUnsure pathwayAssessment;

    private String pathwayAssessmentNotes;

    @Enumerated(EnumType.STRING)
    private FamilyRelations destinationFamily;

    private String destinationFamilyLocation;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidateVisaCheck", cascade = CascadeType.MERGE)
    private Set<CandidateVisaJobCheck> candidateVisaJobChecks = new HashSet<>();

    public int compareTo(CandidateVisaCheck o) {
        if (country == null) {
            return o.getCountry() == null ? 0 : -1;
        }
        return country.compareTo(o.getCountry());
    }
}
