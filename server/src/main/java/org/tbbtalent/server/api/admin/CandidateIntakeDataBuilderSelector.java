/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.api.admin;

import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.constraints.NotNull;

/**
 * Utility for selecting the right DTO build based on the currently logged in
 * user.
 *
 * @author John Cameron
 */
public class CandidateIntakeDataBuilderSelector {
    private final UserContext userContext;

    public CandidateIntakeDataBuilderSelector(UserContext userContext) {
        this.userContext = userContext;
    }

    private @Nullable Role getRole() {
        User user = userContext.getLoggedInUser();
        return user == null ? null : user.getRole();
    }

    public @NotNull DtoBuilder selectBuilder() {
        return candidateIntakeDto();
    }

    private DtoBuilder candidateIntakeDto() {
        return new DtoBuilder()
                .add("asylumYear")
                .add("availImmediate")
                .add("availImmediateReason")
                .add("availImmediateNotes")

                .add("candidateCitizenships", candidateCitizenshipDto())

                .add("candidateDestinations", candidateDestinationDto())

                .add("candidateExams", candidateExamDto())
                
                .add("candidateVisaChecks", candidateVisaCheckDto())

                .add("canDrive")

                .add("children")
                .add("childrenAge")

                .add("conflict")
                .add("conflictNotes")

                .add("crimeConvict")
                .add("crimeConvictNotes")

                .add("dependants")
                .add("dependantsNotes")

                .add("destLimit")
                .add("destLimitNotes")

                .add("destJob")
                .add("destJobNotes")

                .add("drivingLicense")
                .add("drivingLicenseExp")
                .add("drivingLicenseCountry", countryDto())

                .add("familyMove")
                .add("familyMoveNotes")
                .add("familyHealthConcern")
                .add("familyHealthConcernNotes")

                .add("homeLocation")
                .add("hostChallenges")
                .add("hostBorn")
                .add("hostEntryYear")
                .add("hostEntryLegally")
                .add("intRecruitReasons")
                .add("intRecruitRural")

                .add("leftHomeReason")
                .add("leftHomeOther")
                .add("militaryService")

                .add("maritalStatus")
                .add("partnerRegistered")
                .add("partnerCandidate", partnerCandidateDto())
                .add("partnerEduLevel", englishLevelDto())
                .add("partnerProfession", occupationDto())
                .add("partnerEnglish")
                .add("partnerEnglishLevel", languageLevelDto())
                .add("partnerIelts")
                .add("partnerIeltsScore")
                .add("partnerCitizenship", nationalityDto())

                .add("residenceStatus")

                .add("returnedHome")
                .add("returnedHomeReason")
                .add("returnedHomeNotes")
                .add("returnHomeSafe")
                .add("returnHomeFuture")
                .add("returnHomeWhen")

                .add("resettleThird")
                .add("resettleThirdStatus")

                .add("unhcrStatus")
                .add("unhcrOldStatus")
                .add("unhcrNumber")
                .add("unhcrFile")
                .add("unhcrNotes")
                .add("unhcrPermission")

                .add("unrwaStatus")
                .add("unrwaNumber")
                .add("unrwaNotes")

                .add("visaReject")
                .add("visaIssues")
                .add("visaIssuesNotes")

                .add("workAbroad")
                .add("workAbroadLoc", countryDto())
                .add("workAbroadYrs")
                .add("workPermit")
                .add("workPermitDesired")
                .add("workLegally")
                ;
    }

    private DtoBuilder candidateCitizenshipDto() {
        return new DtoBuilder()
                .add("id")
                .add("nationality", nationalityDto())
                .add("hasPassport")
                .add("notes")
                ;
    }

    private DtoBuilder candidateExamDto() {
        return new DtoBuilder()
                .add("id")
                .add("exam")
                .add("otherExam")
                .add("score")
                ;
    }

    private DtoBuilder candidateDestinationDto() {
        return new DtoBuilder()
                .add("id")
                .add("country", countryDto())
                .add("interest")
                .add("family")
                .add("location")
                .add("notes")
                ;
    }

    private DtoBuilder candidateVisaCheckDto() {
        return new DtoBuilder()
                .add("id")
                .add("country", countryDto())
                .add("eligibility")
                .add("assessmentNotes")
                .add("createdBy", userDto())
                .add("createdDate")
                .add("updatedBy", userDto())
                .add("updatedDate")
                .add("protection")
                .add("protectionGrounds")
                .add("tbbEligibilityAssessment")
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                ;
    }

    private DtoBuilder nationalityDto() {
        return new DtoBuilder()
                .add("id")
                ;
    }

    private DtoBuilder occupationDto() {
        return new DtoBuilder()
                .add("id")
                ;
    }

    private DtoBuilder englishLevelDto() {
        return new DtoBuilder()
                .add("id")
                ;
    }

    private DtoBuilder languageLevelDto() {
        return new DtoBuilder()
                .add("id")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder partnerCandidateDto() {
        return new DtoBuilder()
                .add("id")
                .add("candidateNumber")
                .add("user", userDto())
                ;
    }

}
