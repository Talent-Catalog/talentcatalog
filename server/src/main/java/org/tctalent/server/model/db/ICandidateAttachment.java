/*
 * Copyright (c) 2026 Talent Catalog.
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

import org.tctalent.server.files.StoredFile;

/**
 * Adds additional Candidate Attachment fields over StoredFile.
 *
 * @author John Cameron
 */
public interface ICandidateAttachment extends StoredFile {

    /**
     * Candidate associated with attachment.
     */
    Candidate getCandidate();
    
    /**
     * Public ID of the attachment.
     */
    String getPublicId();
}
