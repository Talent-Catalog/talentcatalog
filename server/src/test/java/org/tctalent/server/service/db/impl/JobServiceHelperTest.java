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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JobServiceHelperTest {

    String baseJobName;
    JobServiceHelper helper;

    @BeforeEach
    void setUp() {
       baseJobName = "Base Job Name";
       helper = new JobServiceHelper();
    }

    @Test
    void firstEvergreenChildName() {
        String childName = helper.generateNextEvergreenJobName(baseJobName);
        assertEquals(baseJobName + "-R1", childName);
    }

    @Test
    void nextEvergreenChildName() {
        int revision = 1;
        String jobName = baseJobName + "-R" + revision;
        String childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(baseJobName + "-R" + (revision+1), childName);

        revision = 100;
        jobName = baseJobName + "-R" + revision;
        childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(baseJobName + "-R" + (revision+1), childName);
    }

    @Test
    void ignoreInvalidRevisions() {
        String jobName = baseJobName + "-Rx";
        String childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(jobName + "-R1", childName);

        jobName = "-R9 " + baseJobName;
        childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(jobName + "-R1", childName);

        baseJobName = "Fred -R10-R9";
        int revision = 3;
        jobName = baseJobName + "-R" + revision;
        childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(baseJobName + "-R" + (revision+1), childName);
    }
}
