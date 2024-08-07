/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import javax.validation.constraints.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CacheEvictingRepository<T, ID> extends JpaRepository<T, ID> {

  @NotNull
  @Override
  @CacheEvict(value = "users", allEntries = true)
  <S extends T> S save(@NotNull S entity);

  @NotNull
  @Override
  @CacheEvict(value = "users", allEntries = true)
  <S extends T> List<S> saveAll(@NotNull Iterable<S> entities);

  @NotNull
  @Override
  @CacheEvict(value = "users", allEntries = true)
  <S extends T> S saveAndFlush(@NotNull S entity);

  @NotNull
  @Override
  @CacheEvict(value = "users", allEntries = true)
  <S extends T> List<S> saveAllAndFlush(@NotNull Iterable<S> entities);

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void delete(@NotNull T entity);

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteById(@NotNull ID id);

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteAll();

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteAll(@NotNull Iterable<? extends T> entities);

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteAllInBatch();

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteAllInBatch(@NotNull Iterable<T> entities);

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteAllByIdInBatch(@NotNull Iterable<ID> ids);
}
