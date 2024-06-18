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

package org.tctalent.server.repository.db.integrationhelp

import java.time.OffsetDateTime
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.data.jpa.repository.JpaRepository
import org.tctalent.server.model.db.*
import org.tctalent.server.repository.db.*
import org.tctalent.server.service.db.audit.AuditAction
import org.tctalent.server.service.db.audit.AuditType

fun getTask(taskName: String = "DEFAULT", taskDisplay: String = "DEFAULT DISPLAY"): TaskImpl {
  return TaskImpl().apply {
    name = taskName
    displayName = taskDisplay
    createdBy = user(9997)
    createdDate = OffsetDateTime.now()
  }
}

fun getSavedTask(repo: TaskRepository): TaskImpl = saveHelperObject(repo, getTask()) as TaskImpl

fun getTaskAssignment(user: User): TaskAssignmentImpl {
  return TaskAssignmentImpl().apply {
    activatedBy = user
    activatedDate = OffsetDateTime.now()
    status = Status.active
    candidate = Candidate().apply { id = 99999999 }
  }
}

fun getSavedUser(userRepo: UserRepository): User = userRepo.save(user())

fun getSavedCandidate(repo: CandidateRepository, savedUser: User): Candidate {
  val candidate = getCandidate()
  candidate.apply { user = savedUser }
  return saveHelperObject(repo, candidate) as Candidate
}

fun getCandidate(): Candidate {
  return Candidate().apply {
    candidateNumber = "TEMP%04d" + RandomStringUtils.random(6)
    phone = "999999999"
    contactConsentPartners = true
    contactConsentRegistration = true
    status = CandidateStatus.active
    createdBy = user(1999L)
    createdDate = OffsetDateTime.now()
  }
}

fun getSavedList(savedListRepo: SavedListRepository): SavedList =
  saveHelperObject(savedListRepo, getSavedList())

fun getSavedList(): SavedList {
  return SavedList().apply {
    description = "SavedList"
    name = "SavedList"
    createdBy = user(1999L)
    createdDate = OffsetDateTime.now()
  }
}

fun getCandidateEducation(): CandidateEducation {
  return CandidateEducation().apply {
    lengthOfCourseYears = 3
    institution = "TESTINSTO"
    country = Country().apply { id = 6192 } // Australia
    educationType = EducationType.Masters
  }
}

fun getSavedReaction(repo: ReactionRepository): Reaction = saveHelperObject(repo, getReaction())

fun getReaction(): Reaction {
  return Reaction().apply { emoji = "Smile" }
}

fun getJobChat(): JobChat {
  return JobChat().apply {
    id = 1
    type = JobChatType.JobCreatorAllSourcePartners
  }
}

fun getSavedChatPost(repo: ChatPostRepository): ChatPost = saveHelperObject(repo, getChatPost())

fun getChatPost(): ChatPost {
  return ChatPost().apply {
    jobChat = org.tctalent.server.repository.db.integrationhelp.getJobChat()
    content = "NothingChatContent"
    createdBy = user(1999L)
    createdDate = OffsetDateTime.now()
  }
}

fun getSavedSurveyType(repo: SurveyTypeRepository): SurveyType =
  saveHelperObject(repo, getSurveyType())

fun getSurveyType(): SurveyType {
  return SurveyType().apply {
    name = "IntTestSurvey"
    status = Status.active
  }
}

fun getSavedSystemLanguage(repo: SystemLanguageRepository): SystemLanguage =
  saveHelperObject(repo, getSystemLanguage())

fun getSystemLanguage(): SystemLanguage {
  return SystemLanguage().apply {
    language = "en"
    label = "English"
    status = Status.active
  }
}

fun <T, ID> saveHelperObject(repo: JpaRepository<T, ID>, entity: T & Any): T =
  repo.saveAndFlush(entity)

fun user(idToUse: Long? = null): User {
  return User().apply {
    id = idToUse
    username = "jo.bloggs@email.com"
    firstName = "jo"
    lastName = "bloggs"
    role = Role.user
    status = Status.active
    partner = PartnerImpl().apply { id = 1L } // This is TBB in dumps.
  }
}

fun getAuditLog(objRef: String): AuditLog {
  return AuditLog().apply {
    type = AuditType.CANDIDATE_OCCUPATION
    userId = 9L
    objectRef = objRef
    eventDate = OffsetDateTime.now()
    action = AuditAction.ADD
    description = "Create a test audit record."
  }
}
