// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.service.db.util;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.request.partner.SearchPartnerRequest;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.PageContext;

/**
 * Processes partners a page at a time.
 *
 * @author John Cameron
 */
@Slf4j
@RequiredArgsConstructor
public abstract class PagedPartnerBackProcessor implements BackProcessor<PageContext> {
    private final String action;
    private final SearchPartnerRequest searchPartnerRequest;
    private final PartnerService partnerService;

    @Override
    public boolean process(PageContext ctx) {
        int page = ctx.getLastProcessedPage() == null ? 0 : ctx.getLastProcessedPage() + 1;

        searchPartnerRequest.setPageNumber(page);
        Page<PartnerImpl> pageOfPartners = null;

        try {
            pageOfPartners = partnerService.searchPaged(searchPartnerRequest);

            final List<PartnerImpl> content = pageOfPartners.getContent();
            processPartners(partnerService, content);

            // Log completed page
            LogBuilder.builder(log)
                .action(action)
                .message("Processed " + content.size() + " items in page " + page + " of " + (pageOfPartners.getTotalPages()-1))
                .logInfo();
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action(action)
                .message("Error processing page " + page)
                .logError(e);
        }

        ctx.setLastProcessedPage(page);

        return pageOfPartners == null || !pageOfPartners.hasNext();
    }

    abstract protected void processPartners(
        PartnerService partnerService, List<PartnerImpl> partners);
}
