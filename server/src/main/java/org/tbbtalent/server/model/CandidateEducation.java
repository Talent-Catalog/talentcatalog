package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "candidate_education")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_education_id_seq", allocationSize = 1)
public class CandidateEducation extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    private EducationType educationType;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    private Integer lengthOfCourseYears;
    private String institution;
    private String courseName;

    private Integer yearCompleted;

    public CandidateEducation() {
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public EducationType getEducationType() { return educationType; }

    public void setEducationType(EducationType educationType) { this.educationType = educationType; }

    public Country getCountry() { return country; }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Integer getLengthOfCourseYears() {
        return lengthOfCourseYears;
    }

    public void setLengthOfCourseYears(Integer lengthOfCourseYears) {
        this.lengthOfCourseYears = lengthOfCourseYears;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getYearCompleted() {
        return yearCompleted;
    }

    public void setYearCompleted(Integer yearCompleted) {
        this.yearCompleted = yearCompleted;
    }
}
