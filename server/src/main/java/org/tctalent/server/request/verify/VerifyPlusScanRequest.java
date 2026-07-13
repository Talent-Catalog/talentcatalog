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

package org.tctalent.server.request.verify;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a request to submit a Verify Plus scan.
 * This request contains the raw payload to be processed for verification.
 * The raw payload must be a non-blank string and should not exceed 8192 characters in length. This
 * sets a generous limit, see:
 * <a href="https://qrplanet.com/help/article/what-storage-capacity-does-a-qr-code-have">...</a>
 *
 * @author sadatmalik
 */
@Getter
@Setter
public class VerifyPlusScanRequest {

    @NotBlank(message = "Raw payload is required")
    @Size(max = 8192, message = "Raw payload exceeds maximum allowed length")
    private String rawPayload;
}
