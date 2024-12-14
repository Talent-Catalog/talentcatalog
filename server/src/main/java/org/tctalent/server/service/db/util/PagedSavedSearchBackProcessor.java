// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

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
 * TODO JC Doc
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

            processSavedSearches(savedSearchService, pageOfSavedSearches.getContent());

            // Log completed page
            LogBuilder.builder(log)
                .action(action)
                .message("Processed page " + page + " of " + (pageOfSavedSearches.getTotalPages()-1))
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
