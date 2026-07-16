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
import lombok.RequiredArgsConstructor;

/**
 * Represents the payload of a Verify Plus scan. It contains the version of the payload, the raw
 * payload data, and the UNHCR ID associated with the scan. For now this is for testing and iterative
 * development. The exact payload structure and fields are expected to change as the Verify Plus
 * integration evolves.
 *
 * @author sadatmalik
 */
@Getter
@RequiredArgsConstructor
public class VerifyPlusPayload {

    private final String version;
    private final String rawPayload;
    private final String unhcrId;

}
