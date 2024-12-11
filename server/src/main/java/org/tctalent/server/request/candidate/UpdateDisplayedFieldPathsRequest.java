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

import java.util.List;

import org.springframework.lang.Nullable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateDisplayedFieldPathsRequest {
    /**
     * Field paths of candidate fields to be displayed in long format candidate
     * results.
     * <p/>
     * Empty list indicates that default fields should be displayed.
     */
    @Nullable
    private List<String> displayedFieldsLong;

    /**
     * Field paths of candidate fields to be displayed in short format candidate
     * results.
     * <p/>
     * Empty list indicates that default fields should be displayed.
     */
    @Nullable
    private List<String> displayedFieldsShort;
}
