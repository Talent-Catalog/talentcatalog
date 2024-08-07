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

import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.tctalent.server.model.db.Employer;

public interface EmployerRepository extends JpaRepository<Employer, Long>,
    JpaSpecificationExecutor<Employer> {

    @NotNull
    @Override
    @CacheEvict(value = "users", allEntries = true)
    <T extends Employer> T save(@NotNull T employer);

    @NotNull
    @Override
    @CacheEvict(value = "users", allEntries = true)
    <T extends Employer> List<T> saveAll(@NotNull Iterable<T> employers);

    @NotNull
    @Override
    @CacheEvict(value = "users", allEntries = true)
    <T extends Employer> T saveAndFlush(@NotNull T employer);

    @NotNull
    @Override
    @CacheEvict(value = "users", allEntries = true)
    <T extends Employer> List<T> saveAllAndFlush(@NotNull Iterable<T> employers);

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void delete(@NotNull Employer employer);

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteById(@NotNull Long id);

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteAll();

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteAll(@NotNull Iterable<? extends Employer> employers);

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteAllInBatch();

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteAllInBatch(@NotNull Iterable<Employer> employers);

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteAllByIdInBatch(@NotNull Iterable<Long> ids);

    /**
     * Look up employer by sfId.
     * @param sfId Salesforce id - stored in TC table.
     * @return Optional employer
     * See <a href="https://docs.spring.io/spring-data/data-commons/docs/current/reference/html/#repositories.query-methods.query-creation">
     *     Spring magic </a>
     */
    Optional<Employer> findFirstBySfId(String sfId);

}
