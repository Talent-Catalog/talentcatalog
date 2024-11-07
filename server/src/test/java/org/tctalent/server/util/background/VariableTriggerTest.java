/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.util.background;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.SimpleTriggerContext;

class VariableTriggerTest {
    SimpleTriggerContext context;

    @BeforeEach
    void setUp() {
        context = new SimpleTriggerContext();
        context.update(new Date(0), new Date(0), new Date(1000));
    }

    @Test
    void nextExecutionTime() {
        long startTime = 0;
        long completionTime = 1000;
       context.update(new Date(0), new Date(startTime), new Date(completionTime));

       VariableTrigger trigger = new VariableTrigger(50);
       Date time = trigger.nextExecutionTime(context);
       assertNotNull(time);
       assertEquals(completionTime + 1000, time.getTime());

       trigger = new VariableTrigger(75);
       time = trigger.nextExecutionTime(context);
       assertNotNull(time);
       assertEquals(completionTime + 333, time.getTime());

       trigger = new VariableTrigger(25);
       time = trigger.nextExecutionTime(context);
       assertNotNull(time);
       assertEquals(completionTime + 3000, time.getTime());
    }

    @Test
    void testNullTimes() {
        VariableTrigger trigger = new VariableTrigger(50);
        Date time = trigger.nextExecutionTime(context);
        assertNotNull(time);
    }

    @Test
    void testBadPercentage() {
        try {
            VariableTrigger trigger = new VariableTrigger(0);
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals(ex.getClass(), IllegalArgumentException.class);
        }

        try {
            VariableTrigger trigger = new VariableTrigger(101);
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals(ex.getClass(), IllegalArgumentException.class);
        }

    }
}
