/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.model.db;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.Formula;
import org.springframework.lang.Nullable;
import org.tctalent.server.api.admin.SavedSearchAdminApi;
import org.tctalent.server.api.admin.SystemAdminApi;
import org.tctalent.server.configuration.SystemAdminConfiguration;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.es.CandidateEs;
import org.tctalent.server.service.db.BackgroundProcessingService;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.util.SalesforceHelper;

@Entity
@Table(name = "candidate")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_id_seq", allocationSize = 1)
@Slf4j
public class Candidate extends AbstractAuditableDomainObject<Long> implements HasPublicId {

    private String candidateNumber;
    private String publicId;

    /**
     * Privacy policy that candidate has accepted
     */
    private String acceptedPrivacyPolicyId;

    /**
     * Date time when candidate accepted privacy policy
     */
    private OffsetDateTime acceptedPrivacyPolicyDate;

    /**
     * Partner associated with the accepted privacy policy.
     * Nullable because it may not be set initially.
     */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_privacy_policy_partner_id")
    private PartnerImpl acceptedPrivacyPolicyPartner;

    /**
     * True if candidate wants to receive all notifications.
     * If false, the candidate will only receive notifications when they are well progressed in
     * a job opportunity.
     */
    private boolean allNotifications;

    @Transient
    private Long contextSavedListId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidateId", cascade = CascadeType.MERGE)
    private Set<CandidateProperty> candidateProperties;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    @OrderBy("activatedDate DESC")
    private List<TaskAssignmentImpl> taskAssignments;

    private String phone;
    private String whatsapp;

    @Enumerated(EnumType.STRING)
    @Nullable
    private Gender gender;
    private LocalDate dob;
    private String address1;
    private String city;
    private String state;
    private Integer yearOfArrival;
    private String additionalInfo;
    private String candidateMessage;
    private String linkedInLink;

    /**
     * If true, candidate cannot post to chats. Effectively any chats they see are read only.
     * <p/>
     * A candidate can be muted by their source partner if they are not respecting the TC's chat
     * rules. They can subsequently be unmuted.
     */
    private boolean muted;

    /**
     * Indicates whether the user's password requires update.
     * If true, the candidate will be required to change their password upon their next login.
     * This is particularly useful for candidates who are automatically registered by a third party
     * and assigned a temporary password. When the candidate logs in for the first time, they will
     * be prompted to change their password.
     */
    @Column(name = "change_password" , nullable = false)
    private boolean changePassword;

    /**
     * Candidate's internal id reference with the source partner handling their case.
     * This is the reference used to identify the candidate on the source partner's internal systems.
     * <p/>
     * Some partners may choose to use the candidateNumber field as their own internal reference
     * identifying this candidate. See Partner#isDefaultPartnerRef
     */
    @Nullable
    private String partnerRef;

    /**
     * This can be set to define an optional ranking associated with the candidate as a result
     * of some kind of sorting logic.
     */
    @Transient
    @Nullable
    private Number rank;

    /**
     * If null the candidate registered themselves.
     * If not null, the candidate was registered through the public API by the given partner.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by")
    @Nullable
    private PartnerImpl registeredBy;

    /**
     * IP address of candidate when they registered
     */
    @Nullable
    private String regoIp;

    /**
     * Partner query parameter (p=) associated with candidate on registration
     */
    @Nullable
    private String regoPartnerParam;

    /**
     * Referrer query parameter (r=) associated with candidate on registration
     */
    @Nullable
    private String regoReferrerParam;

    /**
     * Utm query parameter associated with candidate on registration
     */
    @Nullable
    private String regoUtmCampaign;

    /**
     * Utm query parameter associated with candidate on registration
     */
    @Nullable
    private String regoUtmContent;

    /**
     * Utm query parameter associated with candidate on registration
     */
    @Nullable
    private String regoUtmMedium;

    /**
     * Utm query parameter associated with candidate on registration
     */
    @Nullable
    private String regoUtmSource;

