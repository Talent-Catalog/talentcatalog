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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.service.db.AgreementService;
import org.tctalent.server.service.db.TermsInfoService;
import org.tctalent.server.util.dto.DtoBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * API for candidate agreement management.
 *
 * @author sadatmalik
 */
@RestController
@RequestMapping("/api/portal/agreement")
public class AgreementPortalApi {

    private final AgreementService agreementService;
    private final TermsInfoService termsInfoService;
    private final TemplateEngine termsTemplateEngine;

    /**
     * Note - we can't use Lombok RequiredArgsConstructor because currently Lombok doesn't copy
     * the @Qualifier annotation to the constructor.
     */
    public AgreementPortalApi(
        AgreementService agreementService,
        TermsInfoService termsInfoService,
        @Qualifier("termsTemplateEngine") TemplateEngine termsTemplateEngine) {
        this.agreementService = agreementService;
        this.termsInfoService = termsInfoService;
        this.termsTemplateEngine = termsTemplateEngine;
    }

    @GetMapping("list")
    public List<Map<String, Object>> listMyAgreements() {
        List<Map<String, Object>> agreementDtos = agreementDto().buildList(agreementService.listMyAgreements());
        for (Map<String, Object> agreementDto : agreementDtos) {
            String termsInfoId = (String) agreementDto.get("termsInfoId");
            TermsInfo termsInfo = termsInfoService.get(termsInfoId);
            Map<String, Object> termsInfoDto = termsInfoDto().build(termsInfo);

            // Render template variables into terms content before returning to the candidate portal.
            Map<String, Object> counterpartyDto = (Map<String, Object>) agreementDto.get("counterparty");
            String companyName = (String) counterpartyDto.get("displayName");
            termsInfoDto.put("content", renderTermsContent(termsInfo, companyName));

            agreementDto.put("termsInfo", termsInfoDto);
        }
        return agreementDtos;
    }

    private String renderTermsContent(TermsInfo termsInfo, String companyName) {
        Context context = new Context();
        context.setVariable("companyName", companyName);
        return termsTemplateEngine.process(termsInfo.getContent(), context);
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
