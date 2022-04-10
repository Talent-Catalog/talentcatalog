/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.api.admin;

import java.util.Set;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.util.dto.DtoPropertyFilter;

/**
 * Filters out properties in a DtoBuilder based on a user role.
 *
 * @author John Cameron
 */
public class RoleBasedDtoPropertyFilter  implements DtoPropertyFilter {
    private final Role role;

    private final Set<String> publicProperties;
    private final Set<String> semiLimitedExtraProperties;

    public RoleBasedDtoPropertyFilter(@NonNull Role role,
        @Nullable Set<String> publicProperties, @Nullable Set<String> semiLimitedExtraProperties) {
        this.role = role;
        this.publicProperties = publicProperties;
        this.semiLimitedExtraProperties = semiLimitedExtraProperties;
    }

    @Override
    public boolean ignoreProperty(Object o, String property) {
        boolean ignore;

        if (publicProperties != null && publicProperties.contains(property)) {
            //Public properties are never ignored
            ignore = false;
        } else {
            //It is not a public property. Whether it is ignored depends on the user's role.
            switch (role) {

                case admin:
                case sourcepartneradmin:
                    //Admins see all candidate properties
                    ignore = false;
                    break;

                case limited:
                    //Limited roles can only see public properties
                    ignore = true;
                    break;

                case semilimited:
                    //Ignore if property is not one of the extra semilimited properties
                    ignore = semiLimitedExtraProperties == null
                        || !semiLimitedExtraProperties.contains(property);
                    break;

                default:
                    //To be safe, ignore if null or unexpected new roles
                    ignore = true;
            }
        }
        return ignore;
    }
}

