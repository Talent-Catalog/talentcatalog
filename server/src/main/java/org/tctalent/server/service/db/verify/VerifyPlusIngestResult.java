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

package org.tctalent.server.service.db.verify;

import lombok.Getter;

/**
 * Represents the result of a Verify Plus scan ingestion.
 * It contains the UNHCR number associated with the scan and a flag indicating whether the scan
 * is for a duplicate number.
 *
 * @author sadatmalik
 */
@Getter
public class VerifyPlusIngestResult {

    private final String unhcrNumber;
    private final boolean duplicate;

    public VerifyPlusIngestResult(String unhcrNumber, boolean duplicate) {
        this.unhcrNumber = unhcrNumber;
        this.duplicate = duplicate;
    }
}
