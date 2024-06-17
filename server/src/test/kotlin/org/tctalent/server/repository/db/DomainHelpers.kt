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

package org.tctalent.server.repository.db

import org.apache.commons.lang3.RandomStringUtils
import java.time.OffsetDateTime
import org.springframework.data.jpa.repository.JpaRepository
import org.tctalent.server.model.db.AbstractAuditableDomainObject
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateStatus
import org.tctalent.server.model.db.Status
import org.tctalent.server.model.db.TaskAssignmentImpl
import org.tctalent.server.model.db.TaskImpl
import org.tctalent.server.model.db.User

fun getTask(taskName: String = "DEFAULT", taskDisplay: String = "DEFAULT DISPLAY"): TaskImpl {
  return TaskImpl().apply {
    name = taskName
    displayName = taskDisplay
    createdBy = user(9997)
    createdDate = OffsetDateTime.now()
  }
}

fun getSavedTask(repo: TaskRepository): TaskImpl = saveHelperObject(repo, getTask()) as TaskImpl

fun getTaskAssignment(): TaskAssignmentImpl {
  return TaskAssignmentImpl().apply {
    activatedBy = user(9996)
    activatedDate = OffsetDateTime.now()
    status = Status.active
    candidate = Candidate().apply { id = 999 }
  }
}

fun getSavedCandidate(repo: CandidateRepository): Candidate = saveHelperObject(repo, getCandidate()) as Candidate

fun getCandidate(): Candidate {
  return Candidate().apply {
    candidateNumber = "TEMP%04d" + RandomStringUtils.random(6)
    phone = "999999999"
    user = user(9995)
    contactConsentPartners = true
    contactConsentRegistration = true
    status = CandidateStatus.active
    createdBy = user(1999L)
    createdDate = OffsetDateTime.now()
  }
}

fun <T, ID> saveHelperObject(repo: JpaRepository<T, ID>, entity: T): T {
  return repo.saveAndFlush(entity)
}

fun user(idToUse: Long): User {
  return User().apply {
    id = idToUse
    username = "jo.bloggs@email.com"
    firstName = "jo"
    lastName = "bloggs"
  }
}