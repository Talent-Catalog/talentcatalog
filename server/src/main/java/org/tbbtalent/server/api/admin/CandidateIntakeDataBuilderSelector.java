/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.api.admin;

import javax.validation.constraints.NotNull;

import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.util.dto.DtoBuilder;

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
                .add("returnedHome")
                .add("returnedHomeNotes")
                .add("returnedHomeReason")

                .add("visaIssues")
                .add("visaIssuesNotes")
                ;
    }

}
