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

package org.tctalent.server.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;
import org.tctalent.server.request.helplink.UpdateHelpLinkRequest;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.HelpLinkService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/help-link")
@RequiredArgsConstructor
public class HelpLinkAdminApi implements
        ITableApi<SearchHelpLinkRequest, UpdateHelpLinkRequest, UpdateHelpLinkRequest> {

    private final CountryService countryService;
    private final HelpLinkService helpLinkService;

    @Override
    @NotNull
    public Map<String, Object> create(UpdateHelpLinkRequest request) throws EntityExistsException {
        HelpLink helpLink = helpLinkService.createHelpLink(request);
        return helpLinkDto().build(helpLink);
    }

    @Override
    public boolean delete(long id) throws EntityReferencedException, InvalidRequestException {
        return helpLinkService.deleteHelpLink(id);
    }

    @PostMapping("fetch")
    @NotNull List<Map<String, Object>> fetch(@Valid @RequestBody SearchHelpLinkRequest request) {
        List<HelpLink> helpLinks = helpLinkService.fetchHelp(request);
        return helpLinkDto().buildList(helpLinks);
    }

    @Override
    @NotNull
    public List<Map<String, Object>> search(SearchHelpLinkRequest request) {
        List<HelpLink> helpLinks = helpLinkService.search(request);
        return helpLinkDto().buildList(helpLinks);
    }

    @Override
    @NotNull
    public Map<String, Object> searchPaged(SearchHelpLinkRequest request) {
        Page<HelpLink> helpLinks = helpLinkService.searchPaged(request);
        return helpLinkDto().buildPage(helpLinks);
    }

    @Override
    @NotNull
    public Map<String, Object> update(long id, UpdateHelpLinkRequest request)
        throws EntityExistsException, InvalidRequestException, NoSuchObjectException {
        HelpLink helpLink = helpLinkService.updateHelpLink(id, request);
        return helpLinkDto().build(helpLink);
    }

    private DtoBuilder helpLinkDto() {
        return new DtoBuilder()
            .add("id")
            .add("caseStage")
            .add("focus")
            .add("country", countryService.selectBuilder())
            .add("jobStage")
            .add("label")
            .add("link")
            .add("nextStepInfo", nextStepInfoDto())
            ;
    }

    private DtoBuilder nextStepInfoDto() {
        return new DtoBuilder()
            .add("nextStepDays")
            .add("nextStepName")
            .add("nextStepText")
            ;
    }

}
