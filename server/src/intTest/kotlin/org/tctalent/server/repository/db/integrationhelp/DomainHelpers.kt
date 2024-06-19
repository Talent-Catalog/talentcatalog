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
import kotlin.random.Random
import org.springframework.data.jpa.repository.JpaRepository
import org.tctalent.server.model.db.*
import org.tctalent.server.repository.db.*
import org.tctalent.server.service.db.audit.AuditAction
import org.tctalent.server.service.db.audit.AuditType

/**
 * Retrieves a new TaskImpl instance initialized with provided or default values.
 *
 * @param taskName The name of the task (default: "DEFAULT").
 * @param taskDisplay The display name of the task (default: "DEFAULT DISPLAY").
 * @return A new TaskImpl instance.
 */
fun getTask(taskName: String = "DEFAULT", taskDisplay: String = "DEFAULT DISPLAY"): TaskImpl {
  return TaskImpl().apply {
    name = taskName
    displayName = taskDisplay
    createdBy = user(9997)
    createdDate = OffsetDateTime.now()
  }
}

/**
 * Retrieves a saved TaskImpl instance after saving it to the repository.
 *
 * @param repo The repository where the task will be saved.
 * @return The saved TaskImpl instance.
 */
fun getSavedTask(repo: TaskRepository): TaskImpl = saveHelperObject(repo, getTask()) as TaskImpl

/**
 * Retrieves a new TaskAssignmentImpl instance initialized with provided user details.
 *
 * @param user The user to whom the task assignment is made.
 * @return A new TaskAssignmentImpl instance.
 */
fun getTaskAssignment(user: User): TaskAssignmentImpl {
  return TaskAssignmentImpl().apply {
    activatedBy = user
    activatedDate = OffsetDateTime.now()
    status = Status.active
    candidate = Candidate().apply { id = 99999999 }
  }
}

/**
 * Retrieves a saved User instance after saving it to the repository.
 *
 * @param userRepo The repository where the user will be saved.
 * @return The saved User instance.
 */
fun getSavedUser(userRepo: UserRepository): User = userRepo.save(user())

/**
 * Retrieves a saved Candidate instance after saving it to the repository.
 *
 * @param repo The repository where the candidate will be saved.
 * @param savedUser The user associated with the candidate.
 * @return The saved Candidate instance.
 */
fun getSavedCandidate(repo: CandidateRepository, savedUser: User): Candidate {
  val candidate = getCandidate()
  candidate.apply { user = savedUser }
  return saveHelperObject(repo, candidate) as Candidate
}

/**
 * Retrieves a saved CandidateCertification instance after saving it to the repository.
 *
 * @param repo The repository where the candidate certification will be saved.
 * @return The saved CandidateCertification instance.
 */
fun getSavedCandidateCert(repo: CandidateCertificationRepository): CandidateCertification =
  saveHelperObject(repo, getCandidateCert())

/**
 * Retrieves a new CandidateCertification instance initialized with a default name.
 *
 * @return A new CandidateCertification instance.
 */
fun getCandidateCert(): CandidateCertification {
  return CandidateCertification().apply { name = "GREAT CERT" }
}

/**
 * Retrieves a new Candidate instance initialized with random data.
 *
 * @return A new Candidate instance.
 */
fun getCandidate(): Candidate {
  return Candidate().apply {
    candidateNumber = "TEMP%04d".format(Random.nextInt(10000))
    phone = "999999999"
    contactConsentPartners = true
    contactConsentRegistration = true
    status = CandidateStatus.active
    createdBy = user(1999L)
    createdDate = OffsetDateTime.now()
  }
}

/**
 * Retrieves a saved SavedList instance after saving it to the repository.
 *
 * @param savedListRepo The repository where the saved list will be saved.
 * @return The saved SavedList instance.
 */
fun getSavedList(savedListRepo: SavedListRepository): SavedList =
  saveHelperObject(savedListRepo, getSavedList())

/**
 * Retrieves a new SavedList instance initialized with default values.
 *
 * @return A new SavedList instance.
 */
fun getSavedList(): SavedList {
  return SavedList().apply {
    description = "SavedList"
    name = "SavedList"
    createdBy = user(1999L)
    createdDate = OffsetDateTime.now()
  }
}

/**
 * Retrieves a new CandidateEducation instance initialized with default values.
 *
 * @return A new CandidateEducation instance.
 */
