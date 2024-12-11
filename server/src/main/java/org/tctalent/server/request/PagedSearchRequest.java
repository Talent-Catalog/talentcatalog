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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.lang.Nullable;
import org.tctalent.server.api.admin.DtoType;

/**
 * Request that may include paging and sorting fields.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PagedSearchRequest {

    /**
     * If non-null Specifies the type of DTO data to be returned for each search result.
     */
    @Nullable
    private DtoType dtoType;
    private Integer pageSize;
    private Integer pageNumber;
    private Sort.Direction sortDirection;
    private String[] sortFields;

    public PagedSearchRequest(Sort.Direction sortDirection, String[] sortFields) {
        this.sortDirection = sortDirection;
        this.sortFields = sortFields;
    }

    public PageRequest getPageRequest() {
        return PageRequest.of(
                pageNumber != null ? pageNumber : 0,
                pageSize != null ? pageSize : 25,
                getSort());
    }

    public PageRequest getPageRequestWithoutSort() {
        return PageRequest.of(
                pageNumber != null ? pageNumber : 0,
                pageSize != null ? pageSize : 25);

    }

    public Sort getSort() {
        Sort sort = Sort.unsorted();
        if (sortFields != null) {
            sort = Sort.by(sortDirection == null ? Direction.ASC : sortDirection, sortFields);
        }
        return sort;
    }

    /**
     * If dtoType is null, this method will default to returning {@link DtoType#FULL},.
     */
    public DtoType getDtoType() {
        return dtoType == null
            ? DtoType.FULL
            : dtoType;
    }
}
