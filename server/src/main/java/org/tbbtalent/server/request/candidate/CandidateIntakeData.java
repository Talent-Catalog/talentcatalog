/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate;

import java.util.List;

import org.tbbtalent.server.model.db.AvailImmediateReason;
import org.tbbtalent.server.model.db.HasPassport;
import org.tbbtalent.server.model.db.VisaIssue;
import org.tbbtalent.server.model.db.YesNo;
import org.tbbtalent.server.model.db.YesNoUnsure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Holds all candidate intake fields.
 * <p/>
 * When used for updates, null fields are ignored.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class CandidateIntakeData {

    private YesNoUnsure availImmediate;
    private AvailImmediateReason availImmediateReason;
    private String availImmediateNotes;

    private Long citizenNationalityId;
    private HasPassport citizenHasPassport;
    private String citizenNotes;

    private YesNo familyMove;
    private String familyMoveNotes;
    private YesNo familyHealthConcern;
    private String familyHealthConcernNotes;

    private YesNoUnsure returnedHome;
    private String returnedHomeNotes;
    private String returnedHomeReason;

    private List<VisaIssue> visaIssues;
    private String visaIssuesNotes;
}
