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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.model.db.Agreement;
import org.tctalent.server.model.db.Counterparty;
import org.tctalent.server.model.db.CounterpartyType;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsType;
import org.tctalent.server.service.db.AgreementService;
import org.tctalent.server.service.db.TermsInfoService;

class AgreementPortalApiTest {

    @Mock
    private AgreementService agreementService;

    @Mock
    private TermsInfoService termsInfoService;

    @InjectMocks
    private AgreementPortalApi agreementPortalApi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("listMyAgreements returns agreement DTOs with counterparty and terms info")
    void listMyAgreements_returnsAgreementDtosWithCounterpartyAndTermsInfo() {
        Counterparty counterparty = new Counterparty();
        counterparty.setId(10L);
        counterparty.setType(CounterpartyType.DATABASE_PROVIDER);
        counterparty.setName("OPC");

        Agreement agreement = new Agreement();
        agreement.setId(20L);
        agreement.setCounterparty(counterparty);
        agreement.setTermsInfoId("OpcDataProcessingAgreementV1");
        agreement.setStart(OffsetDateTime.now().minusDays(1));

        TermsInfo termsInfo = new TermsInfo(
            "OpcDataProcessingAgreementV1",
            "/terms/OpcDataProcessingAgreement-20250831.html",
            TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT,
            LocalDate.of(2025, Month.AUGUST, 31));
        termsInfo.setContent("<h1>OPC Terms</h1>");

        when(agreementService.listMyAgreements()).thenReturn(List.of(agreement));
        when(termsInfoService.get("OpcDataProcessingAgreementV1")).thenReturn(termsInfo);

        List<Map<String, Object>> result = agreementPortalApi.listMyAgreements();

        assertNotNull(result);
        assertEquals(1, result.size());

        Map<String, Object> agreementDto = result.get(0);
        assertEquals(20L, agreementDto.get("id"));
        assertEquals("OpcDataProcessingAgreementV1", agreementDto.get("termsInfoId"));

        Map<String, Object> counterpartyDto = (Map<String, Object>) agreementDto.get("counterparty");
        assertEquals(10L, counterpartyDto.get("id"));
        assertEquals(CounterpartyType.DATABASE_PROVIDER, counterpartyDto.get("type"));
        assertEquals("OPC", counterpartyDto.get("displayName"));

        Map<String, Object> termsInfoDto = (Map<String, Object>) agreementDto.get("termsInfo");
        assertEquals("OpcDataProcessingAgreementV1", termsInfoDto.get("id"));
        assertEquals(TermsType.OPC_STANDARD_DATA_PROCESSING_AGREEMENT, termsInfoDto.get("type"));
        assertEquals("/terms/OpcDataProcessingAgreement-20250831.html", termsInfoDto.get("pathToContent"));
        assertEquals("<h1>OPC Terms</h1>", termsInfoDto.get("content"));

        verify(agreementService).listMyAgreements();
        verify(termsInfoService).get("OpcDataProcessingAgreementV1");
    }
}
