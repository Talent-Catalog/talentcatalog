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
import static org.tctalent.server.util.NextStepHelper.auditStampNextStep;
import static org.tctalent.server.util.NextStepHelper.constructNextStepAuditStamp;
import static org.tctalent.server.util.NextStepHelper.isNextStepDifferent;
import static org.tctalent.server.util.NextStepHelper.isNextStepInfoChanged;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.NextStepWithDueDate;

/**
 * Test NextStepHelper
 *
 * @author John Cameron
 */
class NextStepHelperTest {
    private static final String PROCESSED_NEXT_STEP = "07May25| Test --SystemAdmin";
    private static final LocalDate DUE_DATE = LocalDate.of(1970, 1, 1);

    String name;
    LocalDate date;
    String strippedNextStep;
    NextStepWithDueDate currentNextStepWithDueDate;
    NextStepWithDueDate requestedNextStepWithDueDate;

    @BeforeEach
    void setUp() {
        name = "Jim";
        date = LocalDate.of(2024, Month.MARCH, 1);
        strippedNextStep = "This is a next step";
        currentNextStepWithDueDate = new NextStepWithDueDate(PROCESSED_NEXT_STEP, DUE_DATE);
    }

    @Test
    void checkAuditStampFormat() {
        String stamp = constructNextStepAuditStamp(name, date, strippedNextStep);
        assertEquals("01Mar24| This is a next step --Jim", stamp);
    }

    @Test
    void doNothingIfRequestNextStepIsNull() {
       String currentNextStep = "anything";
       String processed = auditStampNextStep(name, date, currentNextStep, null);
       assertEquals(processed, currentNextStep);
    }

    @Test
    void doNothingIfNextStepUnchanged() {
        String currentNextStep = "anything";
        String requestedNextStep = currentNextStep;
        String processed = auditStampNextStep(name, date, currentNextStep, requestedNextStep);
        assertEquals(processed, currentNextStep);
    }

    @Test
    void doNothingIfBothNextStepsAreNull() {
        String currentNextStep = null;
        String requestedNextStep = null;
        String processed = auditStampNextStep(name, date, currentNextStep, requestedNextStep);
        assertNull(processed);
    }

    @Test
    void auditStampChangedNextStepWhenCurrentIsNotStamped() {
        String currentNextStep = "Complete intake";
        String requestedNextStep = "Prepare CV";
        String processed = auditStampNextStep(name, date, currentNextStep, requestedNextStep);
        assertEquals(constructNextStepAuditStamp(name, date, requestedNextStep), processed);
    }

    @Test
    void ignoreExistingStampOnRequest() {
        String currentNextStep = "Complete intake";
        String requestedNextStepUnstamped = "Prepare CV";
        String requestedNextStep = "01Mar24| " + requestedNextStepUnstamped + " --john";
        String processed = auditStampNextStep(name, date, currentNextStep, requestedNextStep);
        assertEquals(constructNextStepAuditStamp(name, date, requestedNextStepUnstamped), processed);
    }

    @Test
    void auditStampChangedNextStepWhenCurrentIsAlreadyStamped() {
        String currentNextStep = "Complete intake --240301 john";
        String requestedNextStep = "Prepare CV";
        String processed = auditStampNextStep(name, date, currentNextStep, requestedNextStep);
        assertEquals(constructNextStepAuditStamp(name, date, requestedNextStep), processed);
    }

    @Test
    void isNextStepDifferent_nullCurrentNextStep_returnsTrue() {
        assertTrue(isNextStepDifferent(null, "A step."));
    }

    @Test
    void auditStampNextStep_noChange_returnsFalse() {
        String nextStep = "Complete intake";
        assertFalse(isNextStepDifferent(nextStep, nextStep));
    }

    @Test
    void auditStampNextStep_sameValueDiffAuditStamp_returnsFalse() {
        String currentNextStep = "01Mar24| Complete intake --john";
        String requestedNextStep = "03Mar24| Complete intake --sam";
        assertFalse(isNextStepDifferent(currentNextStep, requestedNextStep));
    }

    @Test
    void auditStampNextStep_diffValueSameAuditStamp_returnsTrue() {
        String currentNextStep = "01Mar24| Complete intake --john";
        String requestedNextStep = "01Mar24| Do nothing --john";
        assertTrue(isNextStepDifferent(currentNextStep, requestedNextStep));
    }

    @Test
    @DisplayName("isNextStepInfoChanged returns false when requested next step due date is null")
    void isNextStepInfoChangedReturnsFalseWhenNextStepDueDateIsNull() {
        // Next Step unchanged; Next Step Due Date changed, but it's null:
        requestedNextStepWithDueDate =
            new NextStepWithDueDate(currentNextStepWithDueDate.nextStep(), null);

        boolean result = isNextStepInfoChanged(requestedNextStepWithDueDate, currentNextStepWithDueDate);
        assertFalse(result);
    }

    @Test
    @DisplayName("isNextStepInfoChanged returns false when request values are unchanged")
    void isNextStepInfoChangedReturnsFalseWhenRequestValuesAreUnchanged() {
        // Create new object because it doesn't need to be same in memory, just unchanged values:
        requestedNextStepWithDueDate = new NextStepWithDueDate(
            currentNextStepWithDueDate.nextStep(),
            currentNextStepWithDueDate.dueDate()
        );

        boolean result = isNextStepInfoChanged(requestedNextStepWithDueDate, currentNextStepWithDueDate);
        assertFalse(result);
    }

    @Test
    @DisplayName("isNextStepInfoChanged returns true when requested Next Step changed")
    void isNextStepInfoChangedReturnsTrueWhenRequestedNextStepChanged() {
        requestedNextStepWithDueDate = new NextStepWithDueDate(
            "07May25| New value --SystemAdmin",
            currentNextStepWithDueDate.dueDate());

        boolean result = isNextStepInfoChanged(requestedNextStepWithDueDate, currentNextStepWithDueDate);
        assertTrue(result);
    }

    @Test
    @DisplayName("isNextStepInfoChanged returns true when requested Next Step Due Date changed")
    void isNextStepInfoChangedReturnsTrueWhenRequestedNextStepDueDateChanged() {
        requestedNextStepWithDueDate = new NextStepWithDueDate(
            currentNextStepWithDueDate.nextStep(),
            LocalDate.of(2025, 1, 1)
        );

        boolean result = isNextStepInfoChanged(requestedNextStepWithDueDate, currentNextStepWithDueDate);
        assertTrue(result);
    }

}
