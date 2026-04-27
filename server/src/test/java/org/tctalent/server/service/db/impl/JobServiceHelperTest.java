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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        //Original name has no revision on it
        String childName = helper.generateNextEvergreenJobName(baseJobName);
        assertEquals(baseJobName + "-R1", childName);
    }

    @Test
    void nextEvergreenChildName() {
        int revision = 1;
        String jobName = baseJobName + "-R" + revision;
        String childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(baseJobName + "-R" + (revision+1), childName);

        //Test multi digit revisions
        revision = 100;
        jobName = baseJobName + "-R" + revision;
        childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(baseJobName + "-R" + (revision+1), childName);
    }

    @Test
    void ignoreInvalidRevisions() {
        //Ignore non numeric revisions
        String jobName = baseJobName + "-Rx";
        String childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(jobName + "-R1", childName);

        //Revision number can't be 0
        jobName = baseJobName + "-R0";
        childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(jobName + "-R1", childName);

        //Revision number must be > 0
        jobName = baseJobName + "-R-1";
        childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(jobName + "-R1", childName);

        //Ignore valid revision at start of name
        jobName = "-R9 " + baseJobName;
        childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(jobName + "-R1", childName);

        //Ignore valid revisions in the middle of the name
        baseJobName = "Fred -R10-R9";
        int revision = 3;
        jobName = baseJobName + "-R" + revision;
        childName = helper.generateNextEvergreenJobName(jobName);
        assertEquals(baseJobName + "-R" + (revision+1), childName);


    }
}
