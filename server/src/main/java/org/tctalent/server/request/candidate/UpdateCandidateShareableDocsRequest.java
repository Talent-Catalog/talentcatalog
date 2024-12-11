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

package org.tctalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * Request to mark candidate attachments as shareable.
 */
@Getter
@Setter
@ToString
public class UpdateCandidateShareableDocsRequest {

    /**
     * Shareable CV attachment (if not null).
     * Replaces any previously shared attachment.
     */
    @Nullable
    private Long shareableCvAttachmentId;

    /**
     * Shareable non CV attachment (if not null).
     * Replaces any previously shared attachment.
     */
    @Nullable
    private Long shareableDocAttachmentId;

    /**
     * If null, the above shareable attachments are just associated with a candidate.
     * If not null, the shareable attachments are associated with a candidate and the given list
     * - ie these are "context" specific.
     */
    @Nullable
    private Long savedListId;
}
