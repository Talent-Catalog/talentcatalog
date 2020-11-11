/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "candidate_visa")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_visa_id_seq", allocationSize = 1)
public class CandidateVisaCheck extends AbstractAuditableDomainObject<Long>
        implements Comparable<CandidateVisaCheck> {
    
    private String assessmentNotes;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checked_by_user_id")
    private User checkedBy;

    @Nullable
    private LocalDate checkedDate;

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
            CandidateIntakeDataUpdate data, @Nullable User checkedBy) {
        setCandidate(candidate);
        setCountry(country);
        if (checkedBy != null) {
            setCheckedBy(checkedBy);
        }
        if (data.getVisaAssessmentNotes() != null) {
            setAssessmentNotes(data.getVisaAssessmentNotes());
        }
        if (data.getVisaEligibility() != null) {
            setEligibility(data.getVisaEligibility());
        }
        if (data.getVisaCheckedDate() != null) {
            setCheckedDate(data.getVisaCheckedDate());
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
