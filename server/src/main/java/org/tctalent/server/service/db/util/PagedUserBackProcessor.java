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
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.user.SearchUserRequest;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.PageContext;

/**
 * Processes users a page at a time
 *
 * @author John Cameron
 */
@Slf4j
@RequiredArgsConstructor
public abstract class PagedUserBackProcessor implements BackProcessor<PageContext> {
    private final String action;
    private final SearchUserRequest searchUserRequest;
    private final UserService userService;

    @Override
    public boolean process(PageContext ctx) {
        int page = ctx.getLastProcessedPage() == null ? 0 : ctx.getLastProcessedPage() + 1;

        searchUserRequest.setPageNumber(page);
        Page<User> pageOfUsers = null;

        try {
            pageOfUsers = userService.searchPaged(searchUserRequest);

            final List<User> content = pageOfUsers.getContent();
            processUsers(userService, content);

            // Log completed page
            LogBuilder.builder(log)
                .action(action)
                .message("Processed " + content.size() + " items in page " + page + " of " +
                    (pageOfUsers.getTotalPages()-1))
                .logInfo();
        } catch (Exception e) {
            LogBuilder.builder(log)
                .action(action)
                .message("Error processing page " + page)
                .logError(e);
        }

        ctx.setLastProcessedPage(page);

        return pageOfUsers == null || !pageOfUsers.hasNext();
    }

    abstract protected void processUsers(
        UserService userService, List<User> users);
}
