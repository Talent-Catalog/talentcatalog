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

package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
class CandidateFormInstanceTest {

    static private class TestForm extends CandidateFormInstance {
        private static final String HAIR_COLOUR_PROPERTY_NAME = "hairColour";

        public String getCity() {
            return getCandidate().getCity();
        }

        public void setCity(String city) {
            getCandidate().setCity(city);
        }

        //TODO JC Test linked Candidate tables - eg CandidateExam

        public String getHairColour() {
            return getProperty(HAIR_COLOUR_PROPERTY_NAME);
        }

        public void setHairColour(String hairColour) {
            setProperty(HAIR_COLOUR_PROPERTY_NAME, hairColour);
        }
    }

    Candidate candidate;
    TestForm testForm;

    @BeforeEach
    void setUp() {
        candidate = new Candidate();
        candidate.setId(99L);
        testForm = new TestForm();
        testForm.setCandidate(candidate);
    }

    @Test
    void testDelegation() {
        String city;
        city = testForm.getCity();
        assertNull(city);

        testForm.setCity("Aman");
        city = testForm.getCity();
        assertEquals("Aman", city);

        testForm.setCity(null);
        city = testForm.getCity();
        assertNull(city);
    }

    @Test
    void testProperties() {
        String colour;
        colour = testForm.getHairColour();
        assertNull(colour);

        testForm.setHairColour("red");
        colour = testForm.getHairColour();
        assertEquals("red", colour);

        testForm.setHairColour("blue");
        colour = testForm.getHairColour();
        assertEquals("blue", colour);

        testForm.setHairColour(null);
        colour = testForm.getHairColour();
        assertNull(colour);
    }
}
