/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.lang.NonNull;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "candidate_visa")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_visa_id_seq", allocationSize = 1)
public class CandidateVisaCheck extends AbstractDomainObject<Long>
        implements Comparable<CandidateVisaCheck> {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    private VisaEligibility eligibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    private String assessmentNotes;

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
            CandidateIntakeDataUpdate data) {
        setCandidate(candidate);
        setCountry(country);
        if (data.getVisaAssessmentNotes() != null) {
            setAssessmentNotes(data.getVisaAssessmentNotes());
        }
        if (data.getVisaEligibility() != null) {
            setEligibility(data.getVisaEligibility());
        }
    }
    
}
