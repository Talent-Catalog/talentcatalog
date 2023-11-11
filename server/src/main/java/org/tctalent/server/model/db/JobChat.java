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

package org.tctalent.server.model.db;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * Represents a JobChat - which is like a Slack Channel associated with a particular job opportunity
 *
 * @author John Cameron
 */
@Getter
@Setter
@Entity
@Table(name = "job_chat")
@SequenceGenerator(name = "seq_gen", sequenceName = "job_chat_id_seq", allocationSize = 1)
public class JobChat extends AbstractAuditableDomainObject<Long> {

  /**
   * Job opportunity associated with chat
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id")
  private SalesforceJobOpp jobOpp;

  /**
   * Optional candidate opportunity associated with chat. This will be required for job chats
   * related to a particular candidate going for that job.
   */
  @Nullable
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "candidate_opp_id")
  private CandidateOpportunity candidateOpp;

  /**
   * Optional source partner associated with chat. This will be required for job chats
   * involving a particular source partner working on the job.
   */
  @Nullable
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_partner_id")
  private PartnerImpl sourcePartner;

  @Enumerated(EnumType.STRING)
  private JobChatType type;
}
