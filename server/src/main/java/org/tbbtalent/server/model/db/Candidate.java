/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import org.springframework.lang.Nullable;
import org.tbbtalent.server.api.admin.SavedSearchAdminApi;
import org.tbbtalent.server.model.es.CandidateEs;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.impl.SalesforceServiceImpl;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateCitizenship> candidateCitizenships;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateDependant> candidateDependants;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateDestination> candidateDestinations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateExam> candidateExams;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateVisaCheck> candidateVisaChecks;
    
    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure returnedHome;

    @Nullable
    private String returnedHomeNotes;

    @Nullable
    private String returnedHomeReason;
    
    @Convert(converter = VisaIssuesConverter.class)
    @Nullable
    private List<VisaIssue> visaIssues;
    
    @Nullable
    private String visaIssuesNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure availImmediate;

    @Enumerated(EnumType.STRING)
    @Nullable
    private AvailImmediateReason availImmediateReason;

    @Nullable
    private String availImmediateNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo familyMove;

    @Nullable
    private String familyMoveNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo familyHealthConcern;

    @Nullable
    private String familyHealthConcernNotes;

    @Convert(converter = IntRecruitReasonConverter.class)
    @Nullable
    private List<IntRecruitReason> intRecruitReasons;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure intRecruitRural;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure returnHomeSafe;

    @Enumerated(EnumType.STRING)
    @Nullable
    private WorkPermit workPermit;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure workPermitDesired;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo workLegally;

    @Nullable
    private Long hostEntryYear;

    @Enumerated(EnumType.STRING)
    @Nullable
    private UnhcrStatus unhcrStatus;

    @Enumerated(EnumType.STRING)
    @Nullable
    private UnhcrStatus unhcrOldStatus;

    @Nullable
    private String unhcrNumber;

    @Nullable
    private Long unhcrFile;

    @Nullable
    private String unhcrNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo unhcrPermission;

    @Enumerated(EnumType.STRING)
    @Nullable
    private UnrwaStatus unrwaStatus;

    @Nullable
    private String unrwaNumber;

    @Nullable
    private String unrwaNotes;

    @Nullable
    private String homeLocation;

    @Nullable
    private LocalDate asylumYear;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo destLimit;

    @Nullable
    private String destLimitNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo destJob;

    @Nullable
    private String destJobNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure crimeConvict;

    @Nullable
    private String crimeConvictNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo conflict;

    @Nullable
    private String conflictNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private ResidenceStatus residenceStatus;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo workAbroad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_abroad_loc")
    @Nullable
    private Country workAbroadLoc;

    @Nullable
    private Long workAbroadYrs;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo hostEntryLegally;

    @Enumerated(EnumType.STRING)
    @Nullable
    private LeftHomeReason leftHomeReason;

    @Nullable
    private String leftHomeOther;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure returnHomeFuture;

    @Nullable
    private String returnHomeWhen;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo resettleThird;

    @Nullable
    private String resettleThirdStatus;

    @Nullable
    private String hostChallenges;

    @Enumerated(EnumType.STRING)
    @Nullable
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure partnerRegistered;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_candidate_id")
    @Nullable
    private Candidate partnerCandidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_edu_level_id")
    @Nullable
    private EducationLevel partnerEduLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_occupation_id")
    @Nullable
    private Occupation partnerProfession;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo partnerEnglish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_english_level_id")
    @Nullable
    private LanguageLevel partnerEnglishLevel;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure partnerIelts;

    @Enumerated(EnumType.STRING)
    @Nullable
    private IeltsScore partnerIeltsScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_citizenship_id")
    @Nullable
    private Nationality partnerCitizenship;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo militaryService;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo children;

    @Nullable
    private String childrenAge;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure visaReject;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo hostBorn;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo canDrive;

    @Enumerated(EnumType.STRING)
    @Nullable
    private DrivingLicenseStatus drivingLicense;

    @Nullable
    private LocalDate drivingLicenseExp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driving_license_country_id")
    @Nullable
    private Country drivingLicenseCountry;

    @Nullable
    private String langAssessment;

    @Enumerated(EnumType.STRING)
    @Nullable
    private IeltsScore langAssessmentScore;

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

    public List<CandidateCitizenship> getCandidateCitizenships() {
        return candidateCitizenships;
    }

    public void setCandidateCitizenships(List<CandidateCitizenship> candidateCitizenships) {
        this.candidateCitizenships = candidateCitizenships;
    }

    public List<CandidateExam> getCandidateExams() { return candidateExams; }

    public void setCandidateExams(List<CandidateExam> candidateExams) { this.candidateExams = candidateExams; }

    public List<CandidateVisaCheck> getCandidateVisaChecks() {
        candidateVisaChecks.sort(null);
        return candidateVisaChecks;
    }

    public void setCandidateVisaChecks(List<CandidateVisaCheck> candidateVisaChecks) {
        this.candidateVisaChecks = candidateVisaChecks;
    }

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
    public YesNoUnsure getReturnedHome() {
        return returnedHome;
    }

    public void setReturnedHome(@Nullable YesNoUnsure returnedHome) { this.returnedHome = returnedHome; }

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

    @Nullable
    public YesNoUnsure getAvailImmediate() { return availImmediate; }

    public void setAvailImmediate(@Nullable YesNoUnsure availImmediate) { this.availImmediate = availImmediate; }

    @Nullable
    public String getAvailImmediateNotes() { return availImmediateNotes; }

    public void setAvailImmediateNotes(@Nullable String availImmediateNotes) { this.availImmediateNotes = availImmediateNotes; }

    @Nullable
    public AvailImmediateReason getAvailImmediateReason() { return availImmediateReason; }

    public void setAvailImmediateReason(@Nullable AvailImmediateReason availImmediateReason) { this.availImmediateReason = availImmediateReason; }

    @Nullable
    public YesNo getFamilyMove() { return familyMove; }

    public void setFamilyMove(@Nullable YesNo familyMove) { this.familyMove = familyMove; }

    @Nullable
    public String getFamilyMoveNotes() { return familyMoveNotes; }

    public void setFamilyMoveNotes(@Nullable String familyMoveNotes) { this.familyMoveNotes = familyMoveNotes; }

    @Nullable
    public YesNo getFamilyHealthConcern() { return familyHealthConcern; }

    public void setFamilyHealthConcern(@Nullable YesNo familyHealthConcern) { this.familyHealthConcern = familyHealthConcern; }

    @Nullable
    public String getFamilyHealthConcernNotes() { return familyHealthConcernNotes; }

    public void setFamilyHealthConcernNotes(@Nullable String familyHealthConcernNotes) { this.familyHealthConcernNotes = familyHealthConcernNotes; }

    @Nullable
    public List<IntRecruitReason> getIntRecruitReasons() { return intRecruitReasons; }

    public void setIntRecruitReasons(@Nullable List<IntRecruitReason> intRecruitReasons) { this.intRecruitReasons = intRecruitReasons; }

    @Nullable
    public YesNoUnsure getIntRecruitRural() { return intRecruitRural; }

    public void setIntRecruitRural(@Nullable YesNoUnsure intRecruitRural) { this.intRecruitRural = intRecruitRural; }

    @Nullable
    public YesNoUnsure getReturnHomeSafe() { return returnHomeSafe; }

    public void setReturnHomeSafe(@Nullable YesNoUnsure returnHomeSafe) { this.returnHomeSafe = returnHomeSafe; }

    @Nullable
    public WorkPermit getWorkPermit() { return workPermit; }

    public void setWorkPermit(@Nullable WorkPermit workPermit) { this.workPermit = workPermit; }

    @Nullable
    public YesNoUnsure getWorkPermitDesired() { return workPermitDesired; }

    public void setWorkPermitDesired(@Nullable YesNoUnsure workPermitDesired) { this.workPermitDesired = workPermitDesired; }

    @Nullable
    public YesNo getWorkLegally() { return workLegally; }

    public void setWorkLegally(@Nullable YesNo workLegally) { this.workLegally = workLegally; }

    @Nullable
    public Long getHostEntryYear() { return hostEntryYear; }

    public void setHostEntryYear(@Nullable Long hostEntryYear) { this.hostEntryYear = hostEntryYear; }

    @Nullable
    public UnhcrStatus getUnhcrStatus() { return unhcrStatus; }

    public void setUnhcrStatus(@Nullable UnhcrStatus unhcrStatus) { this.unhcrStatus = unhcrStatus; }

    @Nullable
    public UnhcrStatus getUnhcrOldStatus() { return unhcrOldStatus; }

    public void setUnhcrOldStatus(@Nullable UnhcrStatus unhcrOldStatus) { this.unhcrOldStatus = unhcrOldStatus; }

    @Nullable
    public String getUnhcrNumber() { return unhcrNumber; }

    public void setUnhcrNumber(@Nullable String unhcrNumber) { this.unhcrNumber = unhcrNumber; }

    @Nullable
    public Long getUnhcrFile() { return unhcrFile; }

    public void setUnhcrFile(@Nullable Long unhcrFile) { this.unhcrFile = unhcrFile; }

    @Nullable
    public String getUnhcrNotes() { return unhcrNotes; }

    public void setUnhcrNotes(@Nullable String unhcrNotes) { this.unhcrNotes = unhcrNotes; }

    @Nullable
    public YesNo getUnhcrPermission() { return unhcrPermission; }

    public void setUnhcrPermission(@Nullable YesNo unhcrPermission) { this.unhcrPermission = unhcrPermission; }

    @Nullable
    public UnrwaStatus getUnrwaStatus() { return unrwaStatus; }

    public void setUnrwaStatus(@Nullable UnrwaStatus unrwaStatus) { this.unrwaStatus = unrwaStatus; }

    @Nullable
    public String getUnrwaNumber() { return unrwaNumber; }

    public void setUnrwaNumber(@Nullable String unrwaNumber) { this.unrwaNumber = unrwaNumber; }

    @Nullable
    public String getUnrwaNotes() { return unrwaNotes; }

    public void setUnrwaNotes(@Nullable String unrwaNotes) { this.unrwaNotes = unrwaNotes; }

    @Nullable
    public String getHomeLocation() { return homeLocation; }

    public void setHomeLocation(@Nullable String homeLocation) { this.homeLocation = homeLocation; }

    @Nullable
    public LocalDate getAsylumYear() { return asylumYear; }

    public void setAsylumYear(@Nullable LocalDate asylumYear) { this.asylumYear = asylumYear; }

    @Nullable
    public List<CandidateDependant> getCandidateDependants() { return candidateDependants; }

    public void setCandidateDependants(List<CandidateDependant> candidateDependants) { this.candidateDependants = candidateDependants; }

    public List<CandidateDestination> getCandidateDestinations() {
        candidateDestinations.sort(null);
        return candidateDestinations;
    }

    public void setCandidateDestinations(List<CandidateDestination> candidateDestinations) { this.candidateDestinations = candidateDestinations; }

    @Nullable
    public YesNo getDestLimit() { return destLimit; }

    public void setDestLimit(@Nullable YesNo destLimit) { this.destLimit = destLimit; }

    @Nullable
    public String getDestLimitNotes() { return destLimitNotes; }

    public void setDestLimitNotes(@Nullable String destLimitNotes) { this.destLimitNotes = destLimitNotes; }

    @Nullable
    public YesNo getDestJob() { return destJob; }

    public void setDestJob(@Nullable YesNo destJob) { this.destJob = destJob; }

    @Nullable
    public String getDestJobNotes() {
        return destJobNotes;
    }

    public void setDestJobNotes(@Nullable String destJobNotes) {
        this.destJobNotes = destJobNotes;
    }

    @Nullable
    public YesNoUnsure getCrimeConvict() { return crimeConvict; }

    public void setCrimeConvict(@Nullable YesNoUnsure crime) { this.crimeConvict = crime; }

    @Nullable
    public String getCrimeConvictNotes() { return crimeConvictNotes; }

    public void setCrimeConvictNotes(@Nullable String crimeConvictNotes) { this.crimeConvictNotes = crimeConvictNotes; }

    @Nullable
    public YesNo getConflict() { return conflict; }

    public void setConflict(@Nullable YesNo conflict) { this.conflict = conflict; }

    @Nullable
    public String getConflictNotes() { return conflictNotes; }

    public void setConflictNotes(@Nullable String conflictNotes) { this.conflictNotes = conflictNotes; }

    @Nullable
    public ResidenceStatus getResidenceStatus() { return residenceStatus; }

    public void setResidenceStatus(@Nullable ResidenceStatus residenceStatus) { this.residenceStatus = residenceStatus; }

    @Nullable
    public YesNo getWorkAbroad() { return workAbroad; }

    public void setWorkAbroad(@Nullable YesNo workAbroad) { this.workAbroad = workAbroad; }

    @Nullable
    public Country getWorkAbroadLoc() { return workAbroadLoc; }

    public void setWorkAbroadLoc(@Nullable Country workAbroadLoc) { this.workAbroadLoc = workAbroadLoc; }

    @Nullable
    public Long getWorkAbroadYrs() { return workAbroadYrs; }

    public void setWorkAbroadYrs(@Nullable Long workAbroadYrs) { this.workAbroadYrs = workAbroadYrs; }

    @Nullable
    public YesNo getHostEntryLegally() { return hostEntryLegally; }

    public void setHostEntryLegally(@Nullable YesNo hostEntryLegally) { this.hostEntryLegally = hostEntryLegally; }

    @Nullable
    public LeftHomeReason getLeftHomeReason() { return leftHomeReason; }

    public void setLeftHomeReason(@Nullable LeftHomeReason leftHomeReason) { this.leftHomeReason = leftHomeReason; }

    @Nullable
    public String getLeftHomeOther() { return leftHomeOther; }

    public void setLeftHomeOther(@Nullable String leftHomeOther) { this.leftHomeOther = leftHomeOther; }

    @Nullable
    public YesNoUnsure getReturnHomeFuture() { return returnHomeFuture; }

    public void setReturnHomeFuture(@Nullable YesNoUnsure returnHomeFuture) { this.returnHomeFuture = returnHomeFuture; }

    @Nullable
    public String getReturnHomeWhen() { return returnHomeWhen; }

    public void setReturnHomeWhen(@Nullable String returnHomeWhen) { this.returnHomeWhen = returnHomeWhen; }

    @Nullable
    public YesNo getResettleThird() { return resettleThird; }

    public void setResettleThird(@Nullable YesNo resettleThird) { this.resettleThird = resettleThird; }

    @Nullable
    public String getResettleThirdStatus() { return resettleThirdStatus; }

    public void setResettleThirdStatus(@Nullable String resettleThirdStatus) { this.resettleThirdStatus = resettleThirdStatus; }

    @Nullable
    public String getHostChallenges() { return hostChallenges; }

    public void setHostChallenges(@Nullable String hostChallenges) { this.hostChallenges = hostChallenges; }

    @Nullable
    public MaritalStatus getMaritalStatus() { return maritalStatus; }

    public void setMaritalStatus(@Nullable MaritalStatus maritalStatus) { this.maritalStatus = maritalStatus; }

    @Nullable
    public YesNoUnsure getPartnerRegistered() { return partnerRegistered; }

    public void setPartnerRegistered(@Nullable YesNoUnsure partnerRegistered) { this.partnerRegistered = partnerRegistered; }

    @Nullable
    public Candidate getPartnerCandidate() { return partnerCandidate; }

    public void setPartnerCandidate(@Nullable Candidate partnerCandidate) { this.partnerCandidate = partnerCandidate; }

    @Nullable
    public EducationLevel getPartnerEduLevel() { return partnerEduLevel; }

    public void setPartnerEduLevel(@Nullable EducationLevel partnerEduLevel) { this.partnerEduLevel = partnerEduLevel; }

    @Nullable
    public Occupation getPartnerProfession() { return partnerProfession; }

    public void setPartnerProfession(@Nullable Occupation partnerProfession) { this.partnerProfession = partnerProfession; }

    @Nullable
    public YesNo getPartnerEnglish() { return partnerEnglish; }

    public void setPartnerEnglish(@Nullable YesNo partnerEnglish) { this.partnerEnglish = partnerEnglish; }

    @Nullable
    public LanguageLevel getPartnerEnglishLevel() { return partnerEnglishLevel; }

    public void setPartnerEnglishLevel(@Nullable LanguageLevel partnerEnglishLevel) { this.partnerEnglishLevel = partnerEnglishLevel; }

    @Nullable
    public YesNoUnsure getPartnerIelts() { return partnerIelts; }

    public void setPartnerIelts(@Nullable YesNoUnsure partnerIelts) { this.partnerIelts = partnerIelts; }

    @Nullable
    public IeltsScore getPartnerIeltsScore() { return partnerIeltsScore; }

    public void setPartnerIeltsScore(@Nullable IeltsScore partnerIeltsScore) { this.partnerIeltsScore = partnerIeltsScore; }

    @Nullable
    public Nationality getPartnerCitizenship() { return partnerCitizenship; }

    public void setPartnerCitizenship(@Nullable Nationality partnerCitizenship) { this.partnerCitizenship = partnerCitizenship; }

    @Nullable
    public YesNo getMilitaryService() { return militaryService; }

    public void setMilitaryService(@Nullable YesNo militaryService) { this.militaryService = militaryService; }

    @Nullable
    public YesNo getChildren() { return children; }

    public void setChildren(@Nullable YesNo children) { this.children = children; }

    @Nullable
    public String getChildrenAge() { return childrenAge; }

    public void setChildrenAge(@Nullable String childrenAge) { this.childrenAge = childrenAge; }

    @Nullable
    public YesNoUnsure getVisaReject() { return visaReject; }

    public void setVisaReject(@Nullable YesNoUnsure visaReject) { this.visaReject = visaReject; }

    @Nullable
    public YesNo getHostBorn() { return hostBorn; }

    public void setHostBorn(@Nullable YesNo hostBorn) { this.hostBorn = hostBorn; }

    @Nullable
    public YesNo getCanDrive() { return canDrive; }

    public void setCanDrive(@Nullable YesNo canDrive) { this.canDrive = canDrive; }

    @Nullable
    public DrivingLicenseStatus getDrivingLicense() { return drivingLicense; }

    public void setDrivingLicense(@Nullable DrivingLicenseStatus drivingLicense) { this.drivingLicense = drivingLicense; }

    @Nullable
    public LocalDate getDrivingLicenseExp() { return drivingLicenseExp; }

    public void setDrivingLicenseExp(@Nullable LocalDate drivingLicenseExp) { this.drivingLicenseExp = drivingLicenseExp; }

    @Nullable
    public Country getDrivingLicenseCountry() { return drivingLicenseCountry; }

    public void setDrivingLicenseCountry(@Nullable Country drivingLicenseCountry) { this.drivingLicenseCountry = drivingLicenseCountry; }

    @Nullable
    public String getLangAssessment() { return langAssessment; }

    public void setLangAssessment(@Nullable String langAssessment) { this.langAssessment = langAssessment; }

    @Nullable
    public IeltsScore getLangAssessmentScore() { return langAssessmentScore; }

    public void setLangAssessmentScore(@Nullable IeltsScore langAssessmentScore) { this.langAssessmentScore = langAssessmentScore; }

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

    public void populateIntakeData(CandidateIntakeDataUpdate data,
                                   @Nullable Country workAbroadLoc,
                                   @Nullable Candidate partnerCandidate,
                                   @Nullable EducationLevel partnerEduLevel,
                                   @Nullable Occupation partnerProfession,
                                   @Nullable LanguageLevel partnerEnglishLevel,
                                   @Nullable Nationality partnerCitizenship,
                                   @Nullable Country drivingLicenseCountry) {
        if (data.getAsylumYear() != null) {
            setAsylumYear(data.getAsylumYear());
        }
        if (data.getAvailImmediate() != null) {
            setAvailImmediate(data.getAvailImmediate());
        }
        if (data.getAvailImmediateReason() != null) {
            setAvailImmediateReason(data.getAvailImmediateReason());
        }
        if (data.getAvailImmediateNotes() != null) {
            setAvailImmediateNotes(data.getAvailImmediateNotes());
        }
        if (data.getCanDrive() != null) {
            setCanDrive(data.getCanDrive());
        }
        if (data.getChildren() != null) {
            setChildren(data.getChildren());
        }
        if (data.getChildrenAge() != null) {
            setChildrenAge(data.getChildrenAge());
        }
        if (data.getConflict() != null) {
            setConflict(data.getConflict());
        }
        if (data.getConflictNotes() != null) {
            setConflictNotes(data.getConflictNotes());
        }
        if (data.getCrimeConvict() != null) {
            setCrimeConvict(data.getCrimeConvict());
        }
        if (data.getCrimeConvictNotes() != null) {
            setCrimeConvictNotes(data.getCrimeConvictNotes());
        }
        if (data.getDestLimit() != null) {
            setDestLimit(data.getDestLimit());
        }
        if (data.getDestLimitNotes() != null) {
            setDestLimitNotes(data.getDestLimitNotes());
        }
        if (data.getDestJob() != null) {
            setDestJob(data.getDestJob());
        }
        if (data.getDestJobNotes() != null) {
            setDestJobNotes(data.getDestJobNotes());
        }
        if (data.getDrivingLicense() != null) {
            setDrivingLicense(data.getDrivingLicense());
        }
        if (data.getDrivingLicenseExp() != null) {
            setDrivingLicenseExp(data.getDrivingLicenseExp());
        }
        if (data.getDrivingLicenseCountryId() != null) {
            setDrivingLicenseCountry(drivingLicenseCountry);
        }
        if (data.getFamilyMove() != null) {
            setFamilyMove(data.getFamilyMove());
        }
        if (data.getFamilyMoveNotes() != null) {
            setFamilyMoveNotes(data.getFamilyMoveNotes());
        }
        if (data.getFamilyHealthConcern() != null) {
            setFamilyHealthConcern(data.getFamilyHealthConcern());
        }
        if (data.getFamilyHealthConcernNotes() != null) {
            setFamilyHealthConcernNotes(data.getFamilyHealthConcernNotes());
        }
        if (data.getHomeLocation() != null) {
            setHomeLocation(data.getHomeLocation());
        }
        if (data.getHostChallenges() != null) {
            setHostChallenges(data.getHostChallenges());
        }
        if (data.getHostBorn() != null) {
            setHostBorn(data.getHostBorn());
        }
        if (data.getHostEntryYear() != null) {
            setHostEntryYear(data.getHostEntryYear());
        }
        if (data.getHostEntryLegally() != null) {
            setHostEntryLegally(data.getHostEntryLegally());
        }
        if (data.getIntRecruitReasons() != null) {
            setIntRecruitReasons(data.getIntRecruitReasons());
        }
        if (data.getIntRecruitRural() != null) {
            setIntRecruitRural(data.getIntRecruitRural());
        }
        if (data.getLangAssessment() != null) {
            setLangAssessment(data.getLangAssessment());
        }
        if (data.getLangAssessmentScore() != null) {
            setLangAssessmentScore(data.getLangAssessmentScore());
        }
        if (data.getLeftHomeReason() != null) {
            setLeftHomeReason(data.getLeftHomeReason());
        }
        if (data.getLeftHomeOther() != null) {
            setLeftHomeOther(data.getLeftHomeOther());
        }
        if (data.getMilitaryService() != null) {
            setMilitaryService(data.getMilitaryService());
        }
        if (data.getMaritalStatus() != null) {
            setMaritalStatus(data.getMaritalStatus());
        }
        if (data.getPartnerRegistered() != null) {
            setPartnerRegistered(data.getPartnerRegistered());
        }
        if (data.getPartnerCandId() != null) {
            setPartnerCandidate(partnerCandidate);
        }
        if (data.getPartnerEduLevelId() != null) {
            setPartnerEduLevel(partnerEduLevel);
        }
        if (data.getPartnerProfessionId() != null) {
            setPartnerProfession(partnerProfession);
        }
        if (data.getPartnerEnglish() != null) {
            setPartnerEnglish(data.getPartnerEnglish());
        }
        if (data.getPartnerEnglishLevelId() != null) {
            setPartnerEnglishLevel(partnerEnglishLevel);
        }
        if (data.getPartnerIelts() != null) {
            setPartnerIelts(data.getPartnerIelts());
        }
        if (data.getPartnerIeltsScore() != null) {
            setPartnerIeltsScore(data.getPartnerIeltsScore());
        }
        if (data.getPartnerCitizenshipId() != null) {
            setPartnerCitizenship(partnerCitizenship);
        }
        if (data.getResidenceStatus() != null) {
            setResidenceStatus(data.getResidenceStatus());
        }
        if (data.getReturnedHome() != null) {
            setReturnedHome(data.getReturnedHome());
        }
        if (data.getReturnedHomeNotes() != null) {
            setReturnedHomeNotes(data.getReturnedHomeNotes());
        }
        if (data.getReturnedHomeReason() != null) {
            setReturnedHomeReason(data.getReturnedHomeReason());
        }
        if (data.getReturnHomeSafe() != null) {
            setReturnHomeSafe(data.getReturnHomeSafe());
        }
        if (data.getReturnHomeFuture() != null) {
            setReturnHomeFuture(data.getReturnHomeFuture());
        }
        if (data.getReturnHomeWhen() != null) {
            setReturnHomeWhen(data.getReturnHomeWhen());
        }
        if (data.getResettleThird() != null) {
            setResettleThird(data.getResettleThird());
        }
        if (data.getResettleThirdStatus() != null) {
            setResettleThirdStatus(data.getResettleThirdStatus());
        }
        if (data.getUnhcrStatus() != null) {
            setUnhcrStatus(data.getUnhcrStatus());
        }
        if (data.getUnhcrOldStatus() != null) {
            setUnhcrOldStatus(data.getUnhcrOldStatus());
        }
        if (data.getUnhcrNumber() != null) {
            setUnhcrNumber(data.getUnhcrNumber());
        }
        if (data.getUnhcrFile() != null) {
            setUnhcrFile(data.getUnhcrFile());
        }
        if (data.getUnhcrNotes() != null) {
            setUnhcrNotes(data.getUnhcrNotes());
        }
        if (data.getUnhcrPermission() != null) {
            setUnhcrPermission(data.getUnhcrPermission());
        }
        if (data.getUnrwaStatus() != null) {
            setUnrwaStatus(data.getUnrwaStatus());
        }
        if (data.getUnrwaNumber() != null) {
            setUnrwaNumber(data.getUnrwaNumber());
        }
        if (data.getUnrwaNotes() != null) {
            setUnrwaNotes(data.getUnrwaNotes());
        }
        if (data.getVisaReject() != null) {
            setVisaReject(data.getVisaReject());
        }
        if (data.getVisaIssues() != null) {
            setVisaIssues(data.getVisaIssues());
        }
        if (data.getVisaIssuesNotes() != null) {
            setVisaIssuesNotes(data.getVisaIssuesNotes());
        }
        if (data.getWorkAbroad() != null) {
            setWorkAbroad(data.getWorkAbroad());
        }
        if (data.getWorkAbroadLocId() != null) {
            setWorkAbroadLoc(workAbroadLoc);
        }
        if (data.getWorkAbroadYrs() != null) {
            setWorkAbroadYrs(data.getWorkAbroadYrs());
        }
        if (data.getWorkPermit() != null) {
            setWorkPermit(data.getWorkPermit());
        }
        if (data.getWorkPermitDesired() != null) {
            setWorkPermitDesired(data.getWorkPermitDesired());
        }
        if (data.getWorkLegally() != null) {
            setWorkLegally(data.getWorkLegally());
        }

    }
}
