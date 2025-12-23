/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.repository.db.read.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.CandidateDestination;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.CandidateExam;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.CandidateNote;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateProperty;
import org.tctalent.server.model.db.CandidateReviewStatusItem;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.CandidateSkill;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.DocumentStatus;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.IeltsStatus;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.LeftHomeReason;
import org.tctalent.server.model.db.MaritalStatus;
import org.tctalent.server.model.db.NotRegisteredStatus;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.ResidenceStatus;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.model.db.TaskAssignmentImpl;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.VaccinationStatus;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.YesNoUnsure;
import org.tctalent.server.repository.db.read.annotation.JsonOneToMany;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlColumn;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

/**
 * This is the ROOT DTO for all candidate data.
 *
 * @author John Cameron
 */
@Getter
@Setter
@SqlTable(name="candidate", alias = "c")
public class CandidateReadDto {
    @SqlColumn
    private Long id;
    
    @SqlColumn
    private String candidateNumber;
    @SqlColumn
    private String publicId;
    private CandidateStatus status;
    private boolean muted;
    private boolean pendingTerms;
    private boolean allNotifications;
    private Gender gender;
    private LocalDate dob;
    private String phone;
    private String whatsapp;
    private String address1;
    private String city;
    private String state;
    private String externalId;
    private String externalIdSource;
    private String partnerRef;
    private YesNoUnsure unhcrRegistered;
    private UnhcrStatus unhcrStatus;
    private String unhcrNumber;
    private YesNo unhcrConsent;
    private Long unhcrFile;
    private NotRegisteredStatus unhcrNotRegStatus;
    private String unhcrNotes;
    private YesNoUnsure unrwaRegistered;
    private String unrwaNumber;
    private Long unrwaFile;
    private NotRegisteredStatus unrwaNotRegStatus;
    private String unrwaNotes;
    private String homeLocation;
    private LocalDate asylumYear;
    private YesNo destLimit;
    private String destLimitNotes;
    private YesNoUnsure crimeConvict;
    private String crimeConvictNotes;
    private YesNoUnsure arrestImprison;
    private String arrestImprisonNotes;
    private YesNo conflict;
    private String conflictNotes;
    private ResidenceStatus residenceStatus;
    private String residenceStatusNotes;
    private YesNo workAbroad;
    private String workAbroadNotes;
    private YesNo hostEntryLegally;
    private String hostEntryLegallyNotes;
    private List<LeftHomeReason> leftHomeReasons;
    private String leftHomeNotes;
    private YesNoUnsure returnHomeFuture;
    private String returnHomeWhen;
    private YesNo resettleThird;
    private String resettleThirdStatus;
    private String hostChallenges;
    private MaritalStatus maritalStatus;
    private String maritalStatusNotes;
    private YesNoUnsure partnerRegistered;
    private Candidate partnerCandidate;
    private EducationLevel partnerEduLevel;
    private String partnerEduLevelNotes;
    private Occupation partnerOccupation;
    private String partnerOccupationNotes;
    private YesNo partnerEnglish;
    private LanguageLevel partnerEnglishLevel;
    private IeltsStatus partnerIelts;
    private String partnerIeltsScore;
    private Long partnerIeltsYr;
    private String partnerCitizenship;
    private YesNo militaryService;
    private YesNoUnsure militaryWanted;
    private String militaryNotes;
    private LocalDate militaryStart;
    private LocalDate militaryEnd;
    private YesNoUnsure visaReject;
    private String visaRejectNotes;
    private YesNo canDrive;
    private DocumentStatus drivingLicense;
    private LocalDate drivingLicenseExp;
    private Country drivingLicenseCountry;
    private String englishAssessment;
    private String englishAssessmentScoreIelts;
    private Long englishAssessmentScoreDet;
    private String frenchAssessment;
    private Long frenchAssessmentScoreNclc;
    private Country birthCountry;
    private BigDecimal ieltsScore;
    private Long numberDependants;
    private YesNo healthIssues;
    private String healthIssuesNotes;
    private YesNo covidVaccinated;
    private VaccinationStatus covidVaccinatedStatus;
    private LocalDate covidVaccinatedDate;
    private String covidVaccineName;
    private String covidVaccineNotes;
    private String mediaWillingness;
    private Boolean contactConsentRegistration;
    private boolean contactConsentPartners = true;
    private User miniIntakeCompletedBy;
    private OffsetDateTime miniIntakeCompletedDate;
    private User fullIntakeCompletedBy;
    private OffsetDateTime fullIntakeCompletedDate;
    private String relocatedAddress;
    private String relocatedCity;
    private String relocatedState;
    private Long contextNote;
    private Integer yearOfArrival;
    private String additionalInfo;
    private String candidateMessage;
    private String linkedInLink;
    private PartnerImpl registeredBy;
    private String regoIp;
    private String regoPartnerParam;
    private String regoReferrerParam;
    private String regoUtmCampaign;
    private String regoUtmContent;
    private String regoUtmMedium;
    private String regoUtmSource;
    private String regoUtmTerm;
    private String shareableNotes;
    private CandidateAttachment shareableCv;
    private CandidateAttachment shareableDoc;
    private SurveyType surveyType;
    private String surveyComment;
    private String acceptedPrivacyPolicyId;
    private OffsetDateTime acceptedPrivacyPolicyDate;
    private PartnerImpl acceptedPrivacyPolicyPartner;
    private Long contextSavedListId;

    private Map<String, CandidateProperty> candidateProperties;
    private List<TaskAssignmentImpl> taskAssignments;
    private boolean changePassword;
    private String text;
    private String textSearchId;
    private Set<CandidateSavedList> candidateSavedLists = new HashSet<>();
    private EducationLevel maxEducationLevel;
    private Country country;
    private Country nationality;
    
    @JsonOneToOne(joinLeftColumn = "user_id")
    private UserReadDto user;

    @JsonOneToOne(joinLeftColumn = "created_by")
    private UserReadDto createdBy;

    @JsonOneToOne(joinLeftColumn = "updated_by")
    private UserReadDto updatedBy;

    private OffsetDateTime createdDate;
    private OffsetDateTime updatedDate;

    private String folderlink;
    private String folderlinkAddress;
    private String folderlinkCharacter;
    private String folderlinkEmployer;
    private String folderlinkEngagement;
    private String folderlinkExperience;
    private String folderlinkFamily;
    private String folderlinkIdentity;
    private String folderlinkImmigration;
    private String folderlinkLanguage;
    private String folderlinkMedical;
    private String folderlinkQualification;
    private String folderlinkRegistration;
    private String sflink;
    private String videolink;
    private Country relocatedCountry;
    private boolean potentialDuplicate;

    private CandidateAttachment listShareableCv;
    private CandidateAttachment listShareableDoc;




    private List<CandidateOccupation> candidateOccupations;
    private List<CandidateNote> candidateNotes;
    private List<CandidateEducation> candidateEducations;
    private List<CandidateExam> candidateExams;
    private List<CandidateLanguage> candidateLanguages;
    private List<CandidateCertification> candidateCertifications;
    private Set<CandidateReviewStatusItem> candidateReviewStatusItems;
    private List<CandidateSkill> candidateSkills;
    private List<CandidateAttachment> candidateAttachments;
    private List<CandidateOpportunity> candidateOpportunities;
    
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateJobExperienceDto> candidateJobExperiences;
    private List<CandidateDestination> candidateDestinations;

    private Number rank;
    private boolean selected;
}
