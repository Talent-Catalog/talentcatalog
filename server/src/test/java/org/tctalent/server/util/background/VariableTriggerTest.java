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

package org.tctalent.server.util.background;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.SimpleTriggerContext;

class VariableTriggerTest {

    SimpleTriggerContext context;

    @BeforeEach
    void setUp() {
        context = new SimpleTriggerContext();
        context.update(
            Instant.ofEpochMilli(0), Instant.ofEpochMilli(0), Instant.ofEpochMilli(1000));
    }

    @Test
    void nextExecutionTime() {
        Instant startTime = Instant.ofEpochMilli(0);
        Duration processingTime = Duration.ofMillis(3000);
        Instant completionTime = startTime.plus(processingTime);
        context.update(startTime, startTime, completionTime);

        //If it takes 3 seconds to process and we want that to be 50% of total CPU time
        //Need a delay of 3 seconds. 3 is 50% of 6 (3+3)
        VariableTrigger trigger = new VariableTrigger(50);
        Instant time = trigger.nextExecution(context);
        assertNotNull(time);
        assertEquals(completionTime.plus(Duration.ofMillis(3000)), time);

        //If it takes 3 seconds to process and we want that to be 75% of total CPU time
        //Need a delay of 1 seconds. 3 is 75% of 4 (3+1)
        trigger = new VariableTrigger(75);
        time = trigger.nextExecution(context);
        assertNotNull(time);
        assertEquals(completionTime.plus(Duration.ofMillis(1000)), time);

        //If it takes 3 seconds to process and we want that to be 25% of total CPU time
        //Need a delay of 9 seconds. 3 is 25% of 12 (3+9)
        trigger = new VariableTrigger(25);
        time = trigger.nextExecution(context);
        assertNotNull(time);
        assertEquals(completionTime.plus(Duration.ofMillis(9000)), time);
    }

    @Test
    void testNullTimes() {
        VariableTrigger trigger = new VariableTrigger(50);
        Instant time = trigger.nextExecution(context);
        assertNotNull(time);
    }

    @Test
    void testBadPercentage() {
        try {
            new VariableTrigger(0);
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals(ex.getClass(), IllegalArgumentException.class);
        }

        try {
            new VariableTrigger(101);
            fail("Expected exception");
        } catch (Exception ex) {
            assertEquals(ex.getClass(), IllegalArgumentException.class);
        }
    }
}
