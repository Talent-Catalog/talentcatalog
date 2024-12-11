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

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.service.db.SavedListService;

@RestController
@RequestMapping("/published")
@RequiredArgsConstructor
public class PublishedLinkAdminApi {

    private final SavedListService savedListService;

    @GetMapping("{short-name}")
    public ResponseEntity<Void> redirect(@PathVariable("short-name") String shortName) {
        SavedList list = savedListService.findByShortName(shortName);
        if (list != null && list.getPublishedDocLink() != null) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(list.getPublishedDocLink())).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
