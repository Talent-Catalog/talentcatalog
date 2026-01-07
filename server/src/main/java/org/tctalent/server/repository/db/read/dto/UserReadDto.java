/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.repository.db.read.dto;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Getter
@Setter
@SqlTable(name="users", alias = "u")
@SqlDefaults(mapUnannotatedColumns = true)
public class UserReadDto {
    private OffsetDateTime createdDate;
    private String email;
    private String emailVerified;
    private String firstName;
    private Long id;
    private OffsetDateTime lastLogin;
    private String lastName;

    @JsonOneToOne(joinColumn = "partner_id")
    private PartnerReadDto partner;

    private OffsetDateTime updatedDate;
    private String username;
}
