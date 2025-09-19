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
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tctalent.server.candidateservices.domain.events.ServiceAssignedEvent;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.service.db.email.EmailHelper;


@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationListener {

  private final CandidateRepository candidates;
  private final EmailHelper emailHelper;

  @Async
  @TransactionalEventListener
  public void onAssigned(ServiceAssignedEvent event) {
    var a = event.assignment();
    String action = "EmailNotificationListener:onAssigned: " + a.getProvider() + " " + a.getServiceCode();

    Long cid = a.getCandidateId();
    if (cid == null) {
      LogBuilder.builder(log)
          // todo -- pass the user (and candidate) in the entity
//          .user(Optional.ofNullable(event.assignment().getActorId()))
          .action(action)
          .message("Resource assignment " + a.getResource().getResourceCode() +
              " with no candidateId " + cid)
          .logWarn();
      return;
    }

    var optional = candidates.findById(cid);
    if (optional.isEmpty()) {
      LogBuilder.builder(log)
          // todo -- pass the user (and candidate) in the entity
//           .user(Optional.ofNullable(event.assignment().getActorId()))
          .action(action)
          .message("Candidate not found for resource assignment  " +
              a.getResource().getResourceCode() + " to candidate " + cid)
          .logWarn();
      return;
    }

    var candidate = optional.get();
    try {
      if (a.getProvider().equals("DUOLINGO")) {
        emailHelper.sendDuolingoCouponEmail(candidate.getUser());
      }

      // add other providers here

    } catch (Exception ex) {
        LogBuilder.builder(log)
            // todo -- pass the user (and candidate) in the entity
//            .user(Optional.ofNullable(event.assignment().getActorId()))
            .action(action)
            .message("Failed sending resource assignment email for "
                + a.getResource().getResourceCode() + " to candidate " + cid)
            .logWarn(ex);
    }
  }
}
