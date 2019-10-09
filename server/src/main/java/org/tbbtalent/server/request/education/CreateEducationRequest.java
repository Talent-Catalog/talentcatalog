package org.tbbtalent.server.request.education;

public class CreateEducationRequest {

    private String educationType;
    private Long countryId;
    private Integer lengthOfCourseYears;
    private String institution;
    private String courseName;
    private Integer yearCompleted;

    public String getEducationType() {
        return educationType;
    }

    public void setEducationType(String educationType) {
        this.educationType = educationType;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
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
