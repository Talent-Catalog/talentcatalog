/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.util.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.HelpFocus;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;

/**
 * Test HelpLinkHelper
 *
 * @author John Cameron
 */
class HelpLinkHelperTest {
    SearchHelpLinkRequest request;

    @BeforeEach
    void setUp() {
        request = new SearchHelpLinkRequest();
    }

    @Test
    void neverReturnsEmptySequence() {
        List<SearchHelpLinkRequest> requests = HelpLinkHelper.generateRequestSequence(request);
        assertNotNull(requests);
        assertNotEquals(0, requests.size());
    }

    @Test
    void stageRequestChildRequestsWithCountryAndFocus() {
        request.setJobStage(JobOpportunityStage.jobOffer);
        request.setCountryId(123L);
        request.setFocus(HelpFocus.updateStage);

        List<SearchHelpLinkRequest> requests = HelpLinkHelper.generateRequestSequence(request);
        assertNotNull(requests);
        assertEquals(4, requests.size());

        int i;

        //Original request
        i = 0;
        assertEquals(JobOpportunityStage.jobOffer, requests.get(i).getJobStage());
        assertEquals(123L, requests.get(i).getCountryId());
        assertEquals(HelpFocus.updateStage, requests.get(i).getFocus());

        //Original request without country
        i = 1;
        assertEquals(JobOpportunityStage.jobOffer, requests.get(i).getJobStage());
        assertNull(requests.get(i).getCountryId());
        assertEquals(HelpFocus.updateStage, requests.get(i).getFocus());

        //Original request without focus
        i = 2;
        assertEquals(JobOpportunityStage.jobOffer, requests.get(i).getJobStage());
        assertEquals(123L, requests.get(i).getCountryId());
        assertNull(requests.get(i).getFocus());

        //Original request without focus or country
        i = 3;
        assertEquals(JobOpportunityStage.jobOffer, requests.get(i).getJobStage());
        assertNull(requests.get(i).getCountryId());
        assertNull(requests.get(i).getFocus());

    }

    @Test
    void stageRequestChildRequestsWithJustCountry() {
        request.setJobStage(JobOpportunityStage.jobOffer);
        request.setCountryId(123L);

        List<SearchHelpLinkRequest> requests = HelpLinkHelper.generateRequestSequence(request);
        assertNotNull(requests);
        assertEquals(2, requests.size());

        int i;

        //Original request
        i = 0;
        assertEquals(JobOpportunityStage.jobOffer, requests.get(i).getJobStage());
        assertEquals(123L, requests.get(i).getCountryId());
        assertNull(requests.get(i).getFocus());

        //Original request without country
        i = 1;
        assertEquals(JobOpportunityStage.jobOffer, requests.get(i).getJobStage());
        assertNull(requests.get(i).getFocus());
        assertNull(requests.get(i).getCountryId());
    }

    @Test
    void stageRequestChildRequestsWithJustFocus() {
        request.setJobStage(JobOpportunityStage.jobOffer);
        request.setFocus(HelpFocus.updateStage);

        List<SearchHelpLinkRequest> requests = HelpLinkHelper.generateRequestSequence(request);
        assertNotNull(requests);
        assertEquals(2, requests.size());

        int i;

        //Original request
        i = 0;
        assertEquals(JobOpportunityStage.jobOffer, requests.get(i).getJobStage());
        assertNull(requests.get(i).getCountryId());
        assertEquals(HelpFocus.updateStage, requests.get(i).getFocus());

        //Original request without focus
        i = 1;
        assertEquals(JobOpportunityStage.jobOffer, requests.get(i).getJobStage());
        assertNull(requests.get(i).getFocus());
        assertNull(requests.get(i).getCountryId());
    }
}
