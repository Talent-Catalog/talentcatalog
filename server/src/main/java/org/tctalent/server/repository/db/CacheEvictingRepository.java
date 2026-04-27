/*
 * Copyright (c) 2024 Talent Catalog.
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;

/**
 * This interface extends JpaRepository to provide CRUD operations with automatic cache eviction.
 * The cache eviction clears the "users" cache whenever entities are saved, deleted, or modified.
 *
 * @param <T>  the type of the entity to handle
 * @param <ID> the type of the entity's identifier
 *
 * <p>All methods are annotated with @CacheEvict to clear all entries in the "users" cache upon
 * execution.
 *
 * <p>Annotations:
 * <ul>
 *   <li>@NoRepositoryBean - Indicates that this interface should not be instantiated directly and
 *            is intended to be extended by other repository interfaces.</li>
 *   <li>@CacheEvict - Configures cache eviction for the specified cache ("users").</li>
 * </ul>
 */
@NoRepositoryBean
public interface CacheEvictingRepository<T, ID> extends JpaRepository<T, ID> {

  @NonNull
  @Override
  @CacheEvict(value = "users", allEntries = true)
  <S extends T> S save(@NonNull S entity);

  @NonNull
  @Override
  @CacheEvict(value = "users", allEntries = true)
  <S extends T> List<S> saveAll(@NonNull Iterable<S> entities);

  @NonNull
  @Override
  @CacheEvict(value = "users", allEntries = true)
  <S extends T> S saveAndFlush(@NonNull S entity);

  @NonNull
  @Override
  @CacheEvict(value = "users", allEntries = true)
  <S extends T> List<S> saveAllAndFlush(@NonNull Iterable<S> entities);

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void delete(@NonNull T entity);

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteById(@NonNull ID id);

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteAll();

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteAll(@NonNull Iterable<? extends T> entities);

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteAllInBatch();

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteAllInBatch(@NonNull Iterable<T> entities);

  @Override
  @CacheEvict(value = "users", allEntries = true)
  void deleteAllByIdInBatch(@NonNull Iterable<ID> ids);
}
