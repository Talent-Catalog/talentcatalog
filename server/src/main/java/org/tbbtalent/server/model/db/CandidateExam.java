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
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "candidate_exam")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_exam_id_seq", allocationSize = 1)
public class CandidateExam extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    private Exam exam;

    private String otherExam;

    private String score;

    private Long year;

    private String notes;

    public CandidateExam() {
    }

    public void populateIntakeData(
            @NonNull Candidate candidate,
            CandidateIntakeDataUpdate data) {
        setCandidate(candidate);
        if (data.getExamType() != null) {
            setExam(data.getExamType());
        }
        if (data.getOtherExam() != null) {
            setOtherExam(data.getOtherExam());
        }
        if (data.getExamScore() != null) {
            setScore(data.getExamScore());
        }
        if (data.getExamYear() != null) {
            setYear(data.getExamYear());
        }
        if (data.getExamNotes() != null) {
            setNotes(data.getExamNotes());
        }
    }

}
