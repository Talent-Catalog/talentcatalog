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

import org.springframework.stereotype.Service;

/**
 * Helper for Job Services
 *
 * @author John Cameron
 */
@Service
public class JobServiceHelper {
    final private static String REVISION_PREFIX = "-R";
    public String generateNextEvergreenJobName(String currentJobName) {
        String name;
        int revisionNumber = extractRevisionNumber(currentJobName);
        if (revisionNumber != 0) {
            name = stripRevision(currentJobName) + REVISION_PREFIX + (revisionNumber+1);
        } else {
            name = currentJobName + REVISION_PREFIX + "1";
        }
       return name;
    }

    private int extractRevisionNumber(String currentJobName) {
        int startRevision = findRevisionStart(currentJobName);
        int revisionNumber;
        if (startRevision < 0) {
            revisionNumber = 0;
        } else {
            try {
                String revision = currentJobName.substring(
                    startRevision + REVISION_PREFIX.length());
                revisionNumber = Integer.parseInt(revision);
            } catch (Exception ex) {
                revisionNumber = 0;
            }
        }
        return revisionNumber;
    }

    private String stripRevision(String currentJobName) {
        String stripped = currentJobName;
        int startRevision = findRevisionStart(currentJobName);
        if (startRevision >= 0) {
            stripped = currentJobName.substring(0, startRevision);
        }
        return stripped;
    }

    private int findRevisionStart(String jobName) {
        int startRevision = jobName.lastIndexOf(REVISION_PREFIX);
        if (startRevision >= 0) {
            String revision = jobName.substring(startRevision);
            int revisionNumber;
            try {
                revisionNumber = Integer.parseInt(
                    revision.substring(REVISION_PREFIX.length()));
            } catch (Exception ex) {
                revisionNumber = 0;
            }
            if (revisionNumber == 0) {
                startRevision = -1;
            }
        }
        return startRevision;
    }
}
