/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tbbtalent.server.model.db;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.annotations.Formula;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.api.admin.SavedSearchAdminApi;
import org.tbbtalent.server.model.es.CandidateEs;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.impl.SalesforceServiceImpl;

import javax.persistence.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    private String linkedInLink;
    
    @Nullable
    private String shareableNotes;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shareable_cv_attachment_id")
    private CandidateAttachment shareableCv;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shareable_doc_attachment_id")
    private CandidateAttachment shareableDoc;

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
    private Country nationality;

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
    private String returnedHomeReason;

    @Nullable
    private String returnedHomeReasonNo;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure visaIssues;
    
    @Nullable
    private String visaIssuesNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure availImmediate;

    @Nullable
    private String availImmediateJobOps;

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

    @Convert(converter = IntRecruitReasonConverter.class)
    @Nullable
    private List<IntRecruitReason> intRecruitReasons;

    @Nullable
    private String intRecruitOther;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure intRecruitRural;

    @Nullable
    private String intRecruitRuralNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure returnHomeSafe;

    @Enumerated(EnumType.STRING)
    @Nullable
    private WorkPermit workPermit;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure workPermitDesired;

    @Nullable
    private String workPermitDesiredNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnemployedOther workDesired;

    @Nullable
    private String workDesiredNotes;

    @Nullable
    private Long hostEntryYear;

    @Nullable
    private String hostEntryYearNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure unhcrRegistered;

    @Enumerated(EnumType.STRING)
    @Nullable
    private UnhcrStatus unhcrStatus;

    @Nullable
    private String unhcrNumber;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo unhcrConsent;

    @Nullable
    private Long unhcrFile;

    @Enumerated(EnumType.STRING)
    @Nullable
    private NotRegisteredStatus unhcrNotRegStatus;

    @Nullable
    private String unhcrNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure unrwaRegistered;

    @Nullable
    private String unrwaNumber;

    @Nullable
    private Long unrwaFile;

    @Enumerated(EnumType.STRING)
    @Nullable
    private NotRegisteredStatus unrwaNotRegStatus;

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

    @Nullable
    private String residenceStatusNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo workAbroad;

    @Nullable
    private String workAbroadNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo hostEntryLegally;

    @Nullable
    private String hostEntryLegallyNotes;

    @Convert(converter = LeftHomeReasonsConverter.class)
    @Nullable
    private List<LeftHomeReason> leftHomeReasons;

    @Nullable
    private String leftHomeNotes;

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

    @Nullable
    private String maritalStatusNotes;

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

    @Nullable
    private String partnerEduLevelNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_occupation_id")
    @Nullable
    private Occupation partnerOccupation;

    @Nullable
    private String partnerOccupationNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo partnerEnglish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_english_level_id")
    @Nullable
    private LanguageLevel partnerEnglishLevel;

    @Enumerated(EnumType.STRING)
    @Nullable
    private IeltsStatus partnerIelts;

    @Nullable
    private String partnerIeltsScore;

    @Nullable
    private Long partnerIeltsYr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_citizenship_id")
    @Nullable
    private Country partnerCitizenship;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo militaryService;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure militaryWanted;

    @Nullable
    private String militaryNotes;

    @Nullable
    private LocalDate militaryStart;

    @Nullable
    private LocalDate militaryEnd;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNoUnsure visaReject;

    @Nullable
    private String visaRejectNotes;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo canDrive;

    @Enumerated(EnumType.STRING)
    @Nullable
    private DocumentStatus drivingLicense;

    @Nullable
    private LocalDate drivingLicenseExp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driving_license_country_id")
    @Nullable
    private Country drivingLicenseCountry;

    @Nullable
    private String langAssessment;

    @Nullable
    private String langAssessmentScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birth_country_id")
    @Nullable
    private Country birthCountry;

    @Nullable
    private BigDecimal ieltsScore;

    @Formula("(SELECT COUNT(cd.id) FROM candidate c inner join candidate_dependant cd on c.id = cd.candidate_id where c.id = id group by c.id)")
    private Long numberDependants;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo healthIssues;

    @Nullable
    private String healthIssuesNotes;

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

    @Nullable
    public Object extractField(String exportField)
        throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Object obj;        
        try {
            obj = PropertyUtils.getProperty(this, exportField);
        } catch (NestedNullException ex) {
            //Return null if any value in nested reference is null.
            //For example user.email should return null if user is null.
            obj = null;
        }
        if (obj instanceof User) {
            obj = ((User) obj).getDisplayName();
        } else if (obj != null && "candidateNumber".equals(exportField)) {
            //Convert candidateNumber to a number
            obj = Long.parseLong((String) obj);
        } else if (obj instanceof Enum) {
            obj = ((Enum<?>) obj).name();
        }
        return obj;
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
     * Used alongside @Formula for updating the elasticsearch record of number of
     * dependants.
     * @return Long of total up-to-date number of dependants belonging to Candidate.
     */
    @Transient
    public Long getNumberDependants() {
        if (candidateDependants != null && !candidateDependants.isEmpty()) {
            Long numberDeps = 0L;
            for (CandidateDependant cd : candidateDependants) {
                numberDeps ++;
            }
            return numberDeps;
        }
        return null;
    }

    /**
     * Candidates can have special values associated with a particular 
     * savedList.
     * These values are stored in {@link CandidateSavedList}.
     * Setting this value to refer to a particular SavedList will result in
     * this Candidate object returning attributes corresponding to that list.
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

    public String getLinkedInLink() { return linkedInLink; }

    public void setLinkedInLink(String linkedInLink) { this.linkedInLink = linkedInLink; }

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

    public Country getNationality() {
        return nationality;
    }

    public void setNationality(Country nationality) {
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
    public String getReturnedHomeReason() {
        return returnedHomeReason;
    }

    public void setReturnedHomeReason(@Nullable String returnedHomeReason) { this.returnedHomeReason = returnedHomeReason; }

    @Nullable
    public String getReturnedHomeReasonNo() { return returnedHomeReasonNo; }

    public void setReturnedHomeReasonNo(@Nullable String returnedHomeReasonNo) { this.returnedHomeReasonNo = returnedHomeReasonNo; }

    @Nullable
    public YesNoUnsure getVisaIssues() {
        return visaIssues;
    }

    public void setVisaIssues(@Nullable YesNoUnsure visaIssues) {
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
    public String getAvailImmediateJobOps() { return availImmediateJobOps; }

    public void setAvailImmediateJobOps(@Nullable String availImmediateJobOps) { this.availImmediateJobOps = availImmediateJobOps; }

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
    public List<IntRecruitReason> getIntRecruitReasons() { return intRecruitReasons; }

    public void setIntRecruitReasons(@Nullable List<IntRecruitReason> intRecruitReasons) { this.intRecruitReasons = intRecruitReasons; }

    @Nullable
    public String getIntRecruitOther() { return intRecruitOther; }

    public void setIntRecruitOther(@Nullable String intRecruitOther) { this.intRecruitOther = intRecruitOther; }

    @Nullable
    public YesNoUnsure getIntRecruitRural() { return intRecruitRural; }

    public void setIntRecruitRural(@Nullable YesNoUnsure intRecruitRural) { this.intRecruitRural = intRecruitRural; }

    @Nullable
    public String getIntRecruitRuralNotes() { return intRecruitRuralNotes; }

    public void setIntRecruitRuralNotes(@Nullable String intRecruitRuralNotes) { this.intRecruitRuralNotes = intRecruitRuralNotes; }

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
    public String getWorkPermitDesiredNotes() { return workPermitDesiredNotes; }

    public void setWorkPermitDesiredNotes(@Nullable String workPermitDesiredNotes) { this.workPermitDesiredNotes = workPermitDesiredNotes; }

    @Nullable
    public YesNoUnemployedOther getWorkDesired() { return workDesired; }

    public void setWorkDesired(@Nullable YesNoUnemployedOther workDesired) { this.workDesired = workDesired; }

    @Nullable
    public String getWorkDesiredNotes() { return workDesiredNotes; }

    public void setWorkDesiredNotes(@Nullable String workDesiredNotes) { this.workDesiredNotes = workDesiredNotes; }

    @Nullable
    public Long getHostEntryYear() { return hostEntryYear; }

    public void setHostEntryYear(@Nullable Long hostEntryYear) { this.hostEntryYear = hostEntryYear; }

    @Nullable
    public String getHostEntryYearNotes() { return hostEntryYearNotes; }

    public void setHostEntryYearNotes(@Nullable String hostEntryYearNotes) { this.hostEntryYearNotes = hostEntryYearNotes; }

    @Nullable
    public YesNoUnsure getUnhcrRegistered() { return unhcrRegistered; }

    public void setUnhcrRegistered(@Nullable YesNoUnsure unhcrRegistered) { this.unhcrRegistered = unhcrRegistered; }

    @Nullable
    public UnhcrStatus getUnhcrStatus() { return unhcrStatus; }

    public void setUnhcrStatus(@Nullable UnhcrStatus unhcrStatus) { this.unhcrStatus = unhcrStatus; }

    @Nullable
    public String getUnhcrNumber() { return unhcrNumber; }

    public void setUnhcrNumber(@Nullable String unhcrNumber) { this.unhcrNumber = unhcrNumber; }

    @Nullable
    public YesNo getUnhcrConsent() {return unhcrConsent;}

    public void setUnhcrConsent(@Nullable YesNo unhcrConsent) {
        this.unhcrConsent = unhcrConsent;
    }

    @Nullable
    public Long getUnhcrFile() { return unhcrFile; }

    public void setUnhcrFile(@Nullable Long unhcrFile) { this.unhcrFile = unhcrFile; }

    @Nullable
    public NotRegisteredStatus getUnhcrNotRegStatus() { return unhcrNotRegStatus; }

    public void setUnhcrNotRegStatus(@Nullable NotRegisteredStatus unhcrNotRegStatus) { this.unhcrNotRegStatus = unhcrNotRegStatus; }

    @Nullable
    public String getUnhcrNotes() { return unhcrNotes; }

    public void setUnhcrNotes(@Nullable String unhcrNotes) { this.unhcrNotes = unhcrNotes; }

    @Nullable
    public YesNoUnsure getUnrwaRegistered() { return unrwaRegistered; }

    public void setUnrwaRegistered(@Nullable YesNoUnsure unrwaRegistered) { this.unrwaRegistered = unrwaRegistered; }

    @Nullable
    public String getUnrwaNumber() { return unrwaNumber; }

    public void setUnrwaNumber(@Nullable String unrwaNumber) { this.unrwaNumber = unrwaNumber; }

    @Nullable
    public Long getUnrwaFile() { return unrwaFile; }

    public void setUnrwaFile(@Nullable Long unrwaFile) { this.unrwaFile = unrwaFile; }

    @Nullable
    public NotRegisteredStatus getUnrwaNotRegStatus() { return unrwaNotRegStatus; }

    public void setUnrwaNotRegStatus(@Nullable NotRegisteredStatus unrwaNotRegStatus) { this.unrwaNotRegStatus = unrwaNotRegStatus; }

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
    public String getResidenceStatusNotes() { return residenceStatusNotes; }

    public void setResidenceStatusNotes(@Nullable String residenceStatusNotes) { this.residenceStatusNotes = residenceStatusNotes; }

    @Nullable
    public YesNo getWorkAbroad() { return workAbroad; }

    public void setWorkAbroad(@Nullable YesNo workAbroad) { this.workAbroad = workAbroad; }

    @Nullable
    public String getWorkAbroadNotes() { return workAbroadNotes; }

    public void setWorkAbroadNotes(@Nullable String workAbroadNotes) { this.workAbroadNotes = workAbroadNotes; }

    @Nullable
    public YesNo getHostEntryLegally() { return hostEntryLegally; }

    public void setHostEntryLegally(@Nullable YesNo hostEntryLegally) { this.hostEntryLegally = hostEntryLegally; }

    @Nullable
    public String getHostEntryLegallyNotes() { return hostEntryLegallyNotes; }

    public void setHostEntryLegallyNotes(@Nullable String hostEntryLegallyNotes) { this.hostEntryLegallyNotes = hostEntryLegallyNotes; }

    @Nullable
    public List<LeftHomeReason> getLeftHomeReasons() { return leftHomeReasons; }

    public void setLeftHomeReasons(@Nullable List<LeftHomeReason> leftHomeReasons) { this.leftHomeReasons = leftHomeReasons; }

    @Nullable
    public String getLeftHomeNotes() { return leftHomeNotes; }

    public void setLeftHomeNotes(@Nullable String leftHomeNotes) { this.leftHomeNotes = leftHomeNotes; }

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
    public String getMaritalStatusNotes() { return maritalStatusNotes; }

    public void setMaritalStatusNotes(@Nullable String maritalStatusNotes) { this.maritalStatusNotes = maritalStatusNotes; }

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
    public String getPartnerEduLevelNotes() { return partnerEduLevelNotes; }

    public void setPartnerEduLevelNotes(@Nullable String partnerEduLevelNotes) { this.partnerEduLevelNotes = partnerEduLevelNotes; }

    @Nullable
    public Occupation getPartnerOccupation() { return partnerOccupation; }

    public void setPartnerOccupation(@Nullable Occupation partnerOccupation) { this.partnerOccupation = partnerOccupation; }

    @Nullable
    public String getPartnerOccupationNotes() { return partnerOccupationNotes; }

    public void setPartnerOccupationNotes(@Nullable String partnerOccupationNotes) { this.partnerOccupationNotes = partnerOccupationNotes; }

    @Nullable
    public YesNo getPartnerEnglish() { return partnerEnglish; }

    public void setPartnerEnglish(@Nullable YesNo partnerEnglish) { this.partnerEnglish = partnerEnglish; }

    @Nullable
    public LanguageLevel getPartnerEnglishLevel() { return partnerEnglishLevel; }

    public void setPartnerEnglishLevel(@Nullable LanguageLevel partnerEnglishLevel) { this.partnerEnglishLevel = partnerEnglishLevel; }

    @Nullable
    public IeltsStatus getPartnerIelts() { return partnerIelts; }

    public void setPartnerIelts(@Nullable IeltsStatus partnerIelts) { this.partnerIelts = partnerIelts; }

    @Nullable
    public String getPartnerIeltsScore() { return partnerIeltsScore; }

    public void setPartnerIeltsScore(@Nullable String partnerIeltsScore) { this.partnerIeltsScore = partnerIeltsScore; }

    @Nullable
    public Long getPartnerIeltsYr() { return partnerIeltsYr; }

    public void setPartnerIeltsYr(@Nullable Long partnerIeltsYr) { this.partnerIeltsYr = partnerIeltsYr; }

    @Nullable
    public Country getPartnerCitizenship() { return partnerCitizenship; }

    public void setPartnerCitizenship(@Nullable Country partnerCitizenship) { this.partnerCitizenship = partnerCitizenship; }

    @Nullable
    public YesNo getMilitaryService() { return militaryService; }

    public void setMilitaryService(@Nullable YesNo militaryService) { this.militaryService = militaryService; }

    @Nullable
    public YesNoUnsure getMilitaryWanted() { return militaryWanted; }

    public void setMilitaryWanted(@Nullable YesNoUnsure militaryWanted) { this.militaryWanted = militaryWanted; }

    @Nullable
    public String getMilitaryNotes() { return militaryNotes; }

    public void setMilitaryNotes(@Nullable String militaryNotes) { this.militaryNotes = militaryNotes; }

    @Nullable
    public LocalDate getMilitaryStart() { return militaryStart; }

    public void setMilitaryStart(@Nullable LocalDate militaryStart) { this.militaryStart = militaryStart; }

    @Nullable
    public LocalDate getMilitaryEnd() { return militaryEnd; }

    public void setMilitaryEnd(@Nullable LocalDate militaryEnd) { this.militaryEnd = militaryEnd; }

    @Nullable
    public YesNoUnsure getVisaReject() { return visaReject; }

    public void setVisaReject(@Nullable YesNoUnsure visaReject) { this.visaReject = visaReject; }

    @Nullable
    public String getVisaRejectNotes() { return visaRejectNotes; }

    public void setVisaRejectNotes(@Nullable String visaRejectNotes) { this.visaRejectNotes = visaRejectNotes; }

    @Nullable
    public YesNo getCanDrive() { return canDrive; }

    public void setCanDrive(@Nullable YesNo canDrive) { this.canDrive = canDrive; }

    @Nullable
    public DocumentStatus getDrivingLicense() { return drivingLicense; }

    public void setDrivingLicense(@Nullable DocumentStatus drivingLicense) { this.drivingLicense = drivingLicense; }

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
    public String getLangAssessmentScore() { return langAssessmentScore; }

    public void setLangAssessmentScore(@Nullable String langAssessmentScore) { this.langAssessmentScore = langAssessmentScore; }

    @Nullable
    public BigDecimal getIeltsScore() {
        return ieltsScore;
    }

    public void setIeltsScore(@Nullable BigDecimal ieltsScore) {this.ieltsScore = ieltsScore;}

    @Nullable
    public Country getBirthCountry() { return birthCountry; }

    public void setBirthCountry(@Nullable Country birthCountry) { this.birthCountry = birthCountry; }

    @Nullable
    public YesNo getHealthIssues() {return healthIssues;}

    public void setHealthIssues(@Nullable YesNo healthIssues) {this.healthIssues = healthIssues;}

    @Nullable
    public String getHealthIssuesNotes() {return healthIssuesNotes;}

    public void setHealthIssuesNotes(@Nullable String healthIssuesNotes) {this.healthIssuesNotes = healthIssuesNotes;}

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
    public String getShareableNotes() {
        return shareableNotes;
    }

    public void setShareableNotes(@Nullable String shareableNotes) {
        this.shareableNotes = shareableNotes;
    }

    @Nullable
    public CandidateAttachment getShareableCv() {
        return shareableCv;
    }

    public void setShareableCv(@Nullable CandidateAttachment shareableCv) {
        this.shareableCv = shareableCv;
    }

    @Nullable
    public CandidateAttachment getShareableDoc() {
        return shareableDoc;
    }

    public void setShareableDoc(@Nullable CandidateAttachment shareableDoc) {
        this.shareableDoc = shareableDoc;
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
                                   @Nullable Candidate partnerCandidate,
                                   @Nullable EducationLevel partnerEduLevel,
                                   @Nullable Occupation partnerOccupation,
                                   @Nullable LanguageLevel partnerEnglishLevel,
                                   @Nullable Country partnerCitizenship,
                                   @Nullable Country drivingLicenseCountry,
                                   @Nullable Country birthCountry) {
        if (data.getAsylumYear() != null) {
            setAsylumYear(data.getAsylumYear());
        }
        if (data.getAvailImmediate() != null) {
            setAvailImmediate(data.getAvailImmediate());
        }
        if (data.getAvailImmediateJobOps() != null) {
            setAvailImmediateJobOps(data.getAvailImmediateJobOps());
        }
        if (data.getAvailImmediateReason() != null) {
            setAvailImmediateReason(data.getAvailImmediateReason());
        }
        if (data.getAvailImmediateNotes() != null) {
            setAvailImmediateNotes(data.getAvailImmediateNotes());
        }
        if (data.getBirthCountryId() != null) {
            setBirthCountry(birthCountry);
        }
        if (data.getCanDrive() != null) {
            setCanDrive(data.getCanDrive());
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
        if (data.getHealthIssues() != null) {
            setHealthIssues(data.getHealthIssues());
        }
        if (data.getHealthIssuesNotes() != null) {
            setHealthIssuesNotes(data.getHealthIssuesNotes());
        }
        if (data.getHomeLocation() != null) {
            setHomeLocation(data.getHomeLocation());
        }
        if (data.getHostChallenges() != null) {
            setHostChallenges(data.getHostChallenges());
        }
        if (data.getHostEntryYear() != null) {
            setHostEntryYear(data.getHostEntryYear());
        }
        if (data.getHostEntryYearNotes() != null) {
            setHostEntryYearNotes(data.getHostEntryYearNotes());
        }
        if (data.getHostEntryLegally() != null) {
            setHostEntryLegally(data.getHostEntryLegally());
        }
        if (data.getHostEntryLegallyNotes() != null) {
            setHostEntryLegallyNotes(data.getHostEntryLegallyNotes());
        }
        if (data.getIntRecruitReasons() != null) {
            setIntRecruitReasons(data.getIntRecruitReasons());
        }
        if (data.getIntRecruitOther() != null) {
            setIntRecruitOther(data.getIntRecruitOther());
        }
        if (data.getIntRecruitRural() != null) {
            setIntRecruitRural(data.getIntRecruitRural());
        }
        if (data.getIntRecruitRuralNotes() != null) {
            setIntRecruitRuralNotes(data.getIntRecruitRuralNotes());
        }
        if (data.getLangAssessment() != null) {
            setLangAssessment(data.getLangAssessment());
        }

        if (data.getLangAssessmentScore() != null) {
            BigDecimal score;
            // If the LangAssessmentScore is NoResponse set to null in database.
            if (data.getLangAssessmentScore().equals("NoResponse")) {
                setLangAssessmentScore(null);
                score = null;
            } else {
                setLangAssessmentScore(data.getLangAssessmentScore());
                score = new BigDecimal(data.getLangAssessmentScore());
            }
            // If no IeltsGen exam exists, the ielts score comes from the lang assessment score and needs to be updated here.
            if (!hasIelts()) {
                setIeltsScore(score);
            }
        }

        if (data.getLeftHomeReasons() != null) {
            setLeftHomeReasons(data.getLeftHomeReasons());
        }
        if (data.getLeftHomeNotes() != null) {
            setLeftHomeNotes(data.getLeftHomeNotes());
        }
        if (data.getMilitaryService() != null) {
            setMilitaryService(data.getMilitaryService());
        }
        if (data.getMilitaryWanted() != null) {
            setMilitaryWanted(data.getMilitaryWanted());
        }
        if (data.getMilitaryNotes() != null) {
            setMilitaryNotes(data.getMilitaryNotes());
        }
        if (data.getMilitaryStart() != null) {
            setMilitaryStart(data.getMilitaryStart());
        }
        if (data.getMilitaryEnd() != null) {
            setMilitaryEnd(data.getMilitaryEnd());
        }
        if (data.getMaritalStatus() != null) {
            setMaritalStatus(data.getMaritalStatus());
        }
        if (data.getMaritalStatusNotes() != null) {
            setMaritalStatusNotes(data.getMaritalStatusNotes());
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
        if (data.getPartnerEduLevelNotes() != null) {
            setPartnerEduLevelNotes(data.getPartnerEduLevelNotes());
        }
        if (data.getPartnerOccupationId() != null) {
            setPartnerOccupation(partnerOccupation);
        }
        if (data.getPartnerOccupationNotes() != null) {
            setPartnerOccupationNotes(data.getPartnerOccupationNotes());
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
        if (data.getPartnerIeltsYr() != null) {
            setPartnerIeltsYr(data.getPartnerIeltsYr());
        }
        if (data.getPartnerCitizenshipId() != null) {
            setPartnerCitizenship(partnerCitizenship);
        }
        if (data.getResidenceStatus() != null) {
            setResidenceStatus(data.getResidenceStatus());
        }
        if (data.getResidenceStatusNotes() != null) {
            setResidenceStatusNotes(data.getResidenceStatusNotes());
        }
        if (data.getReturnedHome() != null) {
            setReturnedHome(data.getReturnedHome());
        }
        if (data.getReturnedHomeReason() != null) {
            setReturnedHomeReason(data.getReturnedHomeReason());
        }
        if (data.getReturnedHomeReasonNo() != null) {
            setReturnedHomeReasonNo(data.getReturnedHomeReasonNo());
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
        if (data.getUnhcrRegistered() != null) {
            setUnhcrRegistered(data.getUnhcrRegistered());
        }
        if (data.getUnhcrStatus() != null) {
            setUnhcrStatus(data.getUnhcrStatus());
        }
        if (data.getUnhcrNotRegStatus() != null) {
            setUnhcrNotRegStatus(data.getUnhcrNotRegStatus());
        }
        if (data.getUnhcrNumber() != null) {
            setUnhcrNumber(data.getUnhcrNumber());
        }
        if (data.getUnhcrFile() != null) {
            setUnhcrFile(data.getUnhcrFile());
        }
        if (data.getUnhcrConsent() != null) {
            setUnhcrConsent(data.getUnhcrConsent());
        }
        if (data.getUnhcrNotes() != null) {
            setUnhcrNotes(data.getUnhcrNotes());
        }
        if (data.getUnrwaRegistered() != null) {
            setUnrwaRegistered(data.getUnrwaRegistered());
        }
        if (data.getUnrwaNumber() != null) {
            setUnrwaNumber(data.getUnrwaNumber());
        }
        if (data.getUnrwaFile() != null) {
            setUnrwaFile(data.getUnrwaFile());
        }
        if (data.getUnrwaNotRegStatus() != null) {
            setUnrwaNotRegStatus(data.getUnrwaNotRegStatus());
        }
        if (data.getUnrwaNotes() != null) {
            setUnrwaNotes(data.getUnrwaNotes());
        }
        if (data.getVisaReject() != null) {
            setVisaReject(data.getVisaReject());
        }
        if (data.getVisaRejectNotes() != null) {
            setVisaRejectNotes(data.getVisaRejectNotes());
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
        if (data.getWorkAbroadNotes() != null) {
            setWorkAbroadNotes(data.getWorkAbroadNotes());
        }
        if (data.getWorkPermit() != null) {
            setWorkPermit(data.getWorkPermit());
        }
        if (data.getWorkPermitDesired() != null) {
            setWorkPermitDesired(data.getWorkPermitDesired());
        }
        if (data.getWorkPermitDesiredNotes() != null) {
            setWorkPermitDesiredNotes(data.getWorkPermitDesiredNotes());
        }
        if (data.getWorkDesired() != null) {
            setWorkDesired(data.getWorkDesired());
        }
        if (data.getWorkDesiredNotes() != null) {
            setWorkDesiredNotes(data.getWorkDesiredNotes());
        }

    }

    private boolean hasIelts() {
        return candidateExams.stream().filter(ce -> Objects.nonNull(ce.getExam())).anyMatch(ce -> ce.getExam().equals(Exam.IELTSGen));
    }
}
