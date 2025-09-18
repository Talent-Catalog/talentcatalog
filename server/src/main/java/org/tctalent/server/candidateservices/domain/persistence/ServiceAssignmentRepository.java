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

package org.tctalent.server.candidateservices.domain.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.candidateservices.domain.model.AssignmentStatus;

public interface ServiceAssignmentRepository extends JpaRepository<ServiceAssignmentEntity, Long> {

  @Query("""
         select e
         from ServiceAssignmentEntity e
         where e.candidate.id = :candidateId
         order by e.assignedAt desc
         """)
  List<ServiceAssignmentEntity> findByCandidateIdOrderByAssignedAtDesc(@Param("candidateId") Long candidateId);


  @Query("""
         select e
         from ServiceAssignmentEntity e
         where e.candidate.id = :candidateId
           and e.status = :status
         """)
  List<ServiceAssignmentEntity> findByCandidateIdAndStatus(
      @Param("candidateId") Long candidateId,
      @Param("status") AssignmentStatus status);


  @Query("""
       select e
       from ServiceAssignmentEntity e
       where e.candidate.id = :candidateId
         and e.provider     = :provider
         and e.serviceCode  = :serviceCode
       order by e.assignedAt desc
       """)
  List<ServiceAssignmentEntity> findByCandidateAndProviderAndService(
      @Param("candidateId") Long candidateId,
      @Param("provider") String provider,
      @Param("serviceCode") String serviceCode);


  @Query("""
       select e
       from ServiceAssignmentEntity e
       where e.candidate.id = :candidateId
         and e.provider     = :provider
         and e.serviceCode  = :serviceCode
         and e.status       = :status
       order by e.assignedAt desc
       """)
  List<ServiceAssignmentEntity> findByCandidateAndProviderServiceAndStatus(
      @Param("candidateId") Long candidateId,
      @Param("provider") String provider,
      @Param("serviceCode") String serviceCode,
      @Param("status") AssignmentStatus status);


  @Query("""
  select e
  from ServiceAssignmentEntity e
  where e.provider    = :provider
    and e.serviceCode = :serviceCode
    and e.resource.id  = :resourceId
  order by e.assignedAt desc
  """)
  Optional<ServiceAssignmentEntity> findTopByProviderAndServiceAndResource(
      @Param("provider") String provider,
      @Param("serviceCode") String serviceCode,
      @Param("resourceId") Long resourceId);


  @Query("""
         select e
         from ServiceAssignmentEntity e
         join fetch e.resource r
         where e.candidate.id = :candidateId
         order by e.assignedAt desc
         """)
  List<ServiceAssignmentEntity> findAllForCandidateWithResource(@Param("candidateId") Long candidateId);
}
