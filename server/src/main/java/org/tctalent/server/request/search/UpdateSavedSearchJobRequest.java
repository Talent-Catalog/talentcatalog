/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.request.search;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * Request to explicitly assign or clear the Talent Catalog job associated with a saved search.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class UpdateSavedSearchJobRequest {

    /**
     * If non null, this is the id of the job to be associated with the saved search.
     * If null, the saved search is no longer associated with any job.
     */
    @Nullable
    private Long jobId;
}
