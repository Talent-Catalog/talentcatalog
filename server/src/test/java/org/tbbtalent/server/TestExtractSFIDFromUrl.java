/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server;

import org.junit.jupiter.api.Test;
import org.tbbtalent.server.service.db.impl.SalesforceServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test id from SFLink extraction
 *
 * @author John Cameron
 */
public class TestExtractSFIDFromUrl {
    
    @Test
    void testExtract() {
        String url = "https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/0061N00000gO3zXQAS/view";
        String id = SalesforceServiceImpl.extractIdFromSfUrl(url);
        assertNotNull(id);
        String objectType = SalesforceServiceImpl.extractObjectTypeFromSfUrl(url);
        assertEquals("Opportunity", objectType);
        url = "https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/0061N00000gO3zXQAS";
        id = SalesforceServiceImpl.extractIdFromSfUrl(url);
        assertNotNull(id);
        objectType = SalesforceServiceImpl.extractObjectTypeFromSfUrl(url);
        assertEquals("Opportunity", objectType);
    } 
}
