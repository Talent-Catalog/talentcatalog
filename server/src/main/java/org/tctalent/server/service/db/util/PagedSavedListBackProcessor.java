// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.service.db.util;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.PageContext;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Slf4j
@RequiredArgsConstructor
public abstract class PagedSavedListBackProcessor implements BackProcessor<PageContext> {
    private final String action;
    private final SearchSavedListRequest searchSavedListRequest;
    private final SavedListService savedListService;

    @Override
    public boolean process(PageContext ctx) {
        int page = ctx.getLastProcessedPage() == null ? 0 : ctx.getLastProcessedPage() + 1;

        searchSavedListRequest.setPageNumber(page);
        Page<SavedList> pageOfSavedLists = null;

        try {
            pageOfSavedLists = savedListService.searchPaged(searchSavedListRequest);

            processSavedLists(savedListService, pageOfSavedLists.getContent());

            // Log completed page
            LogBuilder.builder(log)
                .action(action)
                .message("Processed page " + page + " of " + (pageOfSavedLists.getTotalPages()-1))
                .logInfo();
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action(action)
                .message("Error processing page " + page)
                .logError(e);
        }

        ctx.setLastProcessedPage(page);

        return pageOfSavedLists == null || !pageOfSavedLists.hasNext();
    }

    abstract protected void processSavedLists(
        SavedListService savedListService, List<SavedList> savedLists);
}
