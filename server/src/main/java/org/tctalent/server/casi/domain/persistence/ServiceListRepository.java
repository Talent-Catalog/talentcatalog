/*
 * Copyright (c) 2026 Talent Catalog.
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

package org.tctalent.server.casi.domain.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.casi.domain.model.ListRole;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.casi.domain.model.ServiceProvider;

/**
 * Repository for {@link ServiceListEntity}.
 */
public interface ServiceListRepository extends JpaRepository<ServiceListEntity, Long> {

  /**
   * Returns true if any service list record exists for the given provider, service code, and role.
   * Used by {@link org.tctalent.server.casi.core.services.ServiceListSetupService} to skip
   * creation of single-instance roles that are already registered.
   */
  @Query("""
      select count(e) > 0
      from ServiceListEntity e
      where e.provider    = :provider
        and e.serviceCode = :serviceCode
        and e.listRole    = :role
      """)
  boolean existsByProviderAndServiceCodeAndRole(
      @Param("provider") ServiceProvider provider,
      @Param("serviceCode") ServiceCode serviceCode,
      @Param("role") ListRole role);

  /**
   * Returns true if a service list record exists matching provider, service code, role, and
   * the exact saved list name. Used for {@code allowsMultiple=true} roles such as
   * {@link org.tctalent.server.casi.domain.model.ListRole#SERVICE_ELIGIBILITY}.
   */
  @Query("""
      select count(e) > 0
      from ServiceListEntity e
      where e.provider         = :provider
        and e.serviceCode      = :serviceCode
        and e.listRole         = :role
        and e.savedList.name   = :listName
      """)
  boolean existsByProviderAndServiceCodeAndRoleAndName(
      @Param("provider") ServiceProvider provider,
      @Param("serviceCode") ServiceCode serviceCode,
      @Param("role") ListRole role,
      @Param("listName") String listName);

  /**
   * Returns all service list records for the given provider, service code, and role.
   */
  @Query("""
      select e
      from ServiceListEntity e
      where e.provider    = :provider
        and e.serviceCode = :serviceCode
        and e.listRole    = :role
      """)
  List<ServiceListEntity> findByProviderAndServiceCodeAndRole(
      @Param("provider") ServiceProvider provider,
      @Param("serviceCode") ServiceCode serviceCode,
      @Param("role") ListRole role);

  /**
   * Returns the single service list record for a non-multiple role, or empty if none exists.
   */
  @Query("""
      select e
      from ServiceListEntity e
      where e.provider    = :provider
        and e.serviceCode = :serviceCode
        and e.listRole    = :role
      """)
  Optional<ServiceListEntity> findFirstByProviderAndServiceCodeAndRole(
      @Param("provider") ServiceProvider provider,
      @Param("serviceCode") ServiceCode serviceCode,
      @Param("role") ListRole role);

  /**
   * Returns the service list record for a given saved list ID, if one exists.
   */
  @Query("""
      select e
      from ServiceListEntity e
      where e.savedList.id = :savedListId
      """)
  Optional<ServiceListEntity> findBySavedListId(@Param("savedListId") Long savedListId);
}
