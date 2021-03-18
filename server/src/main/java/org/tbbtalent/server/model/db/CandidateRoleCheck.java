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

@Getter
@Setter
@Entity
@Table(name = "candidate_role")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_role_id_seq", allocationSize = 1)
public class CandidateRoleCheck extends AbstractAuditableDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    //todo rename column to candidate visa id in a flyway
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_visa_id")
    private CandidateVisaCheck candidateVisa;

    @Enumerated(EnumType.STRING)
    private YesNo interest;

    private String interestNotes;

    private Long workExperienceYrs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qualification_id")
    private EducationLevel qualification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_id")
    private Occupation occupation;

    private String occupationNotes;

    @Enumerated(EnumType.STRING)
    private YesNo salaryTsmit;

    @Enumerated(EnumType.STRING)
    private YesNo regional;

    @Enumerated(EnumType.STRING)
    private YesNo eligible494;

    private String eligible494Notes;

    @Enumerated(EnumType.STRING)
    private YesNo eligible186;

    private String eligible186Notes;

    @Enumerated(EnumType.STRING)
    private YesNo eligibleOther;

    private String eligibleOtherNotes;

    @Enumerated(EnumType.STRING)
    private YesNo putForward;

    private String notes;

    public CandidateRoleCheck() {
    }


    public void populateIntakeData(
            @NonNull Candidate candidate, @NonNull Country country,
            CandidateIntakeDataUpdate data, @Nullable User createdBy) {
        setCandidate(candidate);

    }
    
}
