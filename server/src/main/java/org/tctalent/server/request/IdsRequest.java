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

package org.tctalent.server.request;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tctalent.server.api.dto.DtoType;

/**
 * Fetching data by ids
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class IdsRequest {
    /**
     * If non-null Specifies the type of DTO data to be returned for each search result.
     */
    @Nullable
    private DtoType dtoType;

    /**
     * Ids of data requested
     */
    Set<Long> ids;
}
