/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
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
    }

}
