/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.lang.Nullable;
import org.tbbtalent.server.api.admin.SavedSearchAdminApi;
import org.tbbtalent.server.model.es.CandidateEs;
import org.tbbtalent.server.request.candidate.CandidateIntakeData;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.impl.SalesforceServiceImpl;

@Entity
@Table(name = "candidate")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_id_seq", allocationSize = 1)
public class Candidate extends AbstractAuditableDomainObject<Long> {

    private String candidateNumber;
    
    @Transient
    private Long contextSavedListId; 
    
    private String phone;
    private String whatsapp;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private LocalDate dob;
    private String address1;
    private String city;
    private Integer yearOfArrival;
    private Boolean unRegistered;
    private String unRegistrationNumber;
    private String additionalInfo;
    private String candidateMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_type_id")
    private SurveyType surveyType;

    private String surveyComment;

    @Enumerated(EnumType.STRING)
    private CandidateStatus status;

    /**
     * ID of corresponding candidate record in Elasticsearch
     * @see CandidateEs#getId() 
     */
    private String textSearchId;

    /**
     * Even though we would prefer CascadeType.ALL with 'orphanRemoval' so that 
     * removing from the candidateSavedLists collection would automatically
     * cascade down to delete the corresponding entry in the 
     * candidate_saved_list table.
     * However we get Hibernate errors with that set up which it seems can only 
     * be fixed by setting CascadeType.MERGE.
     * <p/>
     * See
     * https://stackoverflow.com/questions/16246675/hibernate-error-a-different-object-with-the-same-identifier-value-was-already-a
     * <p/>
     * This means that we have to manually manage all deletions. That has been
     * moved into {@link CandidateSavedListService} which is used to manage all
     * those deletions, also making sure that the corresponding 
     * candidateSavedLists collections are kept up to date.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<CandidateSavedList> candidateSavedLists = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "max_education_level_id")
    private EducationLevel maxEducationLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality_id")
    private Nationality nationality;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateOccupation> candidateOccupations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateEducation> candidateEducations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateLanguage> candidateLanguages;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateJobExperience> candidateJobExperiences;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateCertification> candidateCertifications;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<CandidateReviewStatusItem> candidateReviewStatusItems;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateSkill> candidateSkills;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateAttachment> candidateAttachments;

    //old data only links to candidate needs to be searchable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_education_major_id")
    private EducationMajor migrationEducationMajor;

    private String migrationNationality;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists. 
     */
    @Nullable
    private String folderlink;

    /**
     * Url link to corresponding Salesforce Contact record, if one exists. 
     */
    @Nullable
    private String sflink;

    /**
     * Url Link to candidate video if any.
     */
    @Nullable
    private String videolink;

    /**
     * This can be set based on a user's selection associated with a saved
     * search, as recorded in the associated selection list for that user
     * and saved search.
     * @see SavedSearchAdminApi#selectCandidate   
     */
    @Transient
    private boolean selected = false;

    /*
              Intake Fields    
     */
    
    @Enumerated(EnumType.STRING)
    @Nullable
    private ReturnedHome returnedHome;

    @Nullable
    private String returnedHomeNotes;

    @Nullable
    private String returnedHomeReason;
    
    @Convert(converter = VisaIssuesConverter.class)
    @Nullable
    private List<VisaIssue> visaIssues;
    
    @Nullable
    private String visaIssuesNotes;
    

    public Candidate() {
    }

    //todo The "caller" is the user used to set the createdBy and updatedBy fields
    //Seems to always be the same as user - so not sure if it has any point.
    public Candidate(User user, String phone, String whatsapp, User caller) {
        super(caller);
        this.user = user;
        this.phone = phone;
        this.whatsapp = whatsapp;
        this.status = CandidateStatus.draft;
    }

    public String getCandidateNumber() {
        return candidateNumber;
    }

    public void setCandidateNumber(String candidateNumber) {
        this.candidateNumber = candidateNumber;
    }

