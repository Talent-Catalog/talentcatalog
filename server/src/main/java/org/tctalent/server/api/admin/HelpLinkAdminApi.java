/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.api.admin;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;
import org.tctalent.server.request.helplink.UpdateHelpLinkRequest;
import org.tctalent.server.service.db.HelpLinkService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/help-link")
@RequiredArgsConstructor
public class HelpLinkAdminApi implements
        ITableApi<SearchHelpLinkRequest, UpdateHelpLinkRequest, UpdateHelpLinkRequest> {

    private final HelpLinkService helpLinkService;

    @Override
    public Map<String, Object> create(UpdateHelpLinkRequest request) throws EntityExistsException {
        HelpLink helpLink = helpLinkService.createHelpLink(request);
        return helpLinkDto().build(helpLink);
    }

    private DtoBuilder helpLinkDto() {
        return new DtoBuilder()
            ;
    }
}
