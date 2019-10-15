package org.tbbtalent.server.request.candidate;

import java.time.LocalDate;
import java.util.List;

import org.tbbtalent.server.model.*;
import org.tbbtalent.server.request.SearchRequest;

public class SearchCandidateRequest extends SearchRequest {

    private Long savedSearchId;
    private String keyword;
    private List<CandidateStatus> statuses;
    private String gender;
    private List<Long> occupationIds;
    private String orProfileKeyword;
    private List<Long> verifiedOccupationIds;
    private SearchType verifiedOccupationSearchType;
    private List<Long> nationalityIds;
    private SearchType nationalitySearchType;
    private List<Long> countryIds;
    private Long englishMinWrittenLevelId;
    private Long englishMinSpokenLevelId;
    private Long otherLanguageId;
    private Long otherMinWrittenLevelId;
    private Long otherMinSpokenLevelId;
    private Boolean unRegistered;
    private LocalDate lastModifiedFrom;
    private LocalDate lastModifiedTo;
    private LocalDate createdFrom;
    private LocalDate createdTo;
    private Integer minAge;
    private Integer maxAge;
    private Long minEducationLevelId;
    private List<Long> educationMajorIds;
    private ShortlistStatus shortlistStatus;

    private List<SearchJoinRequest> searchJoinRequests;

    public Long getSavedSearchId() {
        return savedSearchId;
    }

    public void setSavedSearchId(Long savedSearchId) {
        this.savedSearchId = savedSearchId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<CandidateStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<CandidateStatus> statuses) {
        this.statuses = statuses;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<Long> getOccupationIds() {
        return occupationIds;
    }

    public void setOccupationIds(List<Long> occupationIds) {
        this.occupationIds = occupationIds;
    }

    public String getOrProfileKeyword() {
        return orProfileKeyword;
    }

    public void setOrProfileKeyword(String orProfileKeyword) {
        this.orProfileKeyword = orProfileKeyword;
    }

    public List<Long> getVerifiedOccupationIds() {
        return verifiedOccupationIds;
    }

    public void setVerifiedOccupationIds(List<Long> verifiedOccupationIds) {
        this.verifiedOccupationIds = verifiedOccupationIds;
    }

    public SearchType getVerifiedOccupationSearchType() {
        return verifiedOccupationSearchType;
    }

    public void setVerifiedOccupationSearchType(SearchType verifiedOccupationSearchType) {
        this.verifiedOccupationSearchType = verifiedOccupationSearchType;
    }

    public List<Long> getNationalityIds() {
        return nationalityIds;
    }

    public void setNationalityIds(List<Long> nationalityIds) {
        this.nationalityIds = nationalityIds;
    }

    public SearchType getNationalitySearchType() {
        return nationalitySearchType;
    }

    public void setNationalitySearchType(SearchType nationalitySearchType) {
        this.nationalitySearchType = nationalitySearchType;
    }

    public List<Long> getCountryIds() {
        return countryIds;
    }

    public void setCountryIds(List<Long> countryIds) {
        this.countryIds = countryIds;
    }

    public Long getEnglishMinWrittenLevelId() {
        return englishMinWrittenLevelId;
    }

    public void setEnglishMinWrittenLevelId(Long englishMinWrittenLevelId) {
        this.englishMinWrittenLevelId = englishMinWrittenLevelId;
    }

    public Long getEnglishMinSpokenLevelId() {
        return englishMinSpokenLevelId;
    }

    public void setEnglishMinSpokenLevelId(Long englishMinSpokenLevelId) {
        this.englishMinSpokenLevelId = englishMinSpokenLevelId;
    }

    public Long getOtherLanguageId() {
        return otherLanguageId;
    }

    public void setOtherLanguageId(Long otherLanguageId) {
        this.otherLanguageId = otherLanguageId;
    }

    public Long getOtherMinWrittenLevelId() {
        return otherMinWrittenLevelId;
    }

    public void setOtherMinWrittenLevelId(Long otherMinWrittenLevelId) {
        this.otherMinWrittenLevelId = otherMinWrittenLevelId;
    }

    public Long getOtherMinSpokenLevelId() {
        return otherMinSpokenLevelId;
    }

    public void setOtherMinSpokenLevelId(Long otherMinSpokenLevelId) {
        this.otherMinSpokenLevelId = otherMinSpokenLevelId;
    }

    public Boolean getUnRegistered() {
        return unRegistered;
    }

    public void setUnRegistered(Boolean unRegistered) {
        this.unRegistered = unRegistered;
    }

    public LocalDate getLastModifiedFrom() {
        return lastModifiedFrom;
    }

    public void setLastModifiedFrom(LocalDate lastModifiedFrom) {
        this.lastModifiedFrom = lastModifiedFrom;
    }

    public LocalDate getLastModifiedTo() {
        return lastModifiedTo;
    }

    public void setLastModifiedTo(LocalDate lastModifiedTo) {
        this.lastModifiedTo = lastModifiedTo;
    }

    public LocalDate getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(LocalDate createdFrom) {
        this.createdFrom = createdFrom;
    }

    public LocalDate getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(LocalDate createdTo) {
        this.createdTo = createdTo;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public Long getMinEducationLevelId() {
        return minEducationLevelId;
    }

    public void setMinEducationLevelId(Long minEducationLevelId) {
        this.minEducationLevelId = minEducationLevelId;
    }

    public List<Long> getEducationMajorIds() {
        return educationMajorIds;
    }

    public void setEducationMajorIds(List<Long> educationMajorIds) {
        this.educationMajorIds = educationMajorIds;
    }

    public List<SearchJoinRequest> getSearchJoinRequests() {
        return searchJoinRequests;
    }

    public void setSearchJoinRequests(List<SearchJoinRequest> searchJoinRequests) {
        this.searchJoinRequests = searchJoinRequests;
    }

    public ShortlistStatus getShortlistStatus() {
        return shortlistStatus;
    }

    public void setShortlistStatus(ShortlistStatus shortlistStatus) {
        this.shortlistStatus = shortlistStatus;
    }
}

