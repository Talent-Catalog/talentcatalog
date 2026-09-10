/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.background;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

/**
 * Provides return information for processing paged data, including the page size and total number of pages.
 *
 * @author John Cameron
 */
@Getter
@Setter
@NoArgsConstructor
public class PageProcessReturn {
    boolean morePages;
    int pageSize;
    int totalPages;

    public PageProcessReturn(Page<?> page) {
        this.morePages = page.hasNext();
        this.totalPages = page.getTotalPages();
        this.pageSize = page.getContent().size();
    }
}
