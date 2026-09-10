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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.exception.InvalidVerifyPlusPayloadException;
import org.tctalent.server.request.verify.VerifyPlusScanRequest;
import org.tctalent.server.service.db.verify.VerifyPlusIngestResult;
import org.tctalent.server.service.db.verify.VerifyPlusService;

class VerifyPlusPortalApiTest {

    @Mock
    private VerifyPlusService verifyPlusService;

    @InjectMocks
    private VerifyPlusPortalApi verifyPlusPortalApi;

    private VerifyPlusScanRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new VerifyPlusScanRequest();
        request.setRawPayload("{\"v\":\"mock-1\",\"unhcrId\":\"UNHCR-1\"}");
    }

    @Test
    @DisplayName("Given a valid payload, when submitted, then returns a mapped result with UNHCR number and duplicate flag")
    void submit_validPayload_returnsMappedResult() {
        VerifyPlusIngestResult result = new VerifyPlusIngestResult("UNHCR-1", true);
        when(verifyPlusService.ingestScan(request)).thenReturn(result);

        Map<String, Object> response = verifyPlusPortalApi.submit(request);

        assertNotNull(response);
        assertEquals("UNHCR-1", response.get("unhcrNumber"));
        assertEquals(true, response.get("duplicate"));
        verify(verifyPlusService).ingestScan(request);
    }

    @Test
    @DisplayName("Given a malformed payload, when submitted, then propagates InvalidVerifyPlusPayloadException")
    void submit_malformedPayload_propagatesValidationException() {
        when(verifyPlusService.ingestScan(request))
            .thenThrow(new InvalidVerifyPlusPayloadException("Malformed Verify+ payload"));

        assertThrows(InvalidVerifyPlusPayloadException.class, () -> verifyPlusPortalApi.submit(request));
    }
}
