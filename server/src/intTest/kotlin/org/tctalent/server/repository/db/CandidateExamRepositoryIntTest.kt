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

import kotlin.jvm.optionals.getOrNull
import kotlin.test.*
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateExam
import org.tctalent.server.model.db.Exam
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getCandidateExam
import org.tctalent.server.repository.db.integrationhelp.getSavedCandidate
import org.tctalent.server.repository.db.integrationhelp.getSavedUser

class CandidateExamRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateExamRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  private lateinit var candidateExam: CandidateExam
  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository))
    candidateExam = getCandidateExam().apply { candidate = testCandidate }
    repo.save(candidateExam)
    assertTrue { candidateExam.id > 0 }
  }

  @Test
  fun `test find by id load candidate`() {
    val ce = repo.findByIdLoadCandidate(candidateExam.id).getOrNull()
    assertNotNull(ce)
    assertEquals(candidateExam.id, ce.id)
  }

  @Test
  fun `test find by id load candidate fail`() {
    val ce = repo.findByIdLoadCandidate(99999999L).getOrNull()
    assertNull(ce)
  }

  @Test
  fun `test find duplicate by exam type`() {
    val ce = repo.findDuplicateByExamType(Exam.OET, testCandidate.id, 9999999L).getOrNull()
    assertNotNull(ce)
    assertEquals(candidateExam.id, ce.id)
  }

  @Test
  fun `test find duplicate by exam type fail exam`() {
    val ce =
      repo.findDuplicateByExamType(Exam.IELTSGen, testCandidate.id, candidateExam.id).getOrNull()
    assertNull(ce)
  }

  @Test
  fun `test find duplicate by exam type fail candidate`() {
    val ce = repo.findDuplicateByExamType(Exam.OET, 99999L, candidateExam.id).getOrNull()
    assertNull(ce)
  }

  @Test
  fun `test find duplicate by exam type fail id`() {
    val ce = repo.findDuplicateByExamType(Exam.OET, testCandidate.id, candidateExam.id).getOrNull()
    assertNull(ce)
  }
}
