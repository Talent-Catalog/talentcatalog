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

package org.tctalent.server.service.db.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NotImplementedException;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.repository.db.HelpLinkRepository;
import org.tctalent.server.request.helplink.UpdateHelpLinkRequest;
import org.tctalent.server.service.db.HelpLinkService;

@Service
@RequiredArgsConstructor
public class HelpLinkServiceImpl implements HelpLinkService {
    private final HelpLinkRepository helpLinkRepository;

    @Override
    public HelpLink createHelpLink(UpdateHelpLinkRequest request) {
        //TODO JC createHelpLink not implemented in HelpLinkServiceImpl
        throw new NotImplementedException("HelpLinkServiceImpl", "createHelpLink");
    }
}
