package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.request.SearchRequest;

public class SearchCandidateRequest extends SearchRequest {

    private String keyword;
    private String status;
    private Boolean registeredWithUN;
    private Long nationalityId;
    private Long countryId;
    private String gender;
    private String educationLevel;
    private Long candidateLanguageId;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public Boolean getRegisteredWithUN() { return registeredWithUN; }

    public void setRegisteredWithUN(Boolean registeredWithUN) { this.registeredWithUN = registeredWithUN; }

    public Long getNationalityId() { return nationalityId; }

    public void setNationalityId(Long nationalityId) { this.nationalityId = nationalityId; }

    public Long getCountryId() { return countryId; }

    public void setCountryId(Long countryId) { this.countryId = countryId; }

    public String getGender() { return gender; }

    public void setGender(String gender) { this.gender = gender; }

    public String getEducationLevel() { return educationLevel; }

    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }

    public Long getCandidateLanguageId() { return candidateLanguageId; }

    public void setCandidateLanguageId(Long candidateLanguageId) { this.candidateLanguageId = candidateLanguageId; }
}

