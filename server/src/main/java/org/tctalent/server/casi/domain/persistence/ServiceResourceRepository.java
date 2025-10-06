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

package org.tctalent.server.casi.domain.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.casi.domain.model.ResourceStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;

public interface ServiceResourceRepository extends JpaRepository<ServiceResourceEntity, Long> {

  @Query(value = """
    select * from service_resource
    where provider     = :provider
      and service_code = :serviceCode
      and status       = 'AVAILABLE'
    order by id
    for update skip locked
    limit 1
    """, nativeQuery = true)
  ServiceResourceEntity lockNextAvailable(@Param("provider") String provider,
      @Param("serviceCode") ServiceCode serviceCode);


  @Query("""
    select r
    from ServiceResourceEntity r
    where r.provider = :provider
      and r.serviceCode = :serviceCode
      and r.status = :status
    order by r.id desc
    """)
  List<ServiceResourceEntity> findByProviderAndServiceCodeAndStatus(
      @Param("provider") String provider,
      @Param("serviceCode") ServiceCode serviceCode,
      @Param("status") ResourceStatus status);


  @Query("""
    select r
    from ServiceResourceEntity r
    where r.provider = :provider
      and r.resourceCode = :resourceCode
    """)
  Optional<ServiceResourceEntity> findByProviderAndResourceCode(
      @Param("provider") String provider,
      @Param("resourceCode") String resourceCode);

  boolean existsByProviderAndResourceCode(String provider, String resourceCode);

  // provider + serviceCode (enum)
  @Query("""
      select count(r)
      from ServiceResourceEntity r
      where r.provider    = :provider
        and r.serviceCode = :serviceCode
        and r.status      = org.tctalent.server.casi.domain.model.ResourceStatus.AVAILABLE
      """)
  long countAvailableByProviderAndService(
      @Param("provider") String provider,
      @Param("serviceCode") ServiceCode serviceCode);

  // provider only (all service codes)
  @Query("""
      select count(r)
      from ServiceResourceEntity r
      where r.provider    = :provider
        and r.status      = org.tctalent.server.casi.domain.model.ResourceStatus.AVAILABLE
      """)
  long countAvailableByProvider(
      @Param("provider") String provider);

  // All providers; skip EXPIRED/REDEEMED/DISABLED; ignore null expiresAt
  @Query("""
    select r from ServiceResourceEntity r
     where r.expiresAt < :now
       and r.expiresAt is not null
       and r.status not in :excluded
  """)
  List<ServiceResourceEntity> findExpirable(@Param("now") LocalDateTime now,
      @Param("excluded") Collection<ResourceStatus> excluded);

  // Provider scoped; skip EXPIRED/REDEEMED/DISABLED; ignore null expiresAt
  @Query("""
    select r from ServiceResourceEntity r
     where r.provider = :provider
       and r.expiresAt < :now
       and r.expiresAt is not null
       and r.status not in :excluded
  """)
  List<ServiceResourceEntity> findExpirableForProvider(@Param("provider") String provider,
      @Param("now") LocalDateTime now,
      @Param("excluded") Collection<ResourceStatus> excluded);

}
