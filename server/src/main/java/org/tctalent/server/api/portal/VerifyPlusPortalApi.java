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

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.request.verify.VerifyPlusScanRequest;
import org.tctalent.server.service.db.verify.VerifyPlusIngestResult;
import org.tctalent.server.service.db.verify.VerifyPlusService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Controller for handling Verify Plus scan requests from the portal.
 * It provides an endpoint to submit scan requests and returns the result of the scan.
 * The result includes the UNHCR number and a flag indicating if it is a duplicate.
 *
 * @author sadatmalik
 */
@RestController
@RequestMapping("/api/portal/verify-plus")
@RequiredArgsConstructor
public class VerifyPlusPortalApi {

    private final VerifyPlusService verifyPlusService;

    @PostMapping
    public Map<String, Object> submit(@Valid @RequestBody VerifyPlusScanRequest request) {
        VerifyPlusIngestResult result = verifyPlusService.ingestScan(request);
        return verifyPlusDto().build(result);
    }

    private DtoBuilder verifyPlusDto() {
        return new DtoBuilder()
            .add("unhcrNumber")
            .add("duplicate");
    }
}
