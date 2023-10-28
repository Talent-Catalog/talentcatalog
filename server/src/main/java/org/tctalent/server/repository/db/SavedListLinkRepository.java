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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.SavedListLink;

public interface SavedListLinkRepository extends JpaRepository<SavedListLink, Long>, JpaSpecificationExecutor<SavedListLink>  {

    @Query(" select distinct l from SavedListLink l "
            + " where lower(l.link) = lower(:link)")
    SavedListLink findByLinkIgnoreCase(@Param("link") String link);

    @Query(" select distinct l from SavedListLink l "
            + " left join l.savedList s "
            + " where s.id = :savedListId")
    SavedListLink findBySavedList(@Param("savedListId") long savedListId);
}
