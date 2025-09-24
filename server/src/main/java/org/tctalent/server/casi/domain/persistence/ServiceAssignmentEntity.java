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

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.casi.domain.model.AssignmentStatus;
import org.tctalent.server.casi.domain.model.ServiceCode;
import org.tctalent.server.model.db.AbstractDomainObject;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.User;

@Entity
@Table(name = "service_assignment")
@SequenceGenerator(name = "seq_gen", sequenceName = "service_assignment_id_seq", allocationSize = 1)
@Getter
@Setter
public class ServiceAssignmentEntity extends AbstractDomainObject<Long> {

  @Column(nullable=false)
  private String provider; // e.g. "DUOLINGO" // TODO -- SM -- make enum? Provider.DUOLINGO

  @Enumerated(EnumType.STRING)
  @Column(nullable=false)
  private ServiceCode serviceCode; // e.g. "DUOLINGO_TEST_PROCTORED"

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name="resource_id", nullable=false)
  private ServiceResourceEntity resource;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name="candidate_id", nullable=false)
  private Candidate candidate;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name="actor_id", nullable=false)
  private User actor;

  @Enumerated(EnumType.STRING) @Column(nullable=false)
  private AssignmentStatus status; // ASSIGNED, REDEEMED, EXPIRED, REASSIGNED

  @Column(name="assigned_at", nullable=false)
  private LocalDateTime assignedAt;

  @Column(name="created_at", nullable=false, updatable=false)
  private Instant createdAt = Instant.now();

}
