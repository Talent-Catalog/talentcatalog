/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tbbtalent.server.model.db.AvailImmediate;
import org.tbbtalent.server.model.db.AvailImmediateReason;
import org.tbbtalent.server.model.db.ReturnedHome;
import org.tbbtalent.server.model.db.VisaIssue;

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
    private ReturnedHome returnedHome;
    private String returnedHomeNotes; 
    private String returnedHomeReason;
    
    private List<VisaIssue> visaIssues;
    private String visaIssuesNotes;

    private AvailImmediate availImmediate;
    private AvailImmediateReason availImmediateReason;
    private String availImmediateNotes;
}