    /**
     * Utm query parameter associated with candidate on registration
     */
    @Nullable
    private String regoUtmTerm;

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
     * Computed field of all searchable text associated with the candidate.
     * <p/>
     * Updated in {@link #updateText}
     */
    private String text;

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

    //EAGER loading here reduces number of DB accesses
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nationality_id")
    private Country nationality;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateOccupation> candidateOccupations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    @OrderBy("updatedDate DESC")
    private List<CandidateNote> candidateNotes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    @OrderBy("yearCompleted DESC")
    private List<CandidateEducation> candidateEducations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateLanguage> candidateLanguages;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    @OrderBy("startDate DESC")
    private List<CandidateJobExperience> candidateJobExperiences;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    @OrderBy("dateCompleted DESC")
    private List<CandidateCertification> candidateCertifications;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<CandidateReviewStatusItem> candidateReviewStatusItems;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateSkill> candidateSkills;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateAttachment> candidateAttachments;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateOpportunity> candidateOpportunities;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlink;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkAddress;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkCharacter;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkEmployer;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkEngagement;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkExperience;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkFamily;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkIdentity;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkImmigration;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkLanguage;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkMedical;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkQualification;

    /**
     * Url link to corresponding candidate folder on Google Drive, if one exists.
     */
    @Nullable
    private String folderlinkRegistration;

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

    /**
     * Date that candidate will become available for international opportunities.
     */
    @Nullable
    private LocalDate availDate;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo availImmediate;

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


    /**
     * Computed from {@link #unhcrStatus}
     */
    @Transient
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
    private YesNoUnsure arrestImprison;

    @Nullable
    private String arrestImprisonNotes;

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
    private YesNo monitoringEvaluationConsent;

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

    @Nullable
    private String partnerCitizenship;

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
    private String englishAssessment;

    @Nullable
    private String englishAssessmentScoreIelts;

    @Nullable
    private Long englishAssessmentScoreDet;

    @Nullable
    private String frenchAssessment;

    @Nullable
    private Long frenchAssessmentScoreNclc;

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

    @Nullable
    private String externalId;

    @Nullable
    private String externalIdSource;

    @Enumerated(EnumType.STRING)
    @Nullable
    private YesNo covidVaccinated;

    @Enumerated(EnumType.STRING)
    @Nullable
    private VaccinationStatus covidVaccinatedStatus;

    @Nullable
    private LocalDate covidVaccinatedDate;

    @Nullable
    private String covidVaccineName;

    @Nullable
    private String covidVaccineNotes;

    @Nullable
    private String mediaWillingness;

    @NotNull
    private Boolean contactConsentRegistration;

    /**
     * This field is only used for candidates who have just agreed to the old TBB Privacy Policy.
     * It is not used for candidates who have accepted the new Privacy Policy terms because those
     * terms cover this consent to contact candidates about any opportunities.
     */
    private boolean contactConsentPartners = true;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mini_intake_completed_by")
    private User miniIntakeCompletedBy;

    @Nullable
    @Column(name = "mini_intake_completed_date")
    private OffsetDateTime miniIntakeCompletedDate;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "full_intake_completed_by")
    private User fullIntakeCompletedBy;

    @Nullable
    @Column(name = "full_intake_completed_date")
    private OffsetDateTime fullIntakeCompletedDate;

    @Nullable
    private String relocatedAddress;

    @Nullable
    private String relocatedCity;

    @Nullable
    private String relocatedState;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relocated_country_id")
    private Country relocatedCountry;

    @Nullable
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DuolingoCoupon> coupons = new ArrayList<>();
    public Candidate() {
    }

