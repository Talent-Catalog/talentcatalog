/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import org.springframework.lang.NonNull;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.TaskDtoHelper;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.dto.DtoBuilder;
import org.tctalent.server.util.dto.DtoPropertyFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility for selecting the right DTO build based on the currently logged in
 * user.
 *
 * @author John Cameron
 */
public class CandidateBuilderSelector {
    private final UserService userService;

    private final Set<String> candidatePublicProperties =
        new HashSet<>(Arrays.asList(
            "id",
            "status",
            "candidateNumber",
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

    public CandidateBuilderSelector(UserService userService) {
        this.userService = userService;
    }

    @NonNull
    public DtoBuilder selectBuilder() {
        User user = userService.getLoggedInUser();
        Partner partner = user == null ? null : user.getPartner();

        //Default to Role.limited if user is null.
        Role role = user == null ? Role.limited : user.getRole();
        DtoPropertyFilter candidatePropertyFilter = new PartnerAndRoleBasedDtoPropertyFilter(
            partner, role, candidatePublicProperties, candidateSemiLimitedExtraProperties);
        DtoPropertyFilter userPropertyFilter = new PartnerAndRoleBasedDtoPropertyFilter(
            partner, role, userPublicProperties, null);
        return candidateDto(candidatePropertyFilter, userPropertyFilter);
    }

    private DtoBuilder candidateDto(
        DtoPropertyFilter candidatePropertyFilter, DtoPropertyFilter userPropertyFilter) {
        return new DtoBuilder(candidatePropertyFilter)
            .add("id")
            .add("status")
            .add("candidateNumber")
            .add("gender")
            .add("dob")
            .add("phone")
            .add("whatsapp")
            .add("city")
            .add("state")
            .add("address1")
            .add("yearOfArrival")
            .add("externalId")
            .add("externalIdSource")
            .add("partnerRef")
            .add("unhcrRegistered")
            .add("unhcrNumber")
            .add("unhcrStatus")
            .add("unhcrConsent")
            .add("unrwaRegistered")
            .add("unrwaNumber")
            .add("additionalInfo")
            .add("mediaWillingness")
            .add("linkedInLink")
            .add("candidateMessage")
            .add("folderlink")
            .add("sflink")
            .add("videolink")
            .add("surveyComment")
            .add("selected")
            .add("createdDate")
            .add("updatedDate")
            .add("contextNote")
            .add("maritalStatus")
            .add("drivingLicense")
            .add("langAssessmentScore")
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
            .add("candidateExams", examsDto())
            .add("maxEducationLevel", educationLevelDto())
            .add("surveyType", surveyTypeDto())
            .add("country", countryDto())
            .add("nationality", countryDto())
            .add("candidateOpportunities", candidateOpportunityDto())
            .add("user", userDto(userPropertyFilter))
            .add("candidateReviewStatusItems", reviewDto())

            .add("candidateAttachments", candidateAttachmentDto(userPropertyFilter))
            .add("shareableCv", candidateAttachmentDto(userPropertyFilter))
            .add("shareableDoc", candidateAttachmentDto(userPropertyFilter))
            .add("listShareableCv", candidateAttachmentDto(userPropertyFilter))
            .add("listShareableDoc", candidateAttachmentDto(userPropertyFilter))
            .add("taskAssignments", TaskDtoHelper.getTaskAssignmentDto())
            .add("candidateProperties", candidatePropertyDto())
            .add("shareableNotes")
            .add("miniIntakeCompletedBy", userDto(userPropertyFilter))
            .add("miniIntakeCompletedDate")
            .add("fullIntakeCompletedBy", userDto(userPropertyFilter))
            .add("fullIntakeCompletedDate")

            ;
    }

    private DtoBuilder userDto(DtoPropertyFilter propertyFilter) {
        return new DtoBuilder(propertyFilter)
                .add("id")
                .add("firstName")
                .add("lastName")
                .add("email")
                .add("createdDate")
                .add("updatedDate")
                .add("partner", partnerDto())
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
            .add("username")
            .add("email")
            .add("firstName")
            .add("lastName")
            ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder candidateOpportunityDto() {
        return new DtoBuilder()
                .add("id")
                .add("sfId")
            .add("candidate", shortCandidateDto())
            .add("closingComments")
            .add("closingCommentsForCandidate")
            .add("employerFeedback")
            .add("jobOpp", jobDto())
            .add("name")
            .add("nextStep")
            .add("nextStepDueDate")
            .add("stage")
            .add("createdBy", userDto())
            .add("createdDate")
            .add("updatedBy", userDto())
            .add("updatedDate")

            ;
    }

    private DtoBuilder shortCandidateDto() {
        return new DtoBuilder()
            .add("candidateNumber")
            ;
    }

    private DtoBuilder jobDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("submissionList", savedListDto())
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

    private DtoBuilder candidateAttachmentDto(DtoPropertyFilter userPropertyFilter) {
        return new DtoBuilder()
            .add("id")
            .add("type")
            .add("name")
            .add("location")
            .add("fileType")
            .add("migrated")
            .add("cv")
            .add("createdBy", userDto(userPropertyFilter))
            .add("createdDate")
            .add("uploadType")
            .add("url")
            ;
    }
}
