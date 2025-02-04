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

package org.tctalent.server.api.admin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.TaskDtoHelper;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.dto.DtoBuilder;
import org.tctalent.server.util.dto.DtoPropertyFilter;

/**
 * Utility for selecting the right DTO build based on the currently logged in
 * user.
 *
 * @author John Cameron
 */
public class CandidateBuilderSelector {
    private final CandidateOpportunityService candidateOpportunityService;
    private final CountryService countryService;
    private final OccupationService occupationService;
    private final UserService userService;

    private final Set<String> candidatePublicProperties =
        new HashSet<>(Arrays.asList(
            "id",
            "status",
            "candidateNumber",
            "publicId",
            "allNotifications",
            "gender",
            "dob",
            "yearOfArrival",
            "additionalInfo",
            "mediaWillingness",
            "candidateMessage",
            "folderlink",
            "sflink",
            "selected",
            "createdDate",
            "updatedDate",
            "contextNote",
            "user",
            "candidateOpportunities",
            "candidateReviewStatusItems",
            "country",
            "nationality",
            "regoPartnerParam",
            "regoReferrerParam",
            "regoUtmCampaign",
            "regoUtmContent",
            "regoUtmMedium",
            "regoUtmSource",
            "regoUtmTerm"
        ));

    private final Set<String> candidateSemiLimitedExtraProperties =
        new HashSet<>(Arrays.asList(
            "city",
            "state",
            "address1"
        ));

    private final Set<String> userPublicProperties =
        new HashSet<>(Arrays.asList(
            "id",
            "createdDate",
            "updatedDate",
            "partner"
        ));

    public CandidateBuilderSelector(
        CandidateOpportunityService candidateOpportunityService, CountryService countryService,
        OccupationService occupationService, UserService userService) {
        this.candidateOpportunityService = candidateOpportunityService;
        this.countryService = countryService;
        this.occupationService = occupationService;
        this.userService = userService;
    }

    @NonNull
    public DtoBuilder selectBuilder() {
        return selectBuilder(DtoType.FULL);
    }

    @NonNull
    public DtoBuilder selectBuilder(DtoType type) {
        if (DtoType.MINIMAL.equals(type)) {
            return minimalCandidateDto();
        }

        User user = userService.getLoggedInUser();
        Partner partner = user == null ? null : user.getPartner();


        //If partner is a job creator, look up the candidates for whom they can see all properties
        //(ie candidates who have got past the CV Review stage in any job opps managed by the partner)

        //Start by fetching all the opps associated with the partner
        List<CandidateOpportunity> partnerOpps = candidateOpportunityService.findJobCreatorPartnerOpps(partner);

        //Get all the candidates associated with those opps which got to the CV Review stage.
        //The partner should be able to see full details of these candidates - because they have
        //progressed this far with these candidates in the past.
        Set<Candidate> fullyVisibleCandidates = partnerOpps.stream()
            .filter(opp -> CandidateOpportunityStage.cvReview.compareTo(opp.getLastActiveStage()) <= 0)
            .map(CandidateOpportunity::getCandidate)
            .collect(Collectors.toSet());

        //Default to Role.limited if user is null.
        Role role = user == null ? Role.limited : user.getRole();
        DtoPropertyFilter candidatePropertyFilter = new PartnerAndRoleBasedDtoPropertyFilter(
            partner, role, fullyVisibleCandidates, candidatePublicProperties, candidateSemiLimitedExtraProperties);
        DtoPropertyFilter userPropertyFilter = new PartnerAndRoleBasedDtoPropertyFilter(
            partner, role, fullyVisibleCandidates, userPublicProperties, null);

        return candidateDto(candidatePropertyFilter, userPropertyFilter, type);
    }

