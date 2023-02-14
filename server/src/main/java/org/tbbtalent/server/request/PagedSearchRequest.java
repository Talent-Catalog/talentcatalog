/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Request that includes paging and sorting fields.
 */
@Getter @Setter @ToString
public class PagedSearchRequest {
    private Integer pageSize;
    private Integer pageNumber;
    private Sort.Direction sortDirection;
    private String[] sortFields;

    public PagedSearchRequest() {
    }

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

}
