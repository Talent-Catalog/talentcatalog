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

package org.tctalent.server.model.db;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
@Entity
@Table(name = "duolingo_coupon")
@SequenceGenerator(name = "seq_gen", sequenceName = "coupon_id_seq", allocationSize = 1)
public class DuolingoCoupon {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_gen")
  @Column(name = "id")
  private Long id;

  @Column(name = "coupon_code", unique = true)
  private String couponCode;

  @Nullable
  @ManyToOne
  @JoinColumn(name = "candidate_id")
  private Candidate candidate;

  @Column(name = "expiration_date")
  private LocalDateTime expirationDate;

  @Nullable
  @Column(name = "date_sent")
  private LocalDateTime dateSent;

  @Nullable
  @Column(name = "assignee_email")
  private String assigneeEmail;

  @Column(name = "coupon_status")
  private String couponStatus;

  @Nullable
  @Column(name = "test_status")
  private String testStatus;

}