    /**
     * Candidate Dto's fetch both properties of the candidate entity and the user entity (because
     * every candidate is a user).
     * @param candidatePropertyFilter Filter for candidate properties
     * @param userPropertyFilter Filter for candidate's user properties
     * @param type Type of DTO to build
     * @return DtoBuilder
     */
    private DtoBuilder candidateDto(
        DtoPropertyFilter candidatePropertyFilter, DtoPropertyFilter userPropertyFilter, DtoType type) {
        final DtoBuilder builder = new DtoBuilder(candidatePropertyFilter)
            .add("id")
            .add("status")
            .add("candidateNumber")
            .add("publicId")
            .add("allNotifications")
            .add("gender")
            .add("dob")
            .add("phone")
            .add("whatsapp")
            .add("city")
            .add("state")
            .add("externalId")
            .add("externalIdSource")
            .add("partnerRef")
            .add("unhcrRegistered")
            .add("unhcrNumber")
            .add("unhcrStatus")
            .add("unhcrConsent")
            .add("unrwaRegistered")
            .add("unrwaNumber")
            .add("mediaWillingness")
            .add("linkedInLink")
            .add("folderlink")
            .add("sflink")
            .add("videolink")
            .add("surveyComment")
            .add("selected")
            .add("createdDate")
            .add("updatedDate")
            .add("maritalStatus")
            .add("drivingLicense")
            .add("englishAssessmentScoreIelts")
            .add("englishAssessmentScoreDet")
            .add("frenchAssessmentScoreNclc")
            .add("residenceStatus")
            .add("ieltsScore")
            .add("numberDependants")
            .add("regoPartnerParam")
            .add("regoReferrerParam")
            .add("regoUtmCampaign")
            .add("regoUtmContent")
            .add("regoUtmMedium")
            .add("regoUtmSource")
            .add("regoUtmTerm")
            .add("maxEducationLevel", educationLevelDto())
            .add("surveyType", surveyTypeDto())
            .add("country", countryService.selectBuilder())
            .add("nationality", countryService.selectBuilder())
            .add("user", userDto(userPropertyFilter))
            .add("candidateReviewStatusItems", reviewDto())
            .add("candidateAttachments", candidateAttachmentDto(userPropertyFilter, type))
            .add("taskAssignments", TaskDtoHelper.getTaskAssignmentDto(type))
            .add("candidateExams", examsDto())
            .add("candidateOpportunities", candidateOpportunityDto(type))
            .add("miniIntakeCompletedDate")
            .add("fullIntakeCompletedDate")
            .add("potentialDuplicate")
            ;

            if (!DtoType.PREVIEW.equals(type)) {
                builder
                    .add("candidateProperties", candidatePropertyDto())
                    .add("shareableCv", candidateAttachmentDto(userPropertyFilter, type))
                    .add("shareableDoc", candidateAttachmentDto(userPropertyFilter, type))
                    .add("listShareableCv", candidateAttachmentDto(userPropertyFilter, type))
                    .add("listShareableDoc", candidateAttachmentDto(userPropertyFilter, type))
                    .add("miniIntakeCompletedBy", userDto(userPropertyFilter))
                    .add("fullIntakeCompletedBy", userDto(userPropertyFilter))
                    .add("contextNote")
                    .add("shareableNotes")
                    .add("additionalInfo")
                    .add("candidateMessage")
                    .add("additionalInfo")
                ;
            }

            // Extended DTO used for candidate search card information
            if (DtoType.EXTENDED.equals(type)) {
                builder
                    .add("candidateLanguages", candidateLanguageDto())
                    .add("candidateDestinations", candidateDestinationDto())
                    .add("candidateOccupations", candidateOccupationDto())
                    .add("candidateJobExperiences", candidateJobExperienceDto())
                    .add("candidateSkills", candidateSkillDto())
                    .add("candidateEducations", candidateEducationDto())
                    .add("candidateCertifications", candidateCertificationDto())
                    .add("candidateNotes", candidateNoteDto())
                    .add("relocatedAddress")
                    .add("relocatedCity")
                    .add("relocatedState")
                    .add("relocatedCountry", countryService.selectBuilder())
                ;
            }

            return builder;
    }

    private DtoBuilder userDto(DtoPropertyFilter propertyFilter) {
        return new DtoBuilder(propertyFilter)
                .add("id")
                .add("firstName")
                .add("lastName")
                .add("email")
                .add("createdDate")
                .add("updatedDate")
                .add("lastLogin")
                .add("partner", partnerDto())
                ;
    }

    private DtoBuilder minimalCandidateDto() {
        return new DtoBuilder()
            .add("id")
            .add("candidateNumber")
            .add("publicId")
            .add("user", userDto())
            ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
            .add("username")
            .add("email")
            .add("firstName")
            .add("lastName")
            .add("partner", partnerDto())
            ;
    }

    private DtoBuilder candidateOpportunityDto(DtoType type) {
        final DtoBuilder builder = new DtoBuilder()
            .add("id")
            .add("sfId")
            .add("jobOpp", jobDto())
            .add("candidate", shortCandidateDto())
            .add("name")
            .add("stage")
            .add("nextStep")
            .add("nextStepDueDate")
            ;

        if (!DtoType.PREVIEW.equals(type)) {
            builder
                .add("closingComments")
                .add("closingCommentsForCandidate")
                .add("employerFeedback")
                .add("lastActiveStage")
                .add("createdBy", userDto())
                .add("createdDate")
                .add("updatedBy", userDto())
                .add("updatedDate")
                .add("relocatingDependantIds")
            ;
        }
        return builder;
    }

