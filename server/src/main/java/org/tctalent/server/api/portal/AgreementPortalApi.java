/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.api.portal;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.service.db.AgreementService;
import org.tctalent.server.service.db.TermsInfoService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * API for candidate agreement management.
 *
 * @author sadatmalik
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/portal/agreement")
public class AgreementPortalApi {

    private final AgreementService agreementService;
    private final TermsInfoService termsInfoService;

    @GetMapping("list")
    public List<Map<String, Object>> listMyAgreements() {
        List<Map<String, Object>> agreementDtos = agreementDto().buildList(agreementService.listMyAgreements());
        for (Map<String, Object> agreementDto : agreementDtos) {
            String termsInfoId = (String) agreementDto.get("termsInfoId");
            TermsInfo termsInfo = termsInfoService.get(termsInfoId);
            agreementDto.put("termsInfo", termsInfoDto().build(termsInfo));
        }
        return agreementDtos;
    }

    private DtoBuilder agreementDto() {
        return new DtoBuilder()
            .add("id")
            .add("start")
            .add("end")
            .add("termsInfoId")
            .add("counterparty", counterpartyDto())
            ;
    }

    private DtoBuilder counterpartyDto() {
        return new DtoBuilder()
            .add("id")
            .add("type")
            .add("displayName")
            ;
    }

    private DtoBuilder termsInfoDto() {
        return new DtoBuilder()
            .add("id")
            .add("type")
            .add("pathToContent")
            .add("createdDate")
            .add("content")
            ;
    }
}
