/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.TcInstanceType;

/**
 * Provides details about the running instance of the Talent Catalog.
 *
 * @author John Cameron
 */
@Service
@Getter
@RequiredArgsConstructor
@Slf4j
public class TcInstanceService {

    @Value("${tc.instance-type}")
    private TcInstanceType instanceType;

    public String getDefaultSourcePartnerAbbreviation() {
        return isTBB() ? "TBB" : "NONE";
    }

    public String getDefaultSourcePartnerName() {
        return isTBB() ? "Talent Beyond Boundaries" : "NONE";
    }

    public boolean isTBB() {
        return TcInstanceType.TBB.equals(instanceType);
    }

    public boolean isGRN() {
        return TcInstanceType.GRN.equals(instanceType);
    }
}
