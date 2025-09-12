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

package org.tctalent.server.repository.db;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.NonNull;
import org.tctalent.server.model.db.CandidatePropertyDefinition;

/**
 * Expose a Spring Data Rest API for CandidatePropertyDefinition.
 *
 * @author John Cameron
 */
@RepositoryRestResource(path="candidate-property-definitions")
public interface CandidatePropertyDefinitionRepository
    extends JpaRepository<CandidatePropertyDefinition, Long> {

    //This is a read-only repo. Don't expose any of the write methods on the API.

    @Override @RestResource(exported = false) @NonNull
    <S extends CandidatePropertyDefinition> S save(@NonNull S e);
    @Override @RestResource(exported = false) @NonNull
    <S extends CandidatePropertyDefinition> List<S> saveAll(@NonNull Iterable<S> entities);
    @Override @RestResource(exported = false)
    void deleteById(@NonNull Long id);
    @Override @RestResource(exported = false)
    void delete(@NonNull CandidatePropertyDefinition e);
    @Override @RestResource(exported = false)
    void deleteAll(@NonNull Iterable<? extends CandidatePropertyDefinition> es);
    @Override @RestResource(exported = false)
    void deleteAll();

}
