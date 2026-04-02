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
package org.tctalent.server.files;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.CandidateAttachment;

/**
 * Authorization service for candidate attachments based on currently logged-in user.
 *
 * @author John Cameron
 */
@Service
public class AttachmentAuthorizationService {

    /**
     * Checks that the current user has permission to access the given attachment.
     *
     * @param attachment the attachment to check
     * @throws AccessDeniedException if the current user does not have permission to access the attachment
     */
    public void assertCurrentUserCanAccess(CandidateAttachment attachment) {
        // Currently, attachments are accessible if people have a public link to them and this
        // is only called when processing public links to attachments.
        // Accessibility for logged-in users is handled in CandidateAttachmentsServiceImpl.   
    }
}
