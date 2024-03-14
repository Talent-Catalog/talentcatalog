/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.model.db;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tctalent.server.model.sf.OpportunityHistory;
import org.tctalent.server.util.SalesforceHelper;

/**
 * Stage history item associated with a Candidate Opportunity.
 * <p/>
 * Records that the opp stage was changed to the give value at the given time.
 *
 * @author John Cameron
 */
@Getter
@Setter
public class CandidateOpportunityStageHistory {
    private static final Logger log = LoggerFactory.getLogger(CandidateOpportunityStageHistory.class);

    /**
     * Stage
     */
    private CandidateOpportunityStage stage;

    /**
     * Time at which stage was set to above value
     */
    private OffsetDateTime timeStamp;

    /**
     * Populates this object from the given history from Salesforce.
     * <p/>
     * This involves decoding a date stamp string and a stage name string into an OffsetDateTime and
     * a CandidateOpportunityStage.
     * @param sfHistory A Salesforce OpportunityHistory
     */
    public void decodeFromSfHistory(OpportunityHistory sfHistory) {
        String oppId = sfHistory.getOpportunityId();

        String sfDateStamp = sfHistory.getSystemModstamp();
        if (sfDateStamp != null) {
            try {
                setTimeStamp(SalesforceHelper.parseSalesforceOffsetDateTime(sfDateStamp));
            } catch (DateTimeParseException ex) {
                log.error("Error decoding timeStamp from SF opp history: " + sfDateStamp +
                    " in candidate opp " + oppId);
            }
        }
        CandidateOpportunityStage stage;
        try {
            stage = CandidateOpportunityStage.textToEnum(sfHistory.getStageName());
        } catch (IllegalArgumentException e) {
            log.error("Error decoding stage in load: " + sfHistory.getStageName() +
                " in candidate opp " + oppId);
            stage = CandidateOpportunityStage.prospect;
        }
        setStage(stage);
    }
}
