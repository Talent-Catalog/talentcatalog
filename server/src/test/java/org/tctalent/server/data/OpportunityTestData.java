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

    public static Opportunity getOpportunity() {
        Opportunity o = new Opportunity();
        o.setName("Opportunity");
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

}
