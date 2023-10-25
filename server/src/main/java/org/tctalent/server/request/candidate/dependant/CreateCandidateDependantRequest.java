/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.request.candidate.dependant;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.DependantRelations;
import org.tctalent.server.model.db.Registration;
import org.tctalent.server.model.db.YesNo;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class CreateCandidateDependantRequest {
    @Nullable
    private DependantRelations relation;
    @Nullable
    private String relationOther;
    @Nullable
    private LocalDate dob;
    @Nullable
    private String name;
    @Nullable
    private Registration registered;
    @Nullable
    private String registeredNumber;
    @Nullable
    private String registeredNotes;
    @Nullable
    private YesNo healthConcern;
    @Nullable
    private String healthNotes;
}