fun getCandidateEducation(): CandidateEducation {
  return CandidateEducation().apply {
    lengthOfCourseYears = 3
    institution = "TEST INSTITUTION"
    country = Country().apply { id = 6192 } // Australia
    educationType = EducationType.Masters
  }
}

/**
 * Retrieves a saved Reaction instance after saving it to the repository.
 *
 * @param repo The repository where the reaction will be saved.
 * @return The saved Reaction instance.
 */
fun getSavedReaction(repo: ReactionRepository): Reaction = saveHelperObject(repo, getReaction())

/**
 * Retrieves a new Reaction instance initialized with default values.
 *
 * @return A new Reaction instance.
 */
fun getReaction(): Reaction {
  return Reaction().apply { emoji = "Smile" }
}

/**
 * Retrieves a new JobChat instance initialized with default values.
 *
 * @return A new JobChat instance.
 */
fun getJobChat(): JobChat {
  return JobChat().apply {
    id = 1
    type = JobChatType.JobCreatorAllSourcePartners
  }
}

/**
 * Retrieves a saved ChatPost instance after saving it to the repository.
 *
 * @param repo The repository where the chat post will be saved.
 * @return The saved ChatPost instance.
 */
fun getSavedChatPost(repo: ChatPostRepository): ChatPost = saveHelperObject(repo, getChatPost())

/**
 * Retrieves a new ChatPost instance initialized with default values.
 *
 * @return A new ChatPost instance.
 */
fun getChatPost(): ChatPost {
  return ChatPost().apply {
    jobChat = org.tctalent.server.repository.db.integrationhelp.getJobChat()
    content = "NothingChatContent"
    createdBy = user(1999L)
    createdDate = OffsetDateTime.now()
  }
}

/**
 * Retrieves a saved SurveyType instance after saving it to the repository.
 *
 * @param repo The repository where the survey type will be saved.
 * @return The saved SurveyType instance.
 */
fun getSavedSurveyType(repo: SurveyTypeRepository): SurveyType =
  saveHelperObject(repo, getSurveyType())

/**
 * Retrieves a new SurveyType instance initialized with default values.
 *
 * @return A new SurveyType instance.
 */
fun getSurveyType(): SurveyType {
  return SurveyType().apply {
    name = "IntTestSurvey"
    status = Status.active
  }
}

/**
 * Retrieves a saved SystemLanguage instance after saving it to the repository.
 *
 * @param repo The repository where the system language will be saved.
 * @return The saved SystemLanguage instance.
 */
fun getSavedSystemLanguage(repo: SystemLanguageRepository): SystemLanguage =
  saveHelperObject(repo, getSystemLanguage())

/**
 * Retrieves a new SystemLanguage instance initialized with default values.
 *
 * @return A new SystemLanguage instance.
 */
fun getSystemLanguage(): SystemLanguage {
  return SystemLanguage().apply {
    language = "en"
    label = "English"
    status = Status.active
  }
}

/**
 * Saves the provided entity to the repository and returns the saved entity.
 *
 * @param repo The repository where the entity will be saved.
 * @param entity The entity to be saved.
 * @param <T> Type of the entity.
 * @param <ID> Type of the entity's ID.
 * @return The saved entity.
 */
fun <T, ID> saveHelperObject(repo: JpaRepository<T, ID>, entity: T & Any): T =
  repo.saveAndFlush(entity)

/**
 * Retrieves a User instance initialized with default values.
 *
 * @param idToUse The optional ID to assign to the user.
 * @return A User instance.
 */
fun user(idToUse: Long? = null): User {
  return User().apply {
    id = idToUse
    username = "jo.blogs@email.com"
    firstName = "jo"
    lastName = "blogs"
    role = Role.user
    status = Status.active
    partner = PartnerImpl().apply { id = 1L } // This is TBB in dumps.
  }
}

/**
 * Retrieves a new AuditLog instance initialized with provided object reference.
 *
 * @param objRef The reference to the object associated with the audit log.
 * @return A new AuditLog instance.
 */
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

/**
 * Retrieves a new CandidateDependant instance initialized with a default name and gender.
 *
 * @return A new CandidateDependant instance.
 */
fun getCandidateDependent(): CandidateDependant {
  return CandidateDependant().apply {
    name = "James%04d".format(Random.nextInt(10000))
    gender = Gender.male
  }
}
