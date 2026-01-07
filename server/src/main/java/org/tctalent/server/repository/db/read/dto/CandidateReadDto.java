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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.repository.db.read.annotation.JsonOneToMany;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
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
@SqlTable(name="candidate", alias = "c")
@SqlDefaults(mapUnannotatedColumns = true)
public class CandidateReadDto {

    private OffsetDateTime acceptedPrivacyPolicyDate;
    private String acceptedPrivacyPolicyId;
    
    @JsonOneToOne(joinColumn = "accepted_privacy_policy_partner_id")
    private PartnerReadDto acceptedPrivacyPolicyPartner;
    private String additionalInfo;
    private String address1;
    private String allNotifications;
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
    
    @SqlIgnore //TODO JC Doesnt seem to handle these
//    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateVisaCheckReadDto> candidateVisaChecks;
    private String candidateNumber;
    private String city;
    private String conflict;
    @SqlIgnore //todo Computed field
    private String contextNote;
    @JsonOneToOne(joinColumn = "country_id")
    private CountryReadDto country;
    private OffsetDateTime createdDate;
    private LocalDate dob;
    private String drivingLicense;
    private LocalDate drivingLicenseExp;
    private String englishAssessmentScoreDet;
    private String englishAssessmentScoreIelts;
    private String externalId;
    private String externalIdSource;
    private String familyMove;
    private String folderlink;
    private String frenchAssessmentScoreNclc;
    private OffsetDateTime fullIntakeCompletedDate;
    @JsonOneToOne(joinColumn = "full_intake_completed_by")
    private UserReadDto fullIntakeCompletedBy;
    private String gender;
    private String healthIssues;
    private String hostChallenges;
    private String hostEntryLegally;
    private String hostEntryYear;
    private Long id;
    private String ieltsScore;
    private String intRecruitOther;
    private String intRecruitReasons;
    private String intRecruitRural;
    private String linkedInLink;
    @SqlIgnore //todo Computed field
    private String listShareableCv;
    @SqlIgnore //todo Computed field
    private String listShareableDoc;
    private String maritalStatus;
    @JsonOneToOne(joinColumn = "max_education_level_id")
    private EducationLevelReadDto maxEducationLevel;
    private String mediaWillingness;
    private OffsetDateTime miniIntakeCompletedDate;
    @JsonOneToOne(joinColumn = "mini_intake_completed_by")
    private UserReadDto miniIntakeCompletedBy;
    private String muted;
    @JsonOneToOne(joinColumn = "nationality_id")
    private CountryReadDto nationality;
    
    @SqlIgnore //todo Computed field
    private String numberDependants;
    private String partnerRef;
    @SqlIgnore //todo Computed field
    private String pendingTerms;
    private String phone;
    private String potentialDuplicate;
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
    private String residenceStatus;
    private String returnedHome;
    private String returnHomeFuture;
    private String returnHomeSafe;
    @SqlIgnore
    private boolean selected;
    private String sflink;
    @SqlIgnore //todo Computed field
    private String shareableCv;
    @SqlIgnore //todo Computed field
    private String shareableDoc;
    private String shareableNotes;
    private String state;
    private String status;
    @JsonOneToOne(joinColumn = "survey_type_id")
    private SurveyTypeReadDto surveyType;
    private String surveyComment;
    
    @SqlIgnore //TODO JC Not done yet
//    @JsonOneToMany(joinColumn = "candidate_id")
    private List<TaskAssignmentReadDto> taskAssignments;
    private String unhcrConsent;
    private String unhcrNumber;
    private String unhcrRegistered;
    private String unhcrStatus;
    private String unrwaNumber;
    private String unrwaRegistered;
    private OffsetDateTime updatedDate;
    
    @JsonOneToOne(joinColumn = "user_id")
    private UserReadDto user;
    private String videolink;
    private String whatsapp;
    
}
