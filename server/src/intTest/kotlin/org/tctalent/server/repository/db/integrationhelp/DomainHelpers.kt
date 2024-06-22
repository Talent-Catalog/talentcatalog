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
fun getTask(taskName: String = "DEFAULT", taskDisplay: String = "DEFAULT DISPLAY") =
  TaskImpl().apply {
    name = taskName
    displayName = taskDisplay
    createdBy = systemUser()
    createdDate = OffsetDateTime.now()
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
fun getTaskAssignment(user: User) =
  TaskAssignmentImpl().apply {
    activatedBy = user
    activatedDate = OffsetDateTime.now()
    status = Status.active
    candidate = Candidate().apply { id = 99999999 }
  }

/**
 * Retrieves a saved User instance after saving it to the repository.
 *
 * @param userRepo The repository where the user will be saved.
 * @return The saved User instance.
 */
fun getSavedUser(userRepo: UserRepository): User = userRepo.save(getUser())

/**
 * Retrieves a saved Candidate instance after saving it to the repository.
 *
 * @param repo The repository where the candidate will be saved.
 * @param savedUser The user associated with the candidate.
 * @return The saved Candidate instance.
 */
fun getSavedCandidate(repo: CandidateRepository, savedUser: User): Candidate {
  val candidate = getCandidate().apply { user = savedUser }
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
fun getCandidateCert() = CandidateCertification().apply { name = "GREAT CERT" }

/**
 * Retrieves a new Candidate instance initialized with random data.
 *
 * @return A new Candidate instance.
 */
fun getCandidate() =
  Candidate().apply {
    candidateNumber = "TEMP%04d".format(Random.nextInt(10000))
    phone = "999999999"
    contactConsentPartners = true
    contactConsentRegistration = true
    workAbroadNotes = "GOOD FOR TEST"
    status = CandidateStatus.active
    createdBy = systemUser()
    createdDate = OffsetDateTime.now()
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
fun getSavedList() =
  SavedList().apply {
    description = "SavedList"
    name = "SavedList"
    createdBy = systemUser()
    createdDate = OffsetDateTime.now()
  }

/**
 * Retrieves a new CandidateEducation instance initialized with default values.
 *
 * @return A new CandidateEducation instance.
 */
fun getCandidateEducation() =
  CandidateEducation().apply {
    lengthOfCourseYears = 3
    institution = "TEST INSTITUTION"
    country = Country().apply { id = 6192 } // Australia
    educationType = EducationType.Masters
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
fun getReaction() = Reaction().apply { emoji = "Smile" }

/**
 * Retrieves a new JobChat instance initialized with default values.
 *
 * @return A new JobChat instance.
 */
fun getJobChat() =
  JobChat().apply {
    type = JobChatType.JobCreatorAllSourcePartners
    createdBy = systemUser()
    createdDate = OffsetDateTime.now()
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
fun getChatPost() =
  ChatPost().apply {
    content = "NothingChatContent"
    createdBy = systemUser()
    createdDate = OffsetDateTime.now()
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
fun getSurveyType() =
  SurveyType().apply {
    name = "IntTestSurvey"
    status = Status.active
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
fun getSystemLanguage() =
  SystemLanguage().apply {
    language = "en"
    label = "English"
    status = Status.active
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
fun getUser(idToUse: Long? = null) =
  User().apply {
    id = idToUse
    username = "jo.blogs@email.com"
    firstName = "jo"
    lastName = "blogs"
    role = Role.user
    status = Status.active
    partner = PartnerImpl().apply { id = 1L } // This is TBB in the dump.
  }

/**
 * Retrieves a new AuditLog instance initialized with provided object reference.
 *
 * @param objRef The reference to the object associated with the audit log.
 * @return A new AuditLog instance.
 */
fun getAuditLog(objRef: String) =
  AuditLog().apply {
    type = AuditType.CANDIDATE_OCCUPATION
    userId = 9L
    objectRef = objRef
    eventDate = OffsetDateTime.now()
    action = AuditAction.ADD
    description = "Create a test audit record."
  }

/**
 * Retrieves a new CandidateDependant instance initialized with a default name and gender.
 *
 * @return A new CandidateDependant instance.
 */
fun getCandidateDependent() =
  CandidateDependant().apply {
    name = "James%04d".format(Random.nextInt(10000))
    gender = Gender.male
  }

/**
 * Retrieves a new SalesforceJobOpp instance initialized with default values.
 *
 * @return A new SalesforceJobOpp instance.
 */
fun getSalesforceJobOpp() =
  SalesforceJobOpp().apply {
    description = "SF TEST JOB"
    employer = "Seraco Pty Ltd"
    country = Country().apply { id = 6192 } // Australia
    sfId = "TESTSFID"
    createdBy = systemUser()
    createdDate = OffsetDateTime.now()
  }

/**
 * Retrieves a saved JobChat instance after saving it to the repository.
 *
 * @param repo The repository where the job chat will be saved.
 * @return The saved JobChat instance.
 */
fun getSavedJobChat(repo: JobChatRepository): JobChat = saveHelperObject(repo, getJobChat())

/**
 * Retrieves a saved JobChatUser instance after saving it to the repository.
 *
 * @param repository The repository where the job chat user will be saved.
 * @param savedUser The user associated with the job chat.
 * @param savedJobChat The job chat associated with the user.
 * @return The saved JobChatUser instance.
 */
fun getSavedJobChatUser(
  repository: JobChatUserRepository,
  savedUser: User,
  savedJobChat: JobChat,
): JobChatUser = saveHelperObject(repository, getJobChatUser(savedUser, savedJobChat))

/**
 * Retrieves a new JobChatUser instance initialized with provided user and job chat.
 *
 * @param savedUser The user associated with the job chat.
 * @param savedChat The job chat associated with the user.
 * @return A new JobChatUser instance.
 */
fun getJobChatUser(savedUser: User, savedChat: JobChat): JobChatUser {
  val key = getJobChatUserKey(savedUser, savedChat)
  return JobChatUser().apply {
    id = key
    chat = savedChat
    user = savedUser
  }
}

/**
 * Retrieves a new JobChatUserKey instance initialized with provided user and job chat IDs.
 *
 * @param savedUser The user associated with the job chat.
 * @param savedChat The job chat associated with the user.
 * @return A new JobChatUserKey instance.
 */
fun getJobChatUserKey(savedUser: User, savedChat: JobChat) =
  JobChatUserKey().apply {
    userId = savedUser.id
    jobChatId = savedChat.id
  }

/**
 * Retrieves a saved Partner instance after saving it to the repository.
 *
 * @param repository The repository where the partner will be saved.
 * @return The saved Partner instance.
 */
fun getSavedPartner(repository: PartnerRepository): PartnerImpl =
  saveHelperObject(repository, getPartner())

/**
 * Retrieves a new PartnerImpl instance initialized with a default name.
 *
 * @return A new PartnerImpl instance.
 */
fun getPartner() =
  PartnerImpl().apply {
    name = "GREAT TEST PARTNER"
    status = Status.active
    abbreviation = "GTP"
    isDefaultSourcePartner = false
    isAutoAssignable = true
  }

/**
 * Retrieves a saved Country instance after saving it to the repository.
 *
 * @param repo The repository where the country will be saved.
 * @return The saved Country instance.
 */
fun getSavedCountry(repo: CountryRepository): Country = saveHelperObject(repo, getCountry())

/**
 * Retrieves a new Country instance initialized with default ISO code and name.
 *
 * @return A new Country instance.
 */
fun getCountry() =
  Country().apply {
    isoCode = "ISOCODE"
    name = "NewAustralia"
    status = Status.active
  }

/**
 * Retrieves a new CandidateVisaCheck instance initialized with default protection status.
 *
 * @return A new CandidateVisaCheck instance.
 */
fun getCandidateVisaCheck() = CandidateVisaCheck().apply { protection = YesNo.Yes }

/**
 * Retrieves a saved Industry instance after saving it to the repository.
 *
 * @param repo The repository where the industry will be saved.
 * @return The saved Industry instance.
 */
fun getSavedIndustry(repo: IndustryRepository): Industry = saveHelperObject(repo, getIndustry())

/**
 * Retrieves a new Industry instance initialized with default status and name.
 *
 * @return A new Industry instance.
 */
fun getIndustry() =
  Industry().apply {
    status = Status.active
    name = "TestIndustry"
  }

/**
 * Retrieves a new CandidateVisaJobCheck instance initialized with default name and interest status.
 *
 * @return A new CandidateVisaJobCheck instance.
 */
fun getCandidateVisaJobCheck() =
  CandidateVisaJobCheck().apply {
    name = "TestCandidateVisaJobCheck"
    interest = YesNo.Yes
  }

/**
 * Retrieves a new CandidateReviewStatusItem instance initialized with a default comment and review
 * status.
 *
 * @return A new CandidateReviewStatusItem instance.
 */
fun getCandidateReviewStatusItem(): CandidateReviewStatusItem =
  CandidateReviewStatusItem().apply {
    comment = "TestCandidateReviewStatusItem"
    reviewStatus = ReviewStatus.verified
  }

/**
 * Retrieves a saved SavedSearch instance after saving it to the repository.
 *
 * @param repository The repository where the saved search will be saved.
 * @return The saved SavedSearch instance.
 */
fun getSavedSavedSearch(repository: SavedSearchRepository): SavedSearch =
  saveHelperObject(repository, getSavedSearch())

/**
 * Retrieves a new SavedSearch instance initialized with a default type and status and name.
 *
 * @return A new SavedSearch instance.
 */
fun getSavedSearch(): SavedSearch =
  SavedSearch().apply {
    type = "TestSavedSearch"
    status = Status.active
    name = "TestSavedSearch"
    createdBy = systemUser()
    createdDate = OffsetDateTime.now()
  }

/**
 * Retrieves a new CandidateOpportunity instance initialized with default stage and closing
 * comments.
 *
 * @return A new CandidateOpportunity instance.
 */
fun getCandidateOpportunity(): CandidateOpportunity =
  CandidateOpportunity().apply {
    stage = CandidateOpportunityStage.cvPreparation
    closingCommentsForCandidate = "WELLDONE"
  }

/**
 * Retrieves a saved SalesforceJobOpp instance after saving it to the repository.
 *
 * @param repo The repository where the Salesforce job opportunity will be saved.
 * @return The saved SalesforceJobOpp instance.
 */
fun getSavedSfJobOpp(repo: SalesforceJobOppRepository): SalesforceJobOpp =
  saveHelperObject(repo, getSalesforceJobOpp())

/**
 * Retrieves a User instance with a predefined ID.
 *
 * @return A User instance with ID 25000.
 */
fun systemUser(): User = getUser(25000)

/**
 * Retrieves a saved LanguageLevel instance after saving it to the repository.
 *
 * @param repo The repository where the language level will be saved.
 * @return The saved LanguageLevel instance.
 */
fun getSavedLanguageLevel(repo: LanguageLevelRepository): LanguageLevel =
  saveHelperObject(repo, getLanguageLevel())

/**
 * Retrieves a new LanguageLevel instance initialized with default level, status, and name.
 *
 * @return A new LanguageLevel instance.
 */
fun getLanguageLevel(): LanguageLevel =
  LanguageLevel().apply {
    level = 1
    status = Status.active
    name = "VERY_HIGH_LEVEL"
  }

/**
 * Retrieves a new Occupation instance initialized with a default status and name.
 *
 * @return A new Occupation instance.
 */
fun getOccupation(): Occupation =
  Occupation().apply {
    status = Status.active
    name = "TEST_OCCUPATION%04d".format(Random.nextInt(10000))
  }

/**
 * Retrieves a saved Occupation instance after saving it to the repository.
 *
 * @param repo The repository where the occupation will be saved.
 * @return The saved Occupation instance.
 */
fun getSavedOccupation(repo: OccupationRepository): Occupation =
  saveHelperObject(repo, getOccupation())

fun getSavedLanguage(repo: LanguageRepository) = saveHelperObject(repo, getLanguage())

fun getLanguage() =
  Language().apply {
    status = Status.active
    name = "TEST_LANGUAGE%04d".format(Random.nextInt(10000))
  }
