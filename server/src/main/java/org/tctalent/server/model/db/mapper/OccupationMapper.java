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

package org.tctalent.server.model.db.mapper;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.service.db.OccupationService;

/**
 * Maps public object to equivalent entity form local database.
 * <p/>
 * For MapStruct Mapper we need to us abstract class instead of interface so that we can inject in
 * service.
 *
 * @author John Cameron
 */
@Mapper
public abstract class OccupationMapper {

    @Autowired
    protected OccupationService service;

    public Occupation lookUpFromService(
        org.tctalent.anonymization.model.Occupation publicValue) {
        return publicValue == null ? null : service.findByIsco08Code(publicValue.getIsco08Code());

    }
}
