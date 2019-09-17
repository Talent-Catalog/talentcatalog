package org.tbbtalent.server.request.education;

public class CreateEducationRequest {

    private String educationType;
    private String countryId;
    private String lengthOfCourseYears;
    private String institution;
    private String courseName;
    private String dateCompleted;

    public String getEducationType() {
        return educationType;
    }

    public void setEducationType(String educationType) {
        this.educationType = educationType;
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
