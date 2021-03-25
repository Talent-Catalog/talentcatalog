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

@Getter
@Setter
@MappedSuperclass
public class CandidateVisaJobCheckBase extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_visa_check_id")
    private CandidateVisaCheck candidateVisaCheck;

    @Enumerated(EnumType.STRING)
    private YesNo interest;

    private String interestNotes;

    private Long workExperienceYrs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "education_level_id")
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
}
