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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.request.work.experience.SearchJobExperienceRequest;
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.PageContext;
import org.tctalent.server.util.background.PageProcessReturn;

/**
 * Processes candidate job experiences a page at a time
 *
 * @author John Cameron
 */
@Slf4j
@RequiredArgsConstructor
public abstract class PagedCandidateJobExperienceBackProcessor implements BackProcessor<PageContext> {

    private final String action;
    private final SearchJobExperienceRequest searchJobExperienceRequest;

    @Override
    public boolean process(PageContext ctx) {
        int page = ctx.getLastProcessedPage() == null ? 0 : ctx.getLastProcessedPage() + 1;

        searchJobExperienceRequest.setPageNumber(page);

        boolean hasMorePages;
        try {
            PageProcessReturn pageProcessReturn =
                processPageOfExperiences(searchJobExperienceRequest);
            hasMorePages = pageProcessReturn.isMorePages();

            ctx.setLastProcessedPage(page);

            // Log completed page
            LogBuilder.builder(log)
                .action(action)
                .message("Processed " + pageProcessReturn.getPageSize() + " items in page " + page + " of " + (
                    pageProcessReturn.getTotalPages() - 1))
                .logInfo();
        } catch (Exception e) {
            // If an exception occurs, we log the error and stop processing further pages
            hasMorePages = false;
            LogBuilder.builder(log)
                .action(action)
                .message("Error processing page " + page)
                .logError(e);
        }

        return !hasMorePages;
    }

    abstract protected PageProcessReturn processPageOfExperiences(
        SearchJobExperienceRequest searchJobExperienceRequest
    );
}
