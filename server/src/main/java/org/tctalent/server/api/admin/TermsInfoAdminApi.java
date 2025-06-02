
/*
 * Copyright (c) 2025 Talent Catalog.
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsInfoDto;
import org.tctalent.server.model.db.TermsType;
import org.tctalent.server.model.db.mapper.TermsInfoMapper;
import org.tctalent.server.service.db.TermsInfoService;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@RestController
@RequestMapping("/api/admin/terms-info")
@Slf4j
@RequiredArgsConstructor
public class TermsInfoAdminApi {
    private final TermsInfoMapper termsInfoMapper;
    private final TermsInfoService termsInfoService;

    @GetMapping("type/{enum}")
    public ResponseEntity<TermsInfoDto> getCurrentByType(@PathVariable("enum") TermsType termsType) {
        TermsInfo termsInfo = termsInfoService.getCurrentByType(termsType);
        return ResponseEntity.ok(termsInfoMapper.toDto(termsInfo));
    }

}
