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

package org.tctalent.server.data;

import java.util.List;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;

public class CandidateOpportunityTestData {

    public static CandidateOpportunity getCandidateOpportunity() {
        CandidateOpportunity opportunity = new CandidateOpportunity();
        opportunity.setCandidate(CandidateTestData.getCandidate());
        opportunity.setClosingCommentsForCandidate("Some closing comments for candidate");
        opportunity.setEmployerFeedback("Some employer feedback");
        opportunity.setStage(CandidateOpportunityStage.offer);
        opportunity.setRelocatingDependantIds(List.of(1L, 2L));
        return opportunity;
    }

}
