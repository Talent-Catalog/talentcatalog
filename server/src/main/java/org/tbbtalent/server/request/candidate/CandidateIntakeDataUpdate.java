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
 * Has fields for all candidate intake fields.
 * <p/>
 * An instance of this class is received from the browser on each update.
 * Each update will come from a single intake component - comprising one or
 * a small number of fields. Just values for those fields will be populated 
 * in the class. All other fields will be null.
 * <p/>
 * Null fields are ignored - non null fields update the database.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class CandidateIntakeDataUpdate {

    private YesNoUnsure availImmediate;
    private AvailImmediateReason availImmediateReason;
    private String availImmediateNotes;

    private Long citizenId;
    private Long citizenNationalityId;
    private HasPassport citizenHasPassport;
    private String citizenNotes;

    private YesNo familyMove;
    private String familyMoveNotes;
    private YesNo familyHealthConcern;
    private String familyHealthConcernNotes;
    private String homeLocation;
    private LocalDate hostEntryYear;
    private List<IntRecruitReason> intRecruitReasons;
    private YesNoUnsure intRecruitRural;

    private YesNoUnsure returnedHome;
    private String returnedHomeNotes;
    private String returnedHomeReason;
    private YesNoUnsure returnHomeSafe;
    private UnhcrStatus unhcrStatus;
    private UnhcrStatus unhcrOldStatus;
    private String unhcrNumber;
    private Long unhcrFile;
    private String unhcrNotes;
    private YesNo unhcrPermission;
    private YesNoUnsure unrwaRegistered;
    private YesNoUnsure unrwaWasRegistered;
    private String unrwaNumber;
    private String unrwaNotes;

    private List<VisaIssue> visaIssues;
    private String visaIssuesNotes;
    private WorkPermit workPermit;
    private YesNoUnsure workPermitDesired;
    private YesNo workLegally;

}
