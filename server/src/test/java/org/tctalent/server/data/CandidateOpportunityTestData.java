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

import java.time.LocalDate;
import org.tctalent.server.api.admin.AdminApiTestUtil;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;

public class CandidateOpportunityTestData {

    public static CandidateOpportunity getCandidateOpportunity() {
        CandidateOpportunity opp = new CandidateOpportunity();
        opp.setNextStep("Next step");
        opp.setNextStepDueDate(LocalDate.now().plusDays(14));
        opp.setStage(CandidateOpportunityStage.cvReview);
        opp.setCandidate(AdminApiTestUtil.getFullUser().getCandidate());
        return opp;
    }

}
