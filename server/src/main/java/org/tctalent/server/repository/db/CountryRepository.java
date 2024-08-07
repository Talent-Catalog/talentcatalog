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
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Status;

public interface CountryRepository extends JpaRepository<Country, Long>, JpaSpecificationExecutor<Country> {

    @NotNull
    @Override
    @CacheEvict(value = "users", allEntries = true)
    <T extends Country> T save(@NotNull T country);

    @NotNull
    @Override
    @CacheEvict(value = "users", allEntries = true)
    <T extends Country> List<T> saveAll(@NotNull Iterable<T> countries);

    @NotNull
    @Override
    @CacheEvict(value = "users", allEntries = true)
    <T extends Country> T saveAndFlush(@NotNull T country);

    @NotNull
    @Override
    @CacheEvict(value = "users", allEntries = true)
    <T extends Country> List<T> saveAllAndFlush(@NotNull Iterable<T> countries);

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void delete(@NotNull Country country);

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteById(@NotNull Long id);

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteAll();

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteAll(@NotNull Iterable<? extends Country> countries);

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteAllInBatch();

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteAllInBatch(@NotNull Iterable<Country> countries);

    @Override
    @CacheEvict(value = "users", allEntries = true)
    void deleteAllByIdInBatch(@NotNull Iterable<Long> ids);

    @Query(" select c from Country c "
            + " where c.status = :status order by c.name asc")
    List<Country> findByStatus(@Param("status") Status status);

    @Query(" select distinct c from Country c "
            + " where lower(c.name) = lower(:name)"
            + " and c.status != 'deleted' order by c.name asc" )
    Country findByNameIgnoreCase(@Param("name") String name);

    @Query(" select c.name from Country c "
            + " where c.id in (:ids) order by c.name asc" )
    List<String> getNamesForIds(@Param("ids") List<Long> ids);

    @Query(" select c from Country c "
            + " where c.status = :status"
            + " and c in (:countries)" )
    List<Country> findByStatusAndSourceCountries(@Param("status") Status status, @Param("countries") Set<Country> countries);

}
