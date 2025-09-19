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

package org.tctalent.server.candidateservices.core.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tctalent.server.candidateservices.domain.events.ServiceAssignedEvent;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.service.db.email.EmailHelper;


@Component
@RequiredArgsConstructor
public class NotificationsListener {

  private final CandidateRepository candidates;
  private final EmailHelper emailHelper;

  @Async
  @TransactionalEventListener
  public void onAssigned(ServiceAssignedEvent e) {
    var cid = e.assignment().getCandidateId();

    Candidate c = candidates.findById(cid)
        .orElseThrow(() -> new NoSuchObjectException("Candidate with ID " + cid + " not found"));

    emailHelper.sendDuolingoCouponEmail(c.getUser());
  }

}
