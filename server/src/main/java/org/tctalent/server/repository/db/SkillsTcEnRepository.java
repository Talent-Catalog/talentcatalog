/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tctalent.server.model.db.SkillsTcEn;

public interface SkillsTcEnRepository extends JpaRepository<SkillsTcEn, Long> {
    /**
     * Checks if a skill with the given name exists in the database.
     *
     * @param name the name of the skill to check for existence
     * @return true if a skill with the given name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);
}
