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

package org.tctalent.server.model.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "duolingo_extra_fields")
@SequenceGenerator(name = "seq_gen", sequenceName = "duolingo_extra_fields_id_seq", allocationSize = 1)
@Getter
@Setter
public class DuolingoExtraFields extends AbstractDomainObject<Long> {

  @Column(name = "certificate_url")
  private String certificateUrl;

  @Column(name = "interview_url")
  private String interviewUrl;

  @Column(name = "verification_date")
  private String verificationDate;

  @Column(name = "percent_score")
  private int percentScore;

  @Column(name = "scale")
  private int scale;

  @Column(name = "literacy_subscore")
  private int literacySubscore;

  @Column(name = "conversation_subscore")
  private int conversationSubscore;

  @Column(name = "comprehension_subscore")
  private int comprehensionSubscore;

  @Column(name = "production_subscore")
  private int productionSubscore;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "candidate_exam_id", referencedColumnName = "id", nullable = false, unique = true)
  private CandidateExam candidateExam;
}