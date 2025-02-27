/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.service.db;

import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.job.UpdateJobRequest;

/**
 * Manages notifications that go out as the result of changes to opportunities
 *
 * @author John Cameron
 */
public interface OppNotificationService {

    /**
     * Sends out posts on relevant chats notifying subscribers about the given changes to the given
     * candidate opportunity (case).
     * @param opp Candidate opportunity
     * @param changes Changes to opportunity
     */
    void notifyCaseChanges(CandidateOpportunity opp, CandidateOpportunityParams changes);

    /**
     * Sends out posts on relevant chats notifying subscribers about the given new
     * candidate opportunity (case).
     * @param opp Candidate opportunity
     */
    void notifyNewCase(CandidateOpportunity opp);

    /**
     * Sends out posts on relevant chats notifying subscribers about the given changes to the given
     * job opportunity.
     * @param job Job opportunity
     * @param changes Changes to opportunity
     */
    void notifyJobOppChanges(SalesforceJobOpp job, UpdateJobRequest changes);

    /**
     * Sends out posts on relevant chats notifying subscribers about the given new
     * job opportunity.
     * @param job Job opportunity
     */
    void notifyNewJobOpp(SalesforceJobOpp job);
}
