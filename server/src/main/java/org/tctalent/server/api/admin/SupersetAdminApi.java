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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.service.db.SupersetService;

@RestController
@RequestMapping("/api/admin/superset")
@RequiredArgsConstructor
@Slf4j
public class SupersetAdminApi {

  private final SupersetService supersetService;

  @GetMapping(value="{savedSearchId}/run-stats", produces = MediaType.TEXT_PLAIN_VALUE)
  public String runStats(@PathVariable("savedSearchId") long savedSearchId) {
    String dbName = supersetService.runStats(savedSearchId);
    return dbName;
  }

}
