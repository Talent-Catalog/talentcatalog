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

package org.tctalent.server.model.db;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

/**
 * Entity corresponding to a skill that has been entered by a Talent Catalog user.
 *
 * @author John Cameron
 */
@Entity
@Table(name = "skills_tc_en")
@SequenceGenerator(name = "seq_gen", sequenceName = "skills_tc_en_id_seq", allocationSize = 1)
@Getter
@Setter
public class SkillsTcEn extends AbstractDomainObject<Long> {

    /**
     * The name of the skill.
     */
    private String name;

    /**
     * The date and time when the skill was created.
     */
    @CreatedDate
    private OffsetDateTime createdDate;

    /**
     * The user who created the skill.
     */
    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
