/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server;

import org.junit.jupiter.api.Test;
import org.tbbtalent.server.model.db.Candidate;

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
        String id = Candidate.extractIdFromUrl(url);
        assertNotNull(id);
        url = "https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/0061N00000gO3zXQAS";
        id = Candidate.extractIdFromUrl(url);
        assertNotNull(id);
    } 
}
