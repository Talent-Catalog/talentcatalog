package org.tbbtalent.server.request.candidate.exam;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.Exam;

@Getter
@Setter
@ToString
public class CreateCandidateExamRequest {
    @Nullable
    private Exam exam;
    @Nullable
    private String otherExam;
    @Nullable
    private String score;
}
