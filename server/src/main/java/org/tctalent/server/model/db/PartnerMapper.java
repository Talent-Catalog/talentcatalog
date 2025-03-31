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

package org.tctalent.server.model.db;

import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.service.db.PartnerService;

/**
 * Maps public object to equivalent entity form local database.
 * <p/>
 * For MapStruct Mapper we need to us abstract class instead of interface so that we can inject in
 * service.
 *
 * @author John Cameron
 */
@Mapper
public abstract class PartnerMapper {

    @Autowired
    protected PartnerService service;

    public Partner lookUpFromService(
        org.tctalent.anonymization.model.IdentifiablePartner publicValue) {
        return publicValue == null ? null : service.findByPublicId(publicValue.getPublicId());
    }
}
