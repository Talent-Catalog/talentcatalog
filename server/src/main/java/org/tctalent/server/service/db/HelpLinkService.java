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

package org.tctalent.server.service.db;

import java.util.List;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;
import org.tctalent.server.request.helplink.UpdateHelpLinkRequest;

public interface HelpLinkService {


    @NotNull
    HelpLink createHelpLink(UpdateHelpLinkRequest request) throws NoSuchObjectException;

    /**
     * Deletes help link with given id if found
     * @param id Id of help link
     * @return true if found and deleted. False if not found
     */
    boolean deleteHelpLink(long id);

    /**
     * Fetches help links relevant to the given request.
     * <p/>
     * This is essentially the same as {@link #search(SearchHelpLinkRequest)} except that it is
     * intended to retrieve help links that the user might need to help them whatever they are doing.
     * By contrast search and search paged are only intended for use in managing help links
     * from the TC Settings screen and are intended to only search by keyword.
     *
     * @param request Request providing context related to the type of help being requested.
     * @return Matching help links - empty if none found.
     */
    @NonNull
    List<HelpLink> fetchHelp(SearchHelpLinkRequest request);

    /**
     * @see #fetchHelp(SearchHelpLinkRequest)
     */
    @NonNull
    List<HelpLink> search(SearchHelpLinkRequest request);

    /**
     * @see #fetchHelp(SearchHelpLinkRequest)
     */
    @NonNull
    Page<HelpLink> searchPaged(SearchHelpLinkRequest request);

    @NotNull
    HelpLink updateHelpLink(long id, UpdateHelpLinkRequest request) throws NoSuchObjectException;
}
