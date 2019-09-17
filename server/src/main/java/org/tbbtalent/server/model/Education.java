package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "education")
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "education_gen")
    @SequenceGenerator(name = "education_gen", sequenceName = "education_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    private String educationTypeId;
    private String countryId;
    private String lengthOfCourseYears;
    private String institution;
    private String courseName;
    private String dateCompleted;

    public Education() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public String getEducationTypeId() {
        return educationTypeId;
    }

    public void setEducationTypeId(String educationTypeId) {
        this.educationTypeId = educationTypeId;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getLengthOfCourseYears() {
        return lengthOfCourseYears;
    }

    public void setLengthOfCourseYears(String lengthOfCourseYears) {
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

    public String getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(String dateCompleted) {
        this.dateCompleted = dateCompleted;
    }
}
