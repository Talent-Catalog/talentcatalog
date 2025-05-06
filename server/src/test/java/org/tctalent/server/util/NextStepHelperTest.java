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

package org.tctalent.server.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.util.NextStepHelper.auditStampNextStepIfChanged;
import static org.tctalent.server.util.NextStepHelper.constructNextStepAuditStamp;
import static org.tctalent.server.util.NextStepHelper.isNextStepDifferent;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test NextStepHelper
 *
 * @author John Cameron
 */
class NextStepHelperTest {

    String name;
    LocalDate date;
    String strippedNextStep;

    @BeforeEach
    void setUp() {
        name = "Jim";
        date = LocalDate.of(2024, Month.MARCH, 1);
        strippedNextStep = "This is a next step";
    }

    @Test
    void checkAuditStampFormat() {
        String stamp = constructNextStepAuditStamp(name, date, strippedNextStep);
        assertEquals("01Mar24| This is a next step --Jim", stamp);
    }

    @Test
    void doNothingIfRequestNextStepIsNull() {
       String currentNextStep = "anything";
       String processed = auditStampNextStepIfChanged(name, date, currentNextStep, null);
       assertEquals(processed, currentNextStep);
    }

    @Test
    void doNothingIfNextStepUnchanged() {
        String currentNextStep = "anything";
        String requestedNextStep = currentNextStep;
        String processed = auditStampNextStepIfChanged(name, date, currentNextStep, requestedNextStep);
        assertEquals(processed, currentNextStep);
    }

    @Test
    void doNothingIfBothNextStepsAreNull() {
        String currentNextStep = null;
        String requestedNextStep = null;
        String processed = auditStampNextStepIfChanged(name, date, currentNextStep, requestedNextStep);
        assertNull(processed);
    }

    @Test
    void auditStampChangedNextStepWhenCurrentIsNotStamped() {
        String currentNextStep = "Complete intake";
        String requestedNextStep = "Prepare CV";
        String processed = auditStampNextStepIfChanged(name, date, currentNextStep, requestedNextStep);
        assertEquals(constructNextStepAuditStamp(name, date, requestedNextStep), processed);
    }

    @Test
    void ignoreExistingStampOnRequest() {
        String currentNextStep = "Complete intake";
        String requestedNextStepUnstamped = "Prepare CV";
        String requestedNextStep = "01Mar24| " + requestedNextStepUnstamped + " --john";
        String processed = auditStampNextStepIfChanged(name, date, currentNextStep, requestedNextStep);
        assertEquals(constructNextStepAuditStamp(name, date, requestedNextStepUnstamped), processed);
    }

    @Test
    void auditStampChangedNextStepWhenCurrentIsAlreadyStamped() {
        String currentNextStep = "Complete intake --240301 john";
        String requestedNextStep = "Prepare CV";
        String processed = auditStampNextStepIfChanged(name, date, currentNextStep, requestedNextStep);
        assertEquals(constructNextStepAuditStamp(name, date, requestedNextStep), processed);
    }

    @Test
    void isNextStepDifferent_nullCurrentNextStep_returnsTrue() {
        assertTrue(isNextStepDifferent(null, "A step."));
    }

    @Test
    void auditStampNextStepIfChanged_noChange_returnsFalse() {
        String nextStep = "Complete intake";
        assertFalse(isNextStepDifferent(nextStep, nextStep));
    }

    @Test
    void auditStampNextStepIfChanged_sameValueDiffAuditStamp_returnsFalseIfChanged() {
        String currentNextStep = "01Mar24| Complete intake --john";
        String requestedNextStep = "03Mar24| Complete intake --sam";
        assertFalse(isNextStepDifferent(currentNextStep, requestedNextStep));
    }

    @Test
    void auditStampNextStepIfChanged_diffValueSameAuditStamp_returnsTrueIfChanged() {
        String currentNextStep = "01Mar24| Complete intake --john";
        String requestedNextStep = "01Mar24| Do nothing --john";
        assertTrue(isNextStepDifferent(currentNextStep, requestedNextStep));
    }

}
