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

package org.tctalent.server.service.db;

import java.util.List;
import org.springframework.lang.NonNull;

/**
 * Service related to exposing and managing skills.
 *
 * @author John Cameron
 */
public interface SkillsService {

    //TODO JC Doc
    List<String> extractSkills(String text);

    /**
     * Returns English language skills.
     * <p>
     * Skills can be single words or short phrases. There could be around 20,000 of them. This list
     * of skills does not change often - so the skill list will be cached.
     * <p>
     * These skills can come from multiple sources (for example Esco and ONet).
     * <p>
     * All skills are converted to lower case and duplicates are removed.
     *
     * @return Immutable list of skills.
     */
    @NonNull
    List<String> getSkills();
}
