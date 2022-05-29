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

package org.tbbtalent.server.repository.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.PartnerImpl;

public interface PartnerRepository extends JpaRepository<PartnerImpl, Long>, JpaSpecificationExecutor<PartnerImpl> {


    @Query("select p from SourcePartner p where p.defaultSourcePartner = :defaultSourcePartner")
    Optional<PartnerImpl> findByDefaultSourcePartner(@Param("defaultSourcePartner") boolean defaultSourcePartner);

    @Query("select p from SourcePartner p where p.registrationDomain = :hostDomain")
    Optional<PartnerImpl> findByRegistrationUrl(@Param("hostDomain") String hostDomain);
}
