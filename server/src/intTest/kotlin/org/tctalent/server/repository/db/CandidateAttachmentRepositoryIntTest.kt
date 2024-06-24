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
import org.springframework.data.domain.Pageable
import org.tctalent.server.model.db.Candidate
import org.tctalent.server.model.db.CandidateAttachment
import org.tctalent.server.model.db.task.UploadType
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getCandidateAttachment
import org.tctalent.server.repository.db.integrationhelp.getSavedCandidate
import org.tctalent.server.repository.db.integrationhelp.getSavedUser

class CandidateAttachmentRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: CandidateAttachmentRepository
  @Autowired lateinit var candidateRepository: CandidateRepository
  @Autowired lateinit var userRepository: UserRepository
  private lateinit var candidateAttachment: CandidateAttachment
  private lateinit var testCandidate: Candidate

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    testCandidate = getSavedCandidate(candidateRepository, getSavedUser(userRepository))
    candidateAttachment = getCandidateAttachment().apply { candidate = testCandidate }
    repo.save(candidateAttachment)
    assertTrue { candidateAttachment.id > 0 }
  }

  @Test
  fun `test find by candidate id load audit`() {
    // create a second one so we can check order.
    val newCA = getCandidateAttachment().apply { candidate = testCandidate }
    repo.save(newCA)
    assertTrue { newCA.id > 0 }
    val results = repo.findByCandidateIdLoadAudit(testCandidate.id)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    println(candidateAttachment.createdDate)
    println(newCA.createdDate)
    assertEquals(newCA.id, results.first().id)
  }

  @Test
  fun `test find by candidate id load audit fail`() {
    // create a second one so we can check order.
    val newCA = getCandidateAttachment().apply { candidate = testCandidate }
    repo.save(newCA)
    assertTrue { newCA.id > 0 }
    val results = repo.findByCandidateIdLoadAudit(0L)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test find by candidate id and type`() {
    val results = repo.findByCandidateIdAndType(testCandidate.id, UploadType.idCard)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertTrue { results.all { it.id == candidateAttachment.id } }
  }

  @Test
  fun `test find by candidate id and type fail id`() {
    val results = repo.findByCandidateIdAndType(null, UploadType.idCard)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test find by candidate id and type fail type`() {
    val results = repo.findByCandidateIdAndType(testCandidate.id, UploadType.cv)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test find by candidate id pageable`() {
    val results = repo.findByCandidateId(testCandidate.id, Pageable.unpaged())
    assertNotNull(results)
    assertTrue { results.content.isNotEmpty() }
    assertEquals(1, results.content.size)
    assertTrue { results.content.all { i -> i.id == candidateAttachment.id } }
  }

  @Test
  fun `test find by candidate id pageable fail id`() {
    val results = repo.findByCandidateId(null, Pageable.unpaged())
    assertNotNull(results)
    assertTrue { results.content.isEmpty() }
  }

  @Test
  fun `test find by candidate id`() {
    val results = repo.findByCandidateId(testCandidate.id)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertTrue { results.all { it.id == candidateAttachment.id } }
  }

  @Test
  fun `test find by candidate id fail`() {
    val results = repo.findByCandidateId(9999L)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test find by candidate id and cv false`() {
    val results = repo.findByCandidateIdAndCv(testCandidate.id, false)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertTrue { results.all { it.id == candidateAttachment.id } }
  }

  @Test
  fun `test find by candidate id and cv true`() {
    repo.save(
      candidateAttachment.apply {
        uploadType = UploadType.cv
        isCv = true
      }
    )
    val results = repo.findByCandidateIdAndCv(testCandidate.id, true)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertTrue { results.all { it.id == candidateAttachment.id } }
  }

  @Test
  fun `test find by candidate id and cv fail`() {
    repo.save(candidateAttachment.apply { uploadType = UploadType.cv })
    val results = repo.findByCandidateIdAndCv(999999L, true)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test find by id load candidate`() {
    val results = repo.findByIdLoadCandidate(candidateAttachment.id).getOrNull()
    assertNotNull(results)
    assertEquals(candidateAttachment.id, results.id)
  }

  @Test
  fun `test find by id load candidate fail`() {
    val results = repo.findByIdLoadCandidate(null).getOrNull()
    assertNull(results)
  }

  // I don't think class this is used?
  @Test
  fun `test find by file type`() {
    // No enum for filetype.
    val results = repo.findByFileType("pdf")
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertEquals(candidateAttachment.id, results.first().id)
  }

  @Test
  fun `test find by file type fail`() {
    // No enum for filetype.
    val results = repo.findByFileType("PNG")
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }

  @Test
  fun `test find by file types and migrated`() {
    val results = repo.findByFileTypesAndMigrated(listOf("pdf"), true)
    assertNotNull(results)
    assertTrue { results.isNotEmpty() }
    assertEquals(1, results.size)
    assertEquals(candidateAttachment.id, results.first().id)
  }

  @Test
  fun `test find by file types and migrated failed`() {
    val results = repo.findByFileTypesAndMigrated(null, true)
    assertNotNull(results)
    assertTrue { results.isEmpty() }
  }
}
