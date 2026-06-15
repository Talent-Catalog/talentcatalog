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
package org.tctalent.server.service.db.util;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.request.search.SearchSavedSearchRequest;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.PageContext;

/**
 * Processes saved searches a page at a time
 *
 * @author John Cameron
 */
@Slf4j
@RequiredArgsConstructor
public abstract class PagedSavedSearchBackProcessor implements BackProcessor<PageContext> {
    private final String action;
    private final SearchSavedSearchRequest searchSavedSearchRequest;
    private final SavedSearchService savedSearchService;

    @Override
    public boolean process(PageContext ctx) {
        int page = ctx.getLastProcessedPage() == null ? 0 : ctx.getLastProcessedPage() + 1;

        searchSavedSearchRequest.setPageNumber(page);
        Page<SavedSearch> pageOfSavedSearches = null;

        try {
            pageOfSavedSearches = savedSearchService.searchPaged(searchSavedSearchRequest);

            final List<SavedSearch> content = pageOfSavedSearches.getContent();
            processSavedSearches(savedSearchService, content);

            // Log completed page
            LogBuilder.builder(log)
                .action(action)
                .message("Processed " + content.size() + " items in page " + page + " of " + (pageOfSavedSearches.getTotalPages()-1))
                .logInfo();
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action(action)
                .message("Error processing page " + page)
                .logError(e);
        }

        ctx.setLastProcessedPage(page);

        return pageOfSavedSearches == null || !pageOfSavedSearches.hasNext();
    }

    abstract protected void processSavedSearches(
        SavedSearchService savedSearchService, List<SavedSearch> savedSearches);
}
