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

import org.tctalent.server.model.sf.Opportunity;

public class OpportunityTestData {

    public static Opportunity getOpportunityForCandidate() {
        Opportunity o = new Opportunity();
        o.setName("Opportunity for candidate");
        o.setId("006Uu08800HRBBRIAT");
        o.setParentOpportunityId("016Uu0444GGSRAAT");
        o.setCandidateId("66JGG888GGDFDDDDFGH");
        o.setStageName("Relocated");
        o.setClosingCommentsForCandidate("Closing comments");
        o.setClosingComments("Closing comments");
        o.setEmployerFeedback("Employer feedback");
        o.setNextStep("Next step");
        o.setNextStepDueDate("2025-12-03");
        return o;
    }

    public static Opportunity getOpportunityForJob() {
        Opportunity o = new Opportunity();
        o.setName("Opportunity for job");
        o.setAccountId("077Uu11111IGGAHHA5");
        o.setOwnerId("037Uu0fff0IGDAEIA7");
        o.setClosed(false);
        o.setClosingComments("Closing comments");
        o.setNextStep("Next step");
        o.setWon(false);
        o.setHiringCommitment(3L);
        o.setStageName("Recruitment process");
        o.setNextStepDueDate("2025-12-03");
        o.setCreatedDate("2024-12-01T00:21:58.000+0000");
        o.setLastModifiedDate("2025-03-05T00:20:53.000+0000");
        return o;
    }

}