    /**
     * Candidate has been identified as a potential duplicate of another - monitored by daily
     * scheduled call to {@link BackgroundProcessingService#processPotentialDuplicateCandidates()},
     * which can also be manually triggered from {@link SystemAdminApi}.
     */
    private boolean potentialDuplicate;

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
        } else if (obj != null && obj.getClass().equals(LocalDate.class)) {
            //Convert all local dates to a string
            obj = ((LocalDate) obj).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
        } else if (obj != null && ("phone".equals(exportField) || "whatsapp".equals(exportField))) {
            // Need to add a ' before the number in order to keep any starting '0's in the number
            obj = "'" + obj;
        } else if (obj instanceof Enum) {
            obj = ((Enum<?>) obj).name();
        }
        return obj;
    }

    public String getAcceptedPrivacyPolicyId() {
        return acceptedPrivacyPolicyId;
    }

    public void setAcceptedPrivacyPolicyId(String acceptedPrivacyPolicyId) {
        this.acceptedPrivacyPolicyId = acceptedPrivacyPolicyId;
    }

    public OffsetDateTime getAcceptedPrivacyPolicyDate() {
        return acceptedPrivacyPolicyDate;
    }

    public void setAcceptedPrivacyPolicyDate(OffsetDateTime acceptedPrivacyPolicyDate) {
        this.acceptedPrivacyPolicyDate = acceptedPrivacyPolicyDate;
    }

    public PartnerImpl getAcceptedPrivacyPolicyPartner() {
        return acceptedPrivacyPolicyPartner;
    }

    public void setAcceptedPrivacyPolicyPartner(PartnerImpl acceptedPrivacyPolicyPartner) {
        this.acceptedPrivacyPolicyPartner = acceptedPrivacyPolicyPartner;
    }

    public String getCandidateNumber() {
        return candidateNumber;
    }

    public void setCandidateNumber(String candidateNumber) {
        this.candidateNumber = candidateNumber;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
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

    @Transient
    @Nullable
    public CandidateAttachment getListShareableCv() {
        CandidateAttachment listShareableCv = null;
        if (contextSavedListId != null) {
            for (CandidateSavedList csl : candidateSavedLists) {
                if (contextSavedListId.equals(csl.getSavedList().getId())) {
                    listShareableCv = csl.getShareableCv();
                    break;
                }
            }
        }
        return listShareableCv;
    }

    @Transient
    @Nullable
    public CandidateAttachment getListShareableDoc() {
        CandidateAttachment listShareableDoc = null;
        if (contextSavedListId != null) {
            for (CandidateSavedList csl : candidateSavedLists) {
                if (contextSavedListId.equals(csl.getSavedList().getId())) {
                    listShareableDoc = csl.getShareableDoc();
                    break;
                }
            }
        }
        return listShareableDoc;
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

    // todo : is there a better way to get this value for published doc by consolidating into one method and passing in a Exam type?
    public String getOetOverall() {
        String score = null;
        for (CandidateExam exam : candidateExams) {
            if (exam.getExam().equals(Exam.OET)) {
                score = exam.getScore();
                break;
            }
        }
        return score;
    }
    public String getOetReading() {
        String score = null;
        for (CandidateExam exam : candidateExams) {
            if (exam.getExam().equals(Exam.OETRead)) {
                score = exam.getScore();
                break;
            }
        }
        return score;
    }
    public String getOetListening() {
        String score = null;
        for (CandidateExam exam : candidateExams) {
            if (exam.getExam().equals(Exam.OETList)) {
                score = exam.getScore();
                break;
            }
        }
        return score;
    }
    public String getOetLanguage() {
        String score = null;
        for (CandidateExam exam : candidateExams) {
            if (exam.getExam().equals(Exam.OETLang)) {
                score = exam.getScore();
                break;
            }
        }
        return score;
    }

    public String getOccupationSummary() {
        StringBuilder s = new StringBuilder();
        for (CandidateOccupation occupation : candidateOccupations) {
            if (s.length() > 0) {
                s.append(", ");
            }
            s.append(occupation.getOccupation().getName());
        }
        return s.toString();
    }

    public String getEnglishExamsSummary() {
        StringBuilder s = new StringBuilder();
        for (CandidateExam exam : candidateExams) {
            if (s.length() > 0) {
                s.append(", ");
            }
            String examType = exam.getExam().equals(Exam.Other) ? exam.getOtherExam() : exam.getExam().toString();
            String examString;
            if (examType != null) {
                if (exam.getScore() != null) {
                    examString = examType + ": " + exam.getScore();
                } else {
                    examString = examType;
                }
                s.append(examString);
            }
        }
        return s.toString();
    }

    public String getEducationsSummary() {
        StringBuilder s = new StringBuilder();
        for (CandidateEducation edu : candidateEducations) {
            if (s.length() > 0) {
                s.append(", ");
            }
            String eduString;
            if (edu.getEducationType() != null) {
                if (edu.getEducationMajor() != null) {
                    eduString = edu.getEducationType().toString() + " in " + edu.getEducationMajor().getName();
                } else {
                    if (edu.getCourseName() != null) {
                        eduString = edu.getEducationType().toString() + " in " + edu.getCourseName();
                    } else {
                        eduString = edu.getEducationType().toString();
                    }
                }
            } else {
                eduString = edu.getEducationMajor().getName() != null ? edu.getEducationMajor().getName() : edu.getCourseName();
            }
            s.append(eduString);
        }
        return s.toString();
    }

    public String getCertificationsSummary() {
        StringBuilder s = new StringBuilder();
        for (CandidateCertification cert : candidateCertifications) {
            if (s.length() > 0) {
                s.append(", ");
            }
            String certString = cert.getName() + ": " + cert.getInstitution() + " " + cert.getDateCompleted().getYear();
            s.append(certString);
        }
        return s.toString();
    }

    // Intake audit data (who/when) is now saved in the database.
    // However, for transfer to SF we just want a summary of the top level intake complete.
    // This is consistent with what was previously saved to SF when we used candidate notes only to track the intake completion.
    @Transient
    public String getTopLevelIntakeCompleted() {
        String intaked = "-";
        if (fullIntakeCompletedDate != null) {
            intaked = "Full";
        } else if (miniIntakeCompletedDate != null) {
            intaked = "Mini";
        }
        return intaked;
    }

    @Transient
    public String getTopLevelIntakeCompletedDate() {
        String intakeDate = "";
        if (fullIntakeCompletedDate != null) {
            intakeDate = String.valueOf(fullIntakeCompletedDate).substring(0, 9);
        } else if (miniIntakeCompletedDate != null) {
            intakeDate = String.valueOf(miniIntakeCompletedDate).substring(0, 9);
        }
        return intakeDate;
    }

    public String getTcLink() {
        return "https://tctalent.org/admin-portal/candidate/" + candidateNumber;
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

    public @Nullable Gender getGender() {
        return gender;
    }

    public void setGender(@Nullable Gender gender) {
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

    public boolean isAllNotifications() {
        return allNotifications;
    }

    public void setAllNotifications(boolean allNotifications) {
        this.allNotifications = allNotifications;
    }

    public String getCity() {return city;}

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {return state;}

    public void setState(String state) {this.state = state;}

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

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isChangePassword() {
        return changePassword;
    }

    public void setChangePassword(boolean changePassword) {
        this.changePassword = changePassword;
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

    public void setCandidateOccupations(List<CandidateOccupation> entities) {
        this.candidateOccupations = entities;
        if (entities != null) {
            entities.forEach(entity -> entity.setCandidate(this));
        }
    }

    public List<CandidateNote> getCandidateNotes() {
        return candidateNotes;
    }

    public void setCandidateNotes(List<CandidateNote> entities) {
        this.candidateNotes = entities;
        if (entities != null) {
            for (CandidateNote entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public List<CandidateOpportunity> getCandidateOpportunities() {
        return candidateOpportunities;
    }

    public void setCandidateOpportunities(List<CandidateOpportunity> entities) {
        this.candidateOpportunities = entities;
        if (entities != null) {
            for (CandidateOpportunity entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public Set<CandidateProperty> getCandidateProperties() {
        return candidateProperties;
    }

    public void setCandidateProperties(Set<CandidateProperty> properties) {
        this.candidateProperties = properties;
    }

    public List<CandidateEducation> getCandidateEducations() {
        return candidateEducations;
    }

    public void setCandidateEducations(List<CandidateEducation> entities) {
        this.candidateEducations = entities;
        if (entities != null) {
            for (CandidateEducation entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public List<CandidateLanguage> getCandidateLanguages() {
        return candidateLanguages;
    }

    public void setCandidateLanguages(List<CandidateLanguage> entities) {
        this.candidateLanguages = entities;
        if (entities != null) {
            for (CandidateLanguage entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public List<CandidateJobExperience> getCandidateJobExperiences() {
        return candidateJobExperiences;
    }

    public void setCandidateJobExperiences(List<CandidateJobExperience> entities) {
        this.candidateJobExperiences = entities;
        if (entities != null) {
            for (CandidateJobExperience entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public List<CandidateCertification> getCandidateCertifications() {
        return candidateCertifications;
    }

    public void setCandidateCertifications(List<CandidateCertification> entities) {
        this.candidateCertifications = entities;
        if (entities != null) {
            for (CandidateCertification entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public Set<CandidateReviewStatusItem> getCandidateReviewStatusItems() {
        return candidateReviewStatusItems;
    }

    public void setCandidateReviewStatusItems(Set<CandidateReviewStatusItem> entities) {
        this.candidateReviewStatusItems = entities;
        if (entities != null) {
            for (CandidateReviewStatusItem entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public List<CandidateSkill> getCandidateSkills() {
        return candidateSkills;
    }

    public void setCandidateSkills(List<CandidateSkill> entities) {
        this.candidateSkills = entities;
        if (entities != null) {
            for (CandidateSkill entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public List<CandidateAttachment> getCandidateAttachments() { return candidateAttachments; }

    public void setCandidateAttachments(List<CandidateAttachment> entities) {
        this.candidateAttachments = entities;
        if (entities != null) {
            for (CandidateAttachment entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public List<CandidateCitizenship> getCandidateCitizenships() {
        return candidateCitizenships;
    }

    public void setCandidateCitizenships(List<CandidateCitizenship> entities) {
        this.candidateCitizenships = entities;
        if (entities != null) {
            for (CandidateCitizenship entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public List<CandidateExam> getCandidateExams() { return candidateExams; }

    public void setCandidateExams(List<CandidateExam> entities) {
        this.candidateExams = entities;
        if (entities != null) {
            for (CandidateExam entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public List<CandidateVisaCheck> getCandidateVisaChecks() {
        candidateVisaChecks.sort(null);
        return candidateVisaChecks;
    }

    public void setCandidateVisaChecks(List<CandidateVisaCheck> entities) {
        this.candidateVisaChecks = entities;
        if (entities != null) {
            for (CandidateVisaCheck entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    @Nullable
    public String getFolderlink() {
        return folderlink;
    }

    public void setFolderlink(@Nullable String folderlink) {
        //Treat empty string links like nulls.
        if (folderlink != null && folderlink.trim().length() == 0) {
            folderlink = null;
        }
        this.folderlink = folderlink;
    }

    @Nullable
    public String getFolderlinkAddress() {
        return folderlinkAddress;
    }

    public void setFolderlinkAddress(@Nullable String folderlinkAddress) {
        this.folderlinkAddress = folderlinkAddress;
    }

    @Nullable
    public String getFolderlinkCharacter() {
        return folderlinkCharacter;
    }

    public void setFolderlinkCharacter(@Nullable String folderlinkCharacter) {
        this.folderlinkCharacter = folderlinkCharacter;
    }

    @Nullable
    public String getFolderlinkEmployer() {
        return folderlinkEmployer;
    }

    public void setFolderlinkEmployer(@Nullable String folderlinkEmployer) {
        this.folderlinkEmployer = folderlinkEmployer;
    }

    @Nullable
    public String getFolderlinkEngagement() {
        return folderlinkEngagement;
    }

    public void setFolderlinkEngagement(@Nullable String folderlinkEngagement) {
        this.folderlinkEngagement = folderlinkEngagement;
    }

    @Nullable
    public String getFolderlinkExperience() {
        return folderlinkExperience;
    }

    public void setFolderlinkExperience(@Nullable String folderlinkExperience) {
        this.folderlinkExperience = folderlinkExperience;
    }

    @Nullable
    public String getFolderlinkFamily() {
        return folderlinkFamily;
    }

    public void setFolderlinkFamily(@Nullable String folderlinkFamily) {
        this.folderlinkFamily = folderlinkFamily;
    }

    @Nullable
    public String getFolderlinkIdentity() {
        return folderlinkIdentity;
    }

    public void setFolderlinkIdentity(@Nullable String folderlinkIdentity) {
        this.folderlinkIdentity = folderlinkIdentity;
    }

    @Nullable
    public String getFolderlinkImmigration() {
        return folderlinkImmigration;
    }

    public void setFolderlinkImmigration(@Nullable String folderlinkImmigration) {
        this.folderlinkImmigration = folderlinkImmigration;
    }

    @Nullable
    public String getFolderlinkLanguage() {
        return folderlinkLanguage;
    }

    public void setFolderlinkLanguage(@Nullable String folderlinkLanguage) {
        this.folderlinkLanguage = folderlinkLanguage;
    }

    @Nullable
    public String getFolderlinkMedical() {
        return folderlinkMedical;
    }

    public void setFolderlinkMedical(@Nullable String folderlinkMedical) {
        this.folderlinkMedical = folderlinkMedical;
    }

    @Nullable
    public String getFolderlinkQualification() {
        return folderlinkQualification;
    }

    public void setFolderlinkQualification(@Nullable String folderlinkQualification) {
        this.folderlinkQualification = folderlinkQualification;
    }

    @Nullable
    public String getFolderlinkRegistration() {
        return folderlinkRegistration;
    }

    public void setFolderlinkRegistration(@Nullable String folderlinkRegistration) {
        this.folderlinkRegistration = folderlinkRegistration;
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
    public LocalDate getAvailDate() {
        return availDate;
    }

    public void setAvailDate(@Nullable LocalDate availDate) {
        this.availDate = availDate;
    }

    @Nullable
    public YesNo getAvailImmediate() { return availImmediate; }

    public void setAvailImmediate(@Nullable YesNo availImmediate) { this.availImmediate = availImmediate; }

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
    public YesNoUnsure getUnhcrRegistered() {
        UnhcrStatus status = getUnhcrStatus();
        YesNoUnsure registered;
        if (status == null) {
            registered = YesNoUnsure.NoResponse;
        } else {
            switch (status) {
                case NotRegistered:
                case NA:
                    registered = YesNoUnsure.No;
                    break;
                case RegisteredAsylum:
                case RegisteredStateless:
                case RegisteredStatusUnknown:
                case MandateRefugee:
                    registered = YesNoUnsure.Yes;
                    break;
                case Unsure:
                    registered = YesNoUnsure.Unsure;
                    break;
                case NoResponse:
                    registered = YesNoUnsure.NoResponse;
                    break;
                default:
                    registered = null;
                    LogBuilder.builder(log)
                        .message("Unhandled UNHCRStatus: " + status)
                        .action("GetUnhcrRegistered")
                        .logError();
            }
        }
        return registered;
    }

    public void setUnhcrRegistered(@Nullable YesNoUnsure unhcrRegistered) {
        this.unhcrRegistered = unhcrRegistered;

        UnhcrStatus status = getUnhcrStatus();
        UnhcrStatus newStatus = null;

        if (unhcrRegistered == null) {
            if (status == null) {
                newStatus = UnhcrStatus.NoResponse;
            }
        } else {
            //Only update UnhcrStatus if the unhcrRegistered value is providing additional info,
            //ie if the current status is null, noResponse or unsure
            if (status == null ||
                Arrays.asList(UnhcrStatus.NoResponse, UnhcrStatus.Unsure).contains(status) ) {
                switch (unhcrRegistered) {
                    case Yes:
                        newStatus = UnhcrStatus.RegisteredStatusUnknown;
                        break;
                    case No:
                        newStatus = UnhcrStatus.NotRegistered;
                        break;
                    case NoResponse:
                        newStatus = UnhcrStatus.NoResponse;
                        break;
                    case Unsure:
                        newStatus = UnhcrStatus.Unsure;
                        break;
                }
            }
        }
        if (newStatus != null) {
            setUnhcrStatus(newStatus);
        }
    }

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

    public void setCandidateDependants(List<CandidateDependant> entities) {
        this.candidateDependants = entities;
        if (entities != null) {
            for (CandidateDependant entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public List<CandidateDestination> getCandidateDestinations() {
        if (candidateDestinations != null) {
            candidateDestinations.sort(null);
        }
        return candidateDestinations;
    }

    public void setCandidateDestinations(List<CandidateDestination> entities) {
        this.candidateDestinations = entities;
        if (entities != null) {
            for (CandidateDestination entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

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
    public YesNoUnsure getArrestImprison() { return arrestImprison; }

    public void setArrestImprison(@Nullable YesNoUnsure arrestImprison) { this.arrestImprison = arrestImprison; }

    @Nullable
    public String getArrestImprisonNotes() { return arrestImprisonNotes; }

    public void setArrestImprisonNotes(@Nullable String arrestImprisonNotes) { this.arrestImprisonNotes = arrestImprisonNotes; }

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

    public @Nullable Number getRank() {
        return rank;
    }

    public void setRank(@Nullable Number rank) {
        this.rank = rank;
    }

    @Nullable
    public PartnerImpl getRegisteredBy() {
        return registeredBy;
    }

    public void setRegisteredBy(@Nullable PartnerImpl registeredBy) {
        this.registeredBy = registeredBy;
    }

    @Nullable
    public String getRegoIp() {
        return regoIp;
    }

    public void setRegoIp(@Nullable String regoIp) {
        this.regoIp = regoIp;
    }

    @Nullable
    public String getRegoPartnerParam() {
        return regoPartnerParam;
    }

    public void setRegoPartnerParam(@Nullable String regoPartnerParam) {
        this.regoPartnerParam = regoPartnerParam;
    }

    @Nullable
    public String getRegoReferrerParam() {
        return regoReferrerParam;
    }

    public void setRegoReferrerParam(@Nullable String regoReferrerParam) {
        this.regoReferrerParam = regoReferrerParam;
    }

    @Nullable
    public String getRegoUtmCampaign() {
        return regoUtmCampaign;
    }

    public void setRegoUtmCampaign(@Nullable String regoUtmCampaign) {
        this.regoUtmCampaign = regoUtmCampaign;
    }

    @Nullable
    public String getRegoUtmContent() {
        return regoUtmContent;
    }

    public void setRegoUtmContent(@Nullable String regoUtmContent) {
        this.regoUtmContent = regoUtmContent;
    }

    @Nullable
    public String getRegoUtmMedium() {
        return regoUtmMedium;
    }

    public void setRegoUtmMedium(@Nullable String regoUtmMedium) {
        this.regoUtmMedium = regoUtmMedium;
    }

    @Nullable
    public String getRegoUtmSource() {
        return regoUtmSource;
    }

    public void setRegoUtmSource(@Nullable String regoUtmSource) {
        this.regoUtmSource = regoUtmSource;
    }

    @Nullable
    public String getRegoUtmTerm() {
        return regoUtmTerm;
    }

    public void setRegoUtmTerm(@Nullable String regoUtmTerm) {
        this.regoUtmTerm = regoUtmTerm;
    }

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
    public YesNo getMonitoringEvaluationConsent() {return monitoringEvaluationConsent;}

    public void setMonitoringEvaluationConsent(@Nullable YesNo monitoringEvaluationConsent) {
        this.monitoringEvaluationConsent = monitoringEvaluationConsent;
    }

    @Nullable
    public String getPartnerRef() {
        return partnerRef;
    }

    public void setPartnerRef(@Nullable String partnerRef) {
        this.partnerRef = partnerRef;
    }

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

    /**
     * Provides a list of country IDs for the candidate's partner's citizenships,
     * instead of the comma separated string we store on the DB.
     * Not currently used but left in case of future utility.
     * @return list of country IDs or null if nothing stored
     */
    @Nullable
    public List<Long> getPartnerCitizenship() {
        return partnerCitizenship != null ?
            Stream.of(partnerCitizenship.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList()) : null;
    }

    /**
     * Converts an array of country IDs to a comma-separated string for DB storage.
     * @param partnerCitizenshipCountryIds array of country IDs indicating countries of which a
     *                                     given candidate's partner is a citizen.
     */
    public void setPartnerCitizenship(List<Long> partnerCitizenshipCountryIds) {
        this.partnerCitizenship = !CollectionUtils.isEmpty(partnerCitizenshipCountryIds) ?
            partnerCitizenshipCountryIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) : null;
    }

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
    public String getEnglishAssessment() {
        return englishAssessment;
    }

    public void setEnglishAssessment(@Nullable String englishAssessment) {
        this.englishAssessment = englishAssessment;
    }

    @Nullable
    public String getEnglishAssessmentScoreIelts() {
        return englishAssessmentScoreIelts;
    }

    public void setEnglishAssessmentScoreIelts(@Nullable String englishAssessmentScoreIelts) {
        this.englishAssessmentScoreIelts = englishAssessmentScoreIelts;
    }

    @Nullable
    public Long getEnglishAssessmentScoreDet() {
        return englishAssessmentScoreDet;
    }

    public void setEnglishAssessmentScoreDet(@Nullable Long englishAssessmentScoreDet) {
        this.englishAssessmentScoreDet = englishAssessmentScoreDet;
    }

    @Nullable
    public String getFrenchAssessment() { return frenchAssessment; }

    public void setFrenchAssessment(@Nullable String frenchAssessment) {
        this.frenchAssessment = frenchAssessment;
    }

    @Nullable
    public Long getFrenchAssessmentScoreNclc() {
        return frenchAssessmentScoreNclc;
    }

    public void setFrenchAssessmentScoreNclc(@Nullable Long frenchAssessmentScoreNclc) {
        this.frenchAssessmentScoreNclc = frenchAssessmentScoreNclc;
    }

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

    @Nullable
    public String getExternalId() {return externalId;}

    public void setExternalId(@Nullable String externalId) {this.externalId = externalId;}

    @Nullable
    public String getExternalIdSource() {return externalIdSource;}

    public void setExternalIdSource(@Nullable String externalIdSource) {this.externalIdSource = externalIdSource;}

    @Nullable
    public YesNo getCovidVaccinated() {return covidVaccinated;}

    public void setCovidVaccinated(@Nullable YesNo covidVaccinated) {this.covidVaccinated = covidVaccinated;}

    @Nullable
    public VaccinationStatus getCovidVaccinatedStatus() {return covidVaccinatedStatus;}

    public void setCovidVaccinatedStatus(@Nullable VaccinationStatus covidVaccinatedStatus) {this.covidVaccinatedStatus = covidVaccinatedStatus;}

    @Nullable
    public LocalDate getCovidVaccinatedDate() {return covidVaccinatedDate;}

    public void setCovidVaccinatedDate(@Nullable LocalDate covidVaccinatedDate) {this.covidVaccinatedDate = covidVaccinatedDate;}

    @Nullable
    public String getCovidVaccineName() {return covidVaccineName;}

    public void setCovidVaccineName(@Nullable String covidVaccineName) {this.covidVaccineName = covidVaccineName;}

    @Nullable
    public String getCovidVaccineNotes() {return covidVaccineNotes;}

    public void setCovidVaccineNotes(@Nullable String covidVaccineNotes) {this.covidVaccineNotes = covidVaccineNotes;}

    @Nullable
    public String getMediaWillingness() {return mediaWillingness;}

    public void setMediaWillingness(@Nullable String mediaWillingness) {
        this.mediaWillingness = mediaWillingness;
    }

    public boolean getPotentialDuplicate() {return potentialDuplicate;}

    public void setPotentialDuplicate(boolean potentialDuplicate) {
        this.potentialDuplicate = potentialDuplicate;
    }

    public Boolean getContactConsentRegistration() {
        return contactConsentRegistration;
    }

    public void setContactConsentRegistration(Boolean emailConsentRegistration) {
        this.contactConsentRegistration = emailConsentRegistration;
    }

    public boolean getContactConsentPartners() {
        return contactConsentPartners;
    }

    public void setContactConsentPartners(boolean emailConsentPartners) {
        this.contactConsentPartners = emailConsentPartners;
    }

    @Nullable
    public User getMiniIntakeCompletedBy() {
        return miniIntakeCompletedBy;
    }

    public void setMiniIntakeCompletedBy(@Nullable User miniIntakeCompletedBy) {
        this.miniIntakeCompletedBy = miniIntakeCompletedBy;
    }

    @Nullable
    public OffsetDateTime getMiniIntakeCompletedDate() {
        return miniIntakeCompletedDate;
    }

    public void setMiniIntakeCompletedDate(@Nullable OffsetDateTime miniIntakeCompletedDate) {
        this.miniIntakeCompletedDate = miniIntakeCompletedDate;
    }

    @Nullable
    public User getFullIntakeCompletedBy() {
        return fullIntakeCompletedBy;
    }

    public void setFullIntakeCompletedBy(@Nullable User fullIntakeCompletedBy) {
        this.fullIntakeCompletedBy = fullIntakeCompletedBy;
    }

    @Nullable
    public OffsetDateTime getFullIntakeCompletedDate() {
        return fullIntakeCompletedDate;
    }

    public void setFullIntakeCompletedDate(@Nullable OffsetDateTime fullIntakeCompletedDate) {
        this.fullIntakeCompletedDate = fullIntakeCompletedDate;
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
        return SalesforceHelper.extractIdFromSfUrl(sflink);
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

    public void setCandidateSavedLists(Set<CandidateSavedList> entities) {
        this.candidateSavedLists = entities;
        if (entities != null) {
            for (CandidateSavedList entity : entities) {
                entity.setCandidate(this);
            }
        }
    }

    public List<TaskAssignmentImpl> getTaskAssignments() {
        return taskAssignments;
    }

    public void setTaskAssignments(List<TaskAssignmentImpl> taskAssignments) {
        this.taskAssignments = taskAssignments;
    }

    @Transient
    public Set<SavedList> getSavedLists() {
        Set<SavedList> savedLists = new HashSet<>();
        for (CandidateSavedList candidateSavedList : candidateSavedLists) {
            savedLists.add(candidateSavedList.getSavedList());
        }
        return savedLists;
    }

    /**
     * True if this candidate has been presented with our terms but has not yet accepted them.
     * <p/>
     * This is equivalent to the candidate being in the PendingTermsAcceptance list.
     * @return True if the candidate has not accepted our terms.
     */
    public boolean isPendingTerms() {
        return isTagged(SystemAdminConfiguration.PENDING_TERMS_ACCEPTANCE_LIST_ID);
    }

    /**
     * True if this is a test candidate.
     * <p/>
     * This is equivalent to the candidate being in the TestCandidates list.
     * @return True if the candidate is a test candidate.
     */
    public boolean isTestCandidate() {
        return isTagged(SystemAdminConfiguration.TEST_CANDIDATE_LIST_ID);
    }

    /**
     * True if this candidate is in (ie tagged by) the given list
     * @param savedListId Saved list id
     * @return True if in list (ie tagged by that list)
     */
    public boolean isTagged(long savedListId) {
        Optional<CandidateSavedList> optional = candidateSavedLists.stream()
            .filter(sl -> sl.getSavedList().getId() == savedListId)
            .findAny();
        return optional.isPresent();
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

    // RELOCATED FIELDS - keep track of a relocated candidate's location

    @Nullable
    public String getRelocatedAddress() {
        return relocatedAddress;
    }

    public void setRelocatedAddress(@Nullable String relocatedAddress) {
        this.relocatedAddress = relocatedAddress;
    }

    @Nullable
    public String getRelocatedCity() {
        return relocatedCity;
    }

    public void setRelocatedCity(@Nullable String relocatedCity) {
        this.relocatedCity = relocatedCity;
    }

    @Nullable
    public String getRelocatedState() {
        return relocatedState;
    }

    public void setRelocatedState(@Nullable String relocatedState) {
        this.relocatedState = relocatedState;
    }

    @Nullable
    public Country getRelocatedCountry() {
        return relocatedCountry;
    }

    public void setRelocatedCountry(Country relocatedCountry) {
        this.relocatedCountry = relocatedCountry;
    }

    public String getText() {
        return text;
    }

    public void updateText() {
        String combinedJobText = getCandidateJobExperiences().stream()
            .map(CandidateJobExperience::getDescription)
            .collect(Collectors.joining(" || "));
        String combinedCvText = getCandidateAttachments().stream()
            .filter(CandidateAttachment::isCv)
            .map(CandidateAttachment::getTextExtract)
            .collect(Collectors.joining(" || "));
        this.text = combinedJobText + " || " + combinedCvText;
    }
}