    /**
     * Returns this candidate's contextNote, associated with the current
     * SavedList context - if any.
     * <p/>
     * See {@link #setContextSavedListId(Long)}
     * @return ContextNote associated with current context. Returns null
     * if there is one, or no context has been set.
     */
    @Transient
    @Nullable
    public String getContextNote() {
        String contextNote = null;
        if (contextSavedListId != null) {
            for (CandidateSavedList csl : candidateSavedLists) {
                if (contextSavedListId.equals(csl.getSavedList().getId())) {
                    contextNote = csl.getContextNote();
                    break;
                }
            }
        }
        return contextNote;
    }

    /**
     * Candidates can have special values associated with a particular 
     * savedList.
     * These values are stored in {@link CandidateSavedList}.
     * Setting this value to refer to a particular SavedList will result in
     * this Candidate object returning attritbutes correspondint that list.
     * <p/>
     * For example, see {@link #getContextNote()}
     * 
     * @param contextSavedListId The id of the SavedList whose context we want
     */
    @Transient
    public void setContextSavedListId(@Nullable Long contextSavedListId) {
        this.contextSavedListId = contextSavedListId; 
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getYearOfArrival() {
        return yearOfArrival;
    }

    public void setYearOfArrival(Integer yearOfArrival) {
        this.yearOfArrival = yearOfArrival;
    }

    public Boolean getUnRegistered() {
        return unRegistered;
    }

    public void setUnRegistered(Boolean unRegistered) {
        this.unRegistered = unRegistered;
    }

    public String getUnRegistrationNumber() {
        return unRegistrationNumber;
    }

    public void setUnRegistrationNumber(String unRegistrationNumber) {
        this.unRegistrationNumber = unRegistrationNumber;
    }

    public EducationLevel getMaxEducationLevel() {
        return maxEducationLevel;
    }

    public void setMaxEducationLevel(EducationLevel maxEducationLevel) {
        this.maxEducationLevel = maxEducationLevel;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getCandidateMessage() {
        return candidateMessage;
    }

    public void setCandidateMessage(String candidateMessage) {
        this.candidateMessage = candidateMessage;
    }

    public SurveyType getSurveyType() { return surveyType; }

    public void setSurveyType(SurveyType surveyType) { this.surveyType = surveyType; }

    public String getSurveyComment() { return surveyComment; }

    public void setSurveyComment(String surveyComment) { this.surveyComment = surveyComment; }

    public CandidateStatus getStatus() {
        return status;
    }

    public void setStatus(CandidateStatus status) {
        this.status = status;
    }

    public String getTextSearchId() {
        return textSearchId;
    }

    public void setTextSearchId(String textSearchId) {
        this.textSearchId = textSearchId;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Nationality getNationality() {
        return nationality;
    }

    public void setNationality(Nationality nationality) {
        this.nationality = nationality;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CandidateOccupation> getCandidateOccupations() {
        return candidateOccupations;
    }

    public void setCandidateOccupations(List<CandidateOccupation> candidateOccupations) {
        this.candidateOccupations = candidateOccupations;
    }

    public List<CandidateEducation> getCandidateEducations() {
        return candidateEducations;
    }

    public void setCandidateEducations(List<CandidateEducation> candidateEducations) {
        this.candidateEducations = candidateEducations;
    }

    public List<CandidateLanguage> getCandidateLanguages() {
        return candidateLanguages;
    }

    public void setCandidateLanguages(List<CandidateLanguage> candidateLanguages) {
        this.candidateLanguages = candidateLanguages;
    }

    public List<CandidateJobExperience> getCandidateJobExperiences() {
        return candidateJobExperiences;
    }

    public void setCandidateJobExperiences(List<CandidateJobExperience> candidateJobExperiences) {
        this.candidateJobExperiences = candidateJobExperiences;
    }

    public List<CandidateCertification> getCandidateCertifications() {
        return candidateCertifications;
    }

    public void setCandidateCertifications(List<CandidateCertification> candidateCertifications) {
        this.candidateCertifications = candidateCertifications;
    }

    public Set<CandidateReviewStatusItem> getCandidateReviewStatusItems() {
        return candidateReviewStatusItems;
    }

    public void setCandidateReviewStatusItems(Set<CandidateReviewStatusItem> candidateReviewStatusItems) {
        this.candidateReviewStatusItems = candidateReviewStatusItems;
    }


    public EducationMajor getMigrationEducationMajor() {
        return migrationEducationMajor;
    }

    public void setMigrationEducationMajor(EducationMajor migrationEducationMajor) {
        this.migrationEducationMajor = migrationEducationMajor;
    }

    public List<CandidateSkill> getCandidateSkills() {
        return candidateSkills;
    }

    public void setCandidateSkills(List<CandidateSkill> candidateSkills) {
        this.candidateSkills = candidateSkills;
    }

    public List<CandidateAttachment> getCandidateAttachments() { return candidateAttachments; }

    public void setCandidateAttachments(List<CandidateAttachment> candidateAttachments) { this.candidateAttachments = candidateAttachments; }

    public String getMigrationCountry() {
        return migrationNationality;
    }

    public void setMigrationCountry(String migrationCountry) {
        this.migrationNationality = migrationCountry;
    }

    public String getMigrationNationality() {
        return migrationNationality;
    }

    public void setMigrationNationality(String migrationNationality) {
        this.migrationNationality = migrationNationality;
    }

    @Nullable
    public String getFolderlink() {
        return folderlink;
    }

    public void setFolderlink(@Nullable String folderlink) {
        this.folderlink = folderlink;
    }

    @Nullable
    public ReturnedHome getReturnedHome() {
        return returnedHome;
    }

    public void setReturnedHome(@Nullable ReturnedHome returnedHome) {
        this.returnedHome = returnedHome;
    }

    @Nullable
    public String getReturnedHomeNotes() {
        return returnedHomeNotes;
    }

    public void setReturnedHomeNotes(@Nullable String returnedHomeNotes) {
        this.returnedHomeNotes = returnedHomeNotes;
    }

    @Nullable
    public String getReturnedHomeReason() {
        return returnedHomeReason;
    }

    public void setReturnedHomeReason(@Nullable String returnedHomeReason) {
        this.returnedHomeReason = returnedHomeReason;
    }

    @Nullable
    public List<VisaIssue> getVisaIssues() {
        return visaIssues;
    }

    public void setVisaIssues(@Nullable List<VisaIssue> visaIssues) {
        this.visaIssues = visaIssues;
    }

    @Nullable
    public String getVisaIssuesNotes() {
        return visaIssuesNotes;
    }

    public void setVisaIssuesNotes(@Nullable String visaIssuesNotes) {
        this.visaIssuesNotes = visaIssuesNotes;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Nullable
    public String getSflink() {
        return sflink;
    }

    public void setSflink(@Nullable String sflink) {
        this.sflink = sflink;
    }

    @Nullable
    public String getSfId() {
        return SalesforceServiceImpl.extractIdFromSfUrl(sflink);
    }
    
    @Nullable
    public String getVideolink() {
        return videolink;
    }

    public void setVideolink(@Nullable String videolink) {
        this.videolink = videolink;
    }

    public Set<CandidateSavedList> getCandidateSavedLists() {
        return candidateSavedLists;
    }

    public void setCandidateSavedLists(Set<CandidateSavedList> candidateSavedLists) {
        this.candidateSavedLists = candidateSavedLists;
    }

    @Transient
    public Set<SavedList> getSavedLists() {
        Set<SavedList> savedLists = new HashSet<>();
        for (CandidateSavedList candidateSavedList : candidateSavedLists) {
            savedLists.add(candidateSavedList.getSavedList());
        }
        return savedLists;
    }

    public void addSavedLists(Set<SavedList> savedLists) {
        for (SavedList savedList : savedLists) {
            addSavedList(savedList);
        }
    }

    public void addSavedList(SavedList savedList) {
        final CandidateSavedList csl = 
                new CandidateSavedList(this, savedList);
        candidateSavedLists.add(csl);
        savedList.getCandidateSavedLists().add(csl);
    }

    public void populateIntakeData(CandidateIntakeData data) {
        if (data.getReturnedHome() != null) {
            setReturnedHome(data.getReturnedHome());
        }
        if (data.getReturnedHomeNotes() != null) {
            setReturnedHomeNotes(data.getReturnedHomeNotes());
        }
        if (data.getReturnedHomeReason() != null) {
            setReturnedHomeReason(data.getReturnedHomeReason());
        }

        if (data.getVisaIssues() != null) {
            setVisaIssues(data.getVisaIssues());
        }
        if (data.getVisaIssuesNotes() != null) {
            setVisaIssuesNotes(data.getVisaIssuesNotes());
        }
    }
}
