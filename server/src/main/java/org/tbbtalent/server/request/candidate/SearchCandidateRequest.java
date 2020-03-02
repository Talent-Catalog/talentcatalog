package org.tbbtalent.server.request.candidate;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Sort;
import org.tbbtalent.server.model.CandidateStatus;
import org.tbbtalent.server.model.Gender;
import org.tbbtalent.server.model.SearchType;
import org.tbbtalent.server.model.ShortlistStatus;
import org.tbbtalent.server.request.SearchRequest;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class SearchCandidateRequest extends SearchRequest {

    private Long savedSearchId;
    private String keyword;
    private List<CandidateStatus> statuses;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private List<Long> occupationIds;
    private String orProfileKeyword;
    private List<Long> verifiedOccupationIds;
    private SearchType verifiedOccupationSearchType;
    private List<Long> nationalityIds;
    private SearchType nationalitySearchType;
    private List<Long> countryIds;
    private Integer englishMinWrittenLevel;
    private Integer englishMinSpokenLevel;
    private Long otherLanguageId;
    private Integer otherMinWrittenLevel;
    private Integer otherMinSpokenLevel;
    private Boolean unRegistered;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    private LocalDate lastModifiedFrom;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    private LocalDate lastModifiedTo;
//    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
//    private LocalDate registeredFrom;
//    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
//    private LocalDate registeredTo;
    @NotNull
    private String timezone;
    private Integer minAge;
    private Integer maxAge;
    private Integer minEducationLevel;
    private List<Long> educationMajorIds;
    private List<ShortlistStatus> shortlistStatus;
    private boolean includeNew;
    private Boolean includeDraftAndDeleted;

    public SearchCandidateRequest() {
        super(Sort.Direction.DESC, new String[]{"id"});
    }

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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
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

    public Integer getEnglishMinWrittenLevel() {
        return englishMinWrittenLevel;
    }

    public void setEnglishMinWrittenLevel(Integer englishMinWrittenLevel) {
        this.englishMinWrittenLevel = englishMinWrittenLevel;
    }

    public Integer getEnglishMinSpokenLevel() {
        return englishMinSpokenLevel;
    }

    public void setEnglishMinSpokenLevel(Integer englishMinSpokenLevel) {
        this.englishMinSpokenLevel = englishMinSpokenLevel;
    }

    public Long getOtherLanguageId() {
        return otherLanguageId;
    }

    public void setOtherLanguageId(Long otherLanguageId) {
        this.otherLanguageId = otherLanguageId;
    }

    public Integer getOtherMinWrittenLevel() {
        return otherMinWrittenLevel;
    }

    public void setOtherMinWrittenLevel(Integer otherMinWrittenLevel) {
        this.otherMinWrittenLevel = otherMinWrittenLevel;
    }

    public Integer getOtherMinSpokenLevel() {
        return otherMinSpokenLevel;
    }

    public void setOtherMinSpokenLevel(Integer otherMinSpokenLevel) {
        this.otherMinSpokenLevel = otherMinSpokenLevel;
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

//    public LocalDate getRegisteredFrom() {
//        return registeredFrom;
//    }
//
//    public void setRegisteredFrom(LocalDate registeredFrom) {
//        this.registeredFrom = registeredFrom;
//    }
//
//    public LocalDate getRegisteredTo() {
//        return registeredTo;
//    }
//
//    public void setRegisteredTo(LocalDate registeredTo) {
//        this.registeredTo = registeredTo;
//    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
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

    public Integer getMinEducationLevel() {
        return minEducationLevel;
    }

    public void setMinEducationLevel(Integer minEducationLevel) {
        this.minEducationLevel = minEducationLevel;
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

    public List<ShortlistStatus> getShortlistStatus() {
        return shortlistStatus;
    }

    public void setShortlistStatus(List<ShortlistStatus> shortlistStatus) {
        this.shortlistStatus = shortlistStatus;
    }

    public boolean isIncludeNew() {
        return includeNew;
    }

    public void setIncludeNew(boolean includeNew) {
        this.includeNew = includeNew;
    }

    public Boolean getIncludeDraftAndDeleted() {
        return includeDraftAndDeleted;
    }

    public void setIncludeDraftAndDeleted(Boolean includeDraftAndDeleted) {
        this.includeDraftAndDeleted = includeDraftAndDeleted;
    }
}

