/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.request.candidate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request for the intake fields on the TC (completedBy & completedDate), these are either set at the time of intake
 * completion, or when entering a previously completed external intake.
 *
 * @author Caroline Cameron
 */
@Getter
@Setter
@ToString
public class CandidateIntakeAuditRequest {

    /**
     * Optional field, only provided if it is an external intake being entered at a later date.
     * If the intake is being completed at the current time, then we use the current time to set the completed date.
     */
    @Nullable
    private LocalDate completedDate;

    @NotNull
    private boolean fullIntake;
}
