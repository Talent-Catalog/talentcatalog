package org.tctalent.server.request.candidate.exam;

import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.Exam;
import org.tctalent.server.request.PagedSearchRequest;

@Setter
@Getter
public class SearchCandidateExamRequest extends PagedSearchRequest {

    private Exam exam;

    private String otherExam;

    private String score;

    private Long year;

    private String notes;

    // Getters and Setters

}
