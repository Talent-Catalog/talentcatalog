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

package org.tctalent.server.request.search;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateFromDefaultSavedSearchRequest {
    /**
     * If not zero, the new saved search should take its name from the name of
     * the saved list with this id.
     * <p/>
     * Otherwise the saved search name is given by the "name" attribute.
     */
    private long savedListId;

    /**
     * Only used if savedListId is null. In that case this must not be null and
     * is used to name the new search.
     */
    private String name;

    /**
     * This job (if not null) should be associated with the newly created search.
     */
    private Long jobId;
}
