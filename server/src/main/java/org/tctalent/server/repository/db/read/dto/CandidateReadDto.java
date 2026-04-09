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
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.DocumentStatus;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.IntRecruitReason;
import org.tctalent.server.model.db.MaritalStatus;
import org.tctalent.server.model.db.ResidenceStatus;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.YesNo;
import org.tctalent.server.model.db.YesNoUnsure;
import org.tctalent.server.repository.db.read.annotation.JsonOneToMany;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlColumn;
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlIgnore;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

/**
 * This is the ROOT DTO for all candidate data.
 *
 * @author John Cameron
 */
@Getter
@Setter
@Builder //Useful for constructing unit tests
@SqlTable(name="candidate", alias = "c")
@SqlDefaults(mapUnannotatedColumns = true)
public class CandidateReadDto {

    private OffsetDateTime acceptedPrivacyPolicyDate;
    private String acceptedPrivacyPolicyId;

    @JsonOneToOne(joinColumn = "accepted_privacy_policy_partner_id")
    private PartnerReadDto acceptedPrivacyPolicyPartner;
    private String additionalInfo;
    private String address1;
    private boolean allNotifications;
    private String candidateMessage;

    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateAttachmentReadDto> candidateAttachments;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateCertificationReadDto> candidateCertifications;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateCitizenshipReadDto> candidateCitizenships;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateDependantReadDto> candidateDependants;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateDestinationReadDto> candidateDestinations;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateEducationReadDto> candidateEducations;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateExamReadDto> candidateExams;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateJobExperienceReadDto> candidateJobExperiences;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateLanguageReadDto> candidateLanguages;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateNoteReadDto> candidateNotes;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateOccupationReadDto> candidateOccupations;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateOpportunityReadDto> candidateOpportunities;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidatePropertyReadDto> candidateProperties;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateReviewStatusItemReadDto> candidateReviewStatusItems;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateSkillReadDto> candidateSkills;
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateVisaCheckReadDto> candidateVisaChecks;
    private String candidateNumber;
    private String city;
    private YesNo conflict;

    @SqlIgnore //Loaded later if there is a list context
    private String contextNote;

    @JsonOneToOne(joinColumn = "country_id")
    private CountryReadDto country;
    private OffsetDateTime createdDate;
    private LocalDate dob;
    private DocumentStatus drivingLicense;
    private LocalDate drivingLicenseExp;
    private Long englishAssessmentScoreDet;
    private String englishAssessmentScoreIelts;
    private String externalId;
    private String externalIdSource;
    private YesNo familyMove;
    private String folderlink;
    private Long frenchAssessmentScoreNclc;
    private OffsetDateTime fullIntakeCompletedDate;
    @JsonOneToOne(joinColumn = "full_intake_completed_by")
    private UserReadDto fullIntakeCompletedBy;
    private Gender gender;
    private YesNo healthIssues;
    private String hostChallenges;
    private YesNo hostEntryLegally;
    private Long hostEntryYear;
    private Long id;
    private BigDecimal ieltsScore;
    private String intRecruitOther;

    @SqlColumn(transform = "to_jsonb(string_to_array(%s, ','))") //Convert csv string to jsonb array
    private List<IntRecruitReason> intRecruitReasons;
    private YesNoUnsure intRecruitRural;
    private String linkedInLink;
    @SqlIgnore //Computed field based on list context
    private CandidateAttachmentReadDto listShareableCv;
    @SqlIgnore //Computed field based on list context
    private CandidateAttachmentReadDto listShareableDoc;
    private MaritalStatus maritalStatus;
    @JsonOneToOne(joinColumn = "max_education_level_id")
    private EducationLevelReadDto maxEducationLevel;
    private String mediaWillingness;
    private OffsetDateTime miniIntakeCompletedDate;
    @JsonOneToOne(joinColumn = "mini_intake_completed_by")
    private UserReadDto miniIntakeCompletedBy;
    private boolean muted;
    @JsonOneToOne(joinColumn = "nationality_id")
    private CountryReadDto nationality;

    @SqlIgnore //Computed field
    private int numberDependants;
    private String partnerRef;
    @SqlIgnore //Computed field
    private boolean pendingTerms;
    private String phone;
    private boolean potentialDuplicate;
    private String publicId;
    @SqlIgnore
    private Number rank;
    private String regoPartnerParam;
    private String regoReferrerParam;
    private String regoUtmCampaign;
    private String regoUtmContent;
    private String regoUtmMedium;
    private String regoUtmSource;
    private String regoUtmTerm;
    @JsonOneToOne(joinColumn = "registered_by")
    private PartnerReadDto registeredBy;
    private String relocatedAddress;
    private String relocatedCity;
    @JsonOneToOne(joinColumn = "relocated_country_id")
    private CountryReadDto relocatedCountry;
    private String relocatedState;
    private ResidenceStatus residenceStatus;
    private YesNoUnsure returnedHome;
    private YesNoUnsure returnHomeFuture;
    private YesNoUnsure returnHomeSafe;
    @SqlIgnore
    private boolean selected;
    private String sflink;

    @JsonOneToOne(joinColumn = "shareable_cv_attachment_id")
    private CandidateAttachmentReadDto shareableCv;
    @JsonOneToOne(joinColumn = "shareable_doc_attachment_id")
    private CandidateAttachmentReadDto shareableDoc;
    private String shareableNotes;
    private String state;
    private CandidateStatus status;
    @JsonOneToOne(joinColumn = "survey_type_id")
    private SurveyTypeReadDto surveyType;
    private String surveyComment;

    @JsonOneToMany(joinColumn = "candidate_id")
    private List<TaskAssignmentReadDto> taskAssignments;
    private YesNo unhcrConsent;
    private String unhcrNumber;
    private YesNoUnsure unhcrRegistered;
    private UnhcrStatus unhcrStatus;
    private String unrwaNumber;
    private YesNoUnsure unrwaRegistered;
    private OffsetDateTime updatedDate;

    @JsonOneToOne(joinColumn = "user_id")
    private UserReadDto user;
    private String videolink;
    private String whatsapp;

}
