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

package org.tctalent.server.repository.db;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Status;

/**
 * MODEL - JPA query joining a collection attribute.
 * <p/>
 * See {@link #findSourcePartnerByAutoassignableCountry(Country)} - noting join with sourceCountries attribute
 */

public interface PartnerRepository extends CacheEvictingRepository<PartnerImpl, Long>, JpaSpecificationExecutor<PartnerImpl> {

    @Query("select p from Partner p where p.defaultSourcePartner = :defaultSourcePartner")
    Optional<PartnerImpl> findByDefaultSourcePartner(@Param("defaultSourcePartner") boolean defaultSourcePartner);

    @Query("select p from Partner p where lower(p.abbreviation) = lower(:abbreviation)")
    Optional<PartnerImpl> findByAbbreviation(@Param("abbreviation") String abbreviation);

    @Query("select p from Partner p join p.sourceCountries c "
        + "where c = :country and p.autoAssignable = true and p.status = 'active'")
    List<PartnerImpl> findSourcePartnerByAutoassignableCountry(@Param("country") Country country);

    @Query(" select p.name from Partner p "
        + " where p.id in (:ids) order by p.name asc" )
    List<String> getNamesForIds(@Param("ids") List<Long> ids);

    List<PartnerImpl> findByStatusOrderByName(Status status);
}
