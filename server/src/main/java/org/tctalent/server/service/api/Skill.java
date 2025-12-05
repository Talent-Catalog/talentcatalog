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

package org.tctalent.server.service.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Known skills.
 * <p>
 * Currently not used - but this is a suggestion for providing skill names with an indication
 * of their source. It gets complicated because the same skill name could occur in multiple sources.
 * And each source will have its own id for what is essentially the same skill.
 * <p>
 * Currently, we are working with an approach which merges the names of skills from multiple sources,
 * removing duplicates. So it is not clear which source a particular skill name comes from.
 * <p>
 * @author John Cameron
 */
@Getter
@Setter
@ToString
public class Skill {

    /**
     * Source of the skill.
     */
    private SkillSource source;

    /**
     * Unique ID of skill in the source.
     */
    private String sourceSkillId;
}
