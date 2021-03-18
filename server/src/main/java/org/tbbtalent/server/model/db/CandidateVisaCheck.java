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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "candidate_visa")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_visa_id_seq", allocationSize = 1)
public class CandidateVisaCheck extends AbstractAuditableDomainObject<Long>
        implements Comparable<CandidateVisaCheck> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Enumerated(EnumType.STRING)
    private VisaEligibility eligibility;

    @Enumerated(EnumType.STRING)
    private YesNo protection;
    
    private String protectionGrounds;

    @Enumerated(EnumType.STRING)
    private TBBEligibilityAssessment tbbEligibilityAssessment;

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
    private YesNo validTravelDocs;

    private String validTravelDocsNotes;

    @Enumerated(EnumType.STRING)
    private RiskLevel overallRisk;

    private String overallRiskNotes;

    private String assessmentNotes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidateVisa", cascade = CascadeType.ALL)
    private List<CandidateRoleCheck> candidateRoleChecks;

    public CandidateVisaCheck() {
    }

    @Override
    public int compareTo(CandidateVisaCheck o) {
        if (country == null) {
            return o.country == null ? 0 : -1;
        }
        return country.compareTo(o.country);
    }

    public void populateIntakeData(
            @NonNull Candidate candidate, @NonNull Country country,
            CandidateIntakeDataUpdate data, @Nullable User createdBy) {
        setCandidate(candidate);
        setCountry(country);
        if (createdBy != null) {
            setCreatedBy(createdBy);
        }
        if (data.getVisaAssessmentNotes() != null) {
            setAssessmentNotes(data.getVisaAssessmentNotes());
        }
        if (data.getVisaEligibility() != null) {
            setEligibility(data.getVisaEligibility());
        }
        if (data.getVisaCreatedDate() != null) {
            setCreatedDate(data.getVisaCreatedDate());
        }
        if (data.getVisaProtection() != null) {
            setProtection(data.getVisaProtection());
        }
        if (data.getVisaProtectionGrounds() != null) {
            setProtectionGrounds(data.getVisaProtectionGrounds());
        }
        if (data.getVisaTbbEligibilityAssessment() != null) {
            setTbbEligibilityAssessment(data.getVisaTbbEligibilityAssessment());
        }
    }
    
}
