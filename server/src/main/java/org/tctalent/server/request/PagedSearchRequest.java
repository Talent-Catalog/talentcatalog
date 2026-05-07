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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.lang.Nullable;
import org.tctalent.server.api.dto.DtoType;

/**
 * Request that may include paging and sorting fields.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class PagedSearchRequest {

    /**
     * Maximum allowed page size to protect against excessive memory usage and heavy DB queries.
     */
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * Default page size used when none is provided.
     */
    public static final int DEFAULT_PAGE_SIZE = 25;

    /**
     * If non-null Specifies the type of DTO data to be returned for each search result.
     */
    @Nullable
    private DtoType dtoType;

    @Min(1)
    @Max(MAX_PAGE_SIZE)
    private Integer pageSize;

    @Min(0)
    private Integer pageNumber;
    private Sort.Direction sortDirection;

    /**
     * Note that the TC only supports a single sort field even though this is an array.
     */
    private String[] sortFields;

    public PagedSearchRequest(Sort.Direction sortDirection, String[] sortFields) {
        this.sortDirection = sortDirection;
        this.sortFields = sortFields;
    }

    public PageRequest getPageRequest() {
        int number = pageNumber != null ? Math.max(pageNumber, 0) : 0;
        int size = pageSize != null ? Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        return PageRequest.of(number, size, getSort());
    }

    public PageRequest getPageRequestWithoutSort() {
        int number = pageNumber != null ? Math.max(pageNumber, 0) : 0;
        int size = pageSize != null ? Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        return PageRequest.of(number, size);
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
