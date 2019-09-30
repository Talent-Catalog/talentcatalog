package org.tbbtalent.server.request.candidate;

import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.request.SearchRequest;

import java.util.List;

public class SearchCandidateRequest extends SearchRequest {

    private String keyword;
    private List<Status> selectedStatus;
    private Boolean registeredWithUN;
    private Long nationalityId;
    private Long countryId;
    private String gender;
    private String educationLevel;
    private Long candidateLanguageId;
    private List<Nationality> selectedNationalities;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<Status> getSelectedStatus() { return selectedStatus; }

    public void setSelectedStatus(List<Status> selectedStatus) { this.selectedStatus = selectedStatus; }

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

    public List<Nationality> getSelectedNationalities() { return selectedNationalities; }

    public void setSelectedNationalities(List<Nationality> selectedNationalities) { this.selectedNationalities = selectedNationalities; }
}

