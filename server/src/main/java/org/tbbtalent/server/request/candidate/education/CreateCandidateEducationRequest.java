package org.tbbtalent.server.request.candidate.education;

import org.tbbtalent.server.model.EducationType;

public class CreateCandidateEducationRequest {

    private EducationType educationType;
    private Long countryId;
    private Long educationMajorId;
    private Integer lengthOfCourseYears;
    private String institution;
    private String courseName;
    private Integer yearCompleted;

    public EducationType getEducationType() {
        return educationType;
    }

    public void setEducationType(EducationType educationType) {
        this.educationType = educationType;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public Long getEducationMajorId() {
        return educationMajorId;
    }

    public void setEducationMajorId(Long educationMajorId) {
        this.educationMajorId = educationMajorId;
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
