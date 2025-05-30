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

import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.Status;

public class HelpLinkTestData {

    public static HelpLink getHelpLink() {
        HelpLink helpLink = new HelpLink();
        helpLink.setId(99L);
        helpLink.setCountry(new Country("Jordan", Status.active));
        helpLink.setCaseStage(CandidateOpportunityStage.cvReview);
        helpLink.setJobStage(JobOpportunityStage.jobOffer);
        helpLink.setLabel("Test label");
        helpLink.setLink("https://www.talentbeyondboundaries.org/");
        return helpLink;
    }

}
