/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.LinkSavedList;
import org.tbbtalent.server.request.link.CreateLinkRequest;
import org.tbbtalent.server.request.link.SearchLinkRequest;
import org.tbbtalent.server.request.link.UpdateLinkRequest;
import org.tbbtalent.server.service.db.LinkSavedListService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/link-saved-list")
public class LinkSavedListAdminApi {
    private final LinkSavedListService linkSavedListService;

    @Autowired
    public LinkSavedListAdminApi(LinkSavedListService linkSavedListService) {
        this.linkSavedListService = linkSavedListService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllLinks() {
        List<LinkSavedList> links = linkSavedListService.listLinks();
        return linkSavedListDto().buildList(links);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchLinkRequest request) {
        Page<LinkSavedList> links = this.linkSavedListService.searchLinks(request);
        return linkSavedListDto().buildPage(links);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        LinkSavedList link = this.linkSavedListService.getLink(id);
        return linkSavedListDto().build(link);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateLinkRequest request) throws EntityExistsException {
        LinkSavedList link = this.linkSavedListService.createLink(request);
        return linkSavedListDto().build(link);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateLinkRequest request) throws EntityExistsException  {

        LinkSavedList link = this.linkSavedListService.updateLink(id, request);
        return linkSavedListDto().build(link);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.linkSavedListService.deleteLink(id);
    }

    private DtoBuilder linkSavedListDto() {
        return new DtoBuilder()
                .add("id")
                .add("savedList", savedListDto())
                .add("link")
                ;
    }

    private DtoBuilder savedListDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }
}