    private DtoBuilder shortCandidateDto() {
        return new DtoBuilder()
            .add("id")
            .add("candidateNumber")
            .add("publicId")
            ;
    }

    private DtoBuilder jobDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("submissionList", savedListDto())
            .add("country", countryService.selectBuilder())
            ;
    }

    private DtoBuilder savedListDto() {
        return new DtoBuilder()
            .add("id")
            ;
    }

    private DtoBuilder partnerDto() {
        return new DtoBuilder()
            .add("abbreviation")
            .add("id")
            .add("name")
            .add("websiteUrl")
            ;
    }

    private DtoBuilder reviewDto() {
        return new DtoBuilder()
                .add("id")
                .add("reviewStatus")
                .add("savedSearch", savedSearchDto())
                ;
    }

    private DtoBuilder surveyTypeDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder savedSearchDto() {
        return new DtoBuilder()
                .add("id")
                ;
    }

    private DtoBuilder candidatePropertyDto() {
        return new DtoBuilder()
                .add("name")
                .add("value")
                ;
    }

    private DtoBuilder educationMajor() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder educationLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("level")
                ;
    }

    private DtoBuilder examsDto() {
        return new DtoBuilder()
                .add("id")
                .add("exam")
                .add("score")
                ;
    }

    private DtoBuilder candidateAttachmentDto(DtoPropertyFilter userPropertyFilter, DtoType type) {
        final DtoBuilder builder = new DtoBuilder()
            .add("id")
            .add("type")
            .add("name")
            .add("location")
            .add("fileType")
            .add("migrated")
            .add("cv")
            .add("createdDate")
            .add("uploadType")
            .add("url")
            ;

        if (!DtoType.PREVIEW.equals(type)) {
            builder
                .add("createdBy", userDto(userPropertyFilter))
            ;
        }

        return builder;
    }
    private DtoBuilder candidateLanguageDto() {
        return new DtoBuilder()
            .add("id")
            .add("migrationLanguage")
            .add("language", languageDto())
            .add("writtenLevel", languageLevelDto())
            .add("spokenLevel",languageLevelDto())
            ;
    }

    private DtoBuilder languageDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }

    private DtoBuilder languageLevelDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("level")
            ;
    }

    private DtoBuilder candidateDestinationDto() {
        return new DtoBuilder()
            .add("id")
            .add("country", countryService.selectBuilder())
            .add("interest")
            .add("notes")
            ;
    }

    private DtoBuilder candidateOccupationDto() {
        return new DtoBuilder()
            .add("id")
            .add("migrationOccupation")
            .add("occupation", occupationService.selectBuilder())
            .add("yearsExperience")
            .add("createdBy", userDto())
            .add("createdDate")
            .add("updatedBy", userDto())
            .add("updatedDate")
            ;
    }

    private DtoBuilder candidateJobExperienceDto() {
        return new DtoBuilder()
            .add("id")
            .add("companyName")
            .add("role")
            .add("startDate")
            .add("endDate")
            .add("fullTime")
            .add("paid")
            .add("description")
            .add("country", countryService.selectBuilder())
            .add("candidateOccupation", candidateOccupationDto())
            ;
    }

    private DtoBuilder candidateSkillDto() {
        return new DtoBuilder()
            .add("id")
            .add("skill")
            .add("timePeriod")
            ;
    }

    private DtoBuilder candidateEducationDto() {
        return new DtoBuilder()
            .add("id")
            .add("educationType")
            .add("country", countryService.selectBuilder())
            .add("educationMajor", majorDto())
            .add("lengthOfCourseYears")
            .add("institution")
            .add("courseName")
            .add("yearCompleted")
            .add("incomplete")
            ;
    }

    private DtoBuilder majorDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("status")
            ;
    }

    private DtoBuilder candidateCertificationDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("institution")
            .add("dateCompleted")
            ;
    }

    private DtoBuilder candidateNoteDto() {
        return new DtoBuilder()
            .add("id")
            .add("noteType")
            .add("title")
            .add("comment")
            .add("createdBy", userDto())
            .add("createdDate")
            .add("updatedBy", userDto())
            .add("updatedDate")
            ;
    }
}
