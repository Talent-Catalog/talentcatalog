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
                .add("availImmediate")
                .add("availImmediateReason")
                .add("availImmediateNotes")

                .add("candidateCitizenships", candidateCitizenshipDto())

                .add("familyMove")
                .add("familyMoveNotes")
                .add("familyHealthConcern")
                .add("familyHealthConcernNotes")

                .add("homeLocation")
                .add("hostEntryYear")
                .add("intRecruitReasons")
                .add("intRecruitRural")

                .add("returnedHome")
                .add("returnedHomeReason")
                .add("returnedHomeNotes")
                .add("returnHomeSafe")

                .add("unhcrStatus")
                .add("unhcrOldStatus")
                .add("unhcrNumber")
                .add("unhcrFile")
                .add("unhcrNotes")
                .add("unhcrPermission")

                .add("unrwaRegistered")
                .add("unrwaWasRegistered")
                .add("unrwaNumber")
                .add("unrwaNotes")

                .add("visaIssues")
                .add("visaIssuesNotes")

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

    private DtoBuilder nationalityDto() {
        return new DtoBuilder()
                .add("id")
                ;
    }

}
