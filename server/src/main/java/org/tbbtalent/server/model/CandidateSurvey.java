package org.tbbtalent.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "candidate_survey")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_survey_id_seq", allocationSize = 1)
public class CandidateSurvey extends AbstractDomainObject<Long>  {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private SurveyType surveyType;

    private String comment;

    public CandidateSurvey() {
    }

    public CandidateSurvey(Candidate candidate, SurveyType surveyType, String comment) {
        this.candidate = candidate;
        this.surveyType = surveyType;
        this.comment = comment;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public SurveyType getSurveyType() { return surveyType; }

    public void setSurveyType(SurveyType surveyType) { this.surveyType = surveyType; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

}
