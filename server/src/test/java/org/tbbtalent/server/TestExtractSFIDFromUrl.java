/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tbbtalent.server.util.SalesforceHelper;

/**
 * Test id from SFLink extraction
 *
 * @author John Cameron
 */
public class TestExtractSFIDFromUrl {

    @Test
    @DisplayName("correctly extracts SF object info from SF url")
    void testExtract() {
        String url = "https://talentbeyondboundaries--sfstaging.sandbox.lightning.force.com/lightning/r/Opportunity/0063l00000onLl3AAE/view";
        String id = SalesforceHelper.extractIdFromSfUrl(url);
        assertNotNull(id);
        String objectType = SalesforceHelper.extractObjectTypeFromSfUrl(url);
        assertEquals("Opportunity", objectType);
        url = "https://talentbeyondboundaries--sfstaging.sandbox.lightning.force.com/lightning/r/Opportunity/0063l00000onLl3AAE";
        id = SalesforceHelper.extractIdFromSfUrl(url);
        assertNotNull(id);
        objectType = SalesforceHelper.extractObjectTypeFromSfUrl(url);
        assertEquals("Opportunity", objectType);
    }
}
