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

package org.tctalent.server.request.user;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;

@Getter
@Setter
public class UpdateUserRequest {

    private String email;

    private String firstName;

    private String lastName;

    private Long partnerId;

    private String password;

    private Boolean readOnly;

    private Role role;
    private Boolean jobCreator;

    private Long approverId;

    private String purpose;

    //TODO JC Just ids
    private List<Country> sourceCountries;

    private Status status;

    private String username;

    private Boolean usingMfa;
}
