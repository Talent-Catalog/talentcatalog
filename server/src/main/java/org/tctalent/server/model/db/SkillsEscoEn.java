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

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity corresponding to imported English language ESCO skills.
 *
 * @author John Cameron
 */
@Entity
@Table(name = "skills_esco_en")
@Getter
@Setter
public class SkillsEscoEn implements Serializable {
    @Id
    private String concepturi;

    private String concepttype;

    private String skilltype;
    private String preferredlabel;
    private String altlabels;
    private String hiddenlabels;
    private String definition;
    private String description;

}
