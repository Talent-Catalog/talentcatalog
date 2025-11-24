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

package org.tctalent.server.repository.db;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.Status;

public interface LanguageRepository extends JpaRepository<Language, Long>, JpaSpecificationExecutor<Language> {

    @Query(" select l from Language l "
            + " where l.status = :status order by l.name asc")
    List<Language> findByStatus(@Param("status") Status status);

    @Query(" select distinct l from Language l "
            + " where lower(l.name) = lower(:name)"
            + " and l.status != 'deleted' order by l.name asc" )
    Language findByNameIgnoreCase(@Param("name") String name);

    Optional<Language> findByIsoCode(String isoCode);
}
