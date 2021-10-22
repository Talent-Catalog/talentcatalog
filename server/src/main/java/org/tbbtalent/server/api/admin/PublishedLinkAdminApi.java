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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.service.db.SavedListService;

import java.net.URI;

@RestController()
public class PublishedLinkAdminApi {

    private final SavedListService savedListService;

    @Autowired
    public PublishedLinkAdminApi(SavedListService savedListService) {
        this.savedListService = savedListService;
    }

    @GetMapping("/published/{short-name}")
    public ResponseEntity<Void> redirect(@PathVariable("short-name") String shortName){
        SavedList list = this.savedListService.findByShortName(shortName);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(list.getPublishedDocLink())).build();
    }
}
