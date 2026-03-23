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

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.tctalent.server.model.db.Candidate;

/**
 * Generates candidate numbers
 *
 * @author John Cameron
 */
@Component
@RequiredArgsConstructor
public class CandidateNumberGenerator {
    private final TcInstanceService tcInstanceService;

    public String generateCandidateNumber(@NonNull Candidate candidate) {
        //Use id to generate candidate number
        long number = candidate.getId();
        if (tcInstanceService.isGRN()) {
            //GRN uses 5000000 as the first number to distinguish its candidate numbers from
            //TBB instance candidate numbers
            number = number + 5000000L;
        }
        return String.format("%04d", number);
    }
}
