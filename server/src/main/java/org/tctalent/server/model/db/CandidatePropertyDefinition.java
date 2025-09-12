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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * This has a loose connection to {@link CandidateProperty}. It is not necessary for every name
 * in a CandidateProperty to have a corresponding definition for that name in this entity.
 * In other words, a CandidatePropertyDefinition is optional for a given name.
 * <p>
 * This provides backward compatibility for existing properties.
 * </p>
 * <p>
 *     However front end code may only present properties which have definitions to users for
 *     selection.
 * </p>
 *
 * @author John Cameron
 */
@Entity
@Table(name = "candidate_property_definition")
@Getter
@Setter
public class CandidatePropertyDefinition extends AbstractDomainObject<Long> {
    @NonNull
    String name;

    @Nullable
    String label;

    @Nullable
    String definition;

    @Nullable
    String type;
}

