package org.tbbtalent.server.model;

import javax.persistence.*;

@Entity
@Table(name = "education")
public class Education  extends AbstractDomainObject<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "education_gen")
    @SequenceGenerator(name = "education_gen", sequenceName = "education_id_seq", allocationSize = 1)
    private Long id;

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

    // TODO determine if this is a date or a year?
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

    public String getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(String dateCompleted) {
        this.dateCompleted = dateCompleted;
    }
}
