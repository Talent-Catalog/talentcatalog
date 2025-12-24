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
    private PartnerReadDto acceptedPrivacyPolicyPartner;
    private String additionalInfo;
    private String address1;
    private String allNotifications;
    private String candidateMessage;
    private List<CandidateAttachmentReadDto> candidateAttachments;
    private List<CandidateCertificationReadDto> candidateCertifications;
    private List<CandidateCitizenshipReadDto> candidateCitizenships;
    private List<CandidateDependantReadDto> candidateDependants;
    private List<CandidateDestinationReadDto> candidateDestinations;
    private List<CandidateEducationReadDto> candidateEducations;
    private List<CandidateExamReadDto> candidateExams;
    
    @JsonOneToMany(joinColumn = "candidate_id")
    private List<CandidateJobExperienceReadDto> candidateJobExperiences;
    private List<CandidateLanguageReadDto> candidateLanguages;
    private List<CandidateNoteReadDto> candidateNotes;
    private List<CandidateOccupationReadDto> candidateOccupations;
    private List<CandidateOpportunityReadDto> candidateOpportunities;
    private List<CandidatePropertyReadDto> candidateProperties;
    private List<CandidateReviewStatusItemReadDto> candidateReviewStatusItems;
    private List<CandidateSkillReadDto> candidateSkills;
    private List<CandidateVisaCheckReadDto> candidateVisaChecks;
    private String candidateNumber;
    private String city;
    private String conflict;
    private String contextNote;
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
    private String maritalStatus;
    private EducationLevelReadDto maxEducationLevel;
    private String mediaWillingness;
    private OffsetDateTime miniIntakeCompletedDate;
    private UserReadDto miniIntakeCompletedBy;
    private String muted;
    private CountryReadDto nationality;
    private String numberDependants;
    private String partnerRef;
    private String pendingTerms;
    private String phone;
    private String potentialDuplicate;
    private String publicId;
    @SqlIgnore
    private String rank;
    private String regoPartnerParam;
    private String regoReferrerParam;
    private String regoUtmCampaign;
    private String regoUtmContent;
    private String regoUtmMedium;
    private String regoUtmSource;
    private String regoUtmTerm;
    private PartnerReadDto registeredBy;
    private String relocatedAddress;
    private String relocatedCity;
    private CountryReadDto relocatedCountry;
    private String relocatedState;
    private String residenceStatus;
    private String returnedHome;
    private String returnHomeFuture;
    private String returnHomeSafe;
    @SqlIgnore
    private String selected;
    private String sflink;
    private String shareableCv;
    private String shareableDoc;
    private String shareableNotes;
    private String state;
    private String status;
    private SurveyTypeReadDto surveyType;
    private String surveyComment;
    private String unhcrConsent;
    private String unhcrNumber;
    private String unhcrRegistered;
    private String unhcrStatus;
    private String unrwaNumber;
    private String unrwaRegistered;
    private OffsetDateTime updatedDate;
    
    @JsonOneToOne(joinLeftColumn = "user_id")
    private UserReadDto user;
    private String videolink;
    private String whatsapp;
    
}
