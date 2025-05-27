// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

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
