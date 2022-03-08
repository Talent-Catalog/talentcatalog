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

package org.tbbtalent.server.model.db;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

/**
 * Primary key for {@link CandidateProperty}. Needed because we have a composite private key.
 * <p/>
 * See https://www.baeldung.com/jpa-composite-primary-keys
 * <p/>
 * Note that hashcode and equals of Candidate is based on its id - like all entities that extend
 * {@link AbstractDomainObject}.
 *
 * @author John Cameron
 */
@Getter
@Setter
@EqualsAndHashCode
public class CandidatePropertyKey implements Serializable {

    @NonNull
    private Candidate candidate;

    @NonNull
    private String name;
}
