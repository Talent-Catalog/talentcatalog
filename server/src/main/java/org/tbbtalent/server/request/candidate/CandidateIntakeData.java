/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate;

import java.util.List;

import org.tbbtalent.server.model.db.ReturnedHome;
import org.tbbtalent.server.model.db.VisaIssue;

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
    private ReturnedHome returnedHome;
    private String returnedHomeNotes; 
    private String returnedHomeReason;
    
    private List<VisaIssue> visaIssues;
    private String visaIssuesNotes;
}
