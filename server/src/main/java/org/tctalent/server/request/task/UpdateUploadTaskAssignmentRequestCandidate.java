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

package org.tctalent.server.request.task;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.tctalent.server.api.portal.TaskAssignmentPortalApi;

/**
 * Request to update an Upload TaskAssignment received through the candidate portal
 * <p/>
 * The main difference from other Update TaskAssignmentRequests is that "complete" is not set here.
 * When a user requests an upload they don't know whether it will succeed or not. If it does
 * not succeed, it should not be marked as complete.
 *
 * For the above reason, there is a special completeUploadTask server call to set the complete
 * status. See {@link TaskAssignmentPortalApi#completeUploadTask}
 *
 * @author John Cameron
 */
@Getter
@Setter
public class UpdateUploadTaskAssignmentRequestCandidate {

    /**
     * If task is set as abandoned
     */
    boolean abandoned;

    /**
     * If task has some notes provided by the candidate
     */
    @Nullable
    String candidateNotes;
}
