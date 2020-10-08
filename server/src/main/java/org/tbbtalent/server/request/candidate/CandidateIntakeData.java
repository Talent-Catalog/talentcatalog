/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tbbtalent.server.model.db.*;

import java.time.LocalDate;
import java.util.List;

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

    private List<IntRecruitReason> intRecruitReasons;
    private YesNoUnsure intRecruitRural;

    private YesNoUnsure returnHomeSafe;

    private WorkPermit workPermit;
    private YesNoUnsure workPermitDesired;
    private YesNo workLegally;
    private LocalDate hostEntryYear;
}
