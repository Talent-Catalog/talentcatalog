/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.request.user;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.request.PagedSearchRequest;

@Getter
@Setter
@ToString(callSuper = true)
public class SearchUserRequest extends PagedSearchRequest {
    private String keyword;
    private List<Role> role;
    private Status status;
}

