/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import org.springframework.data.domain.Page;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;
import org.tctalent.server.request.helplink.UpdateHelpLinkRequest;

public interface HelpLinkService {


    HelpLink createHelpLink(UpdateHelpLinkRequest request) throws NoSuchObjectException;

    /**
     * Deletes help link with given id if found
     * @param id Id of help link
     * @return true if found and deleted. False if not found
     */
    boolean deleteHelpLink(long id);

    List<HelpLink> search(SearchHelpLinkRequest request);

    Page<HelpLink> searchPaged(SearchHelpLinkRequest request);

    HelpLink updateHelpLink(long id, UpdateHelpLinkRequest request) throws NoSuchObjectException;
}
