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

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.tctalent.server.model.db.Role
import org.tctalent.server.model.db.Status
import org.tctalent.server.model.db.User
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedUser
import org.tctalent.server.request.user.SearchUserRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserSpecificationTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: UserRepository
  private lateinit var testUser: User

  @BeforeEach
  fun setup() {
    assertTrue { isContainerInitialized() }
    testUser = getSavedUser(repo)
    assertTrue { testUser.id > 0 }
  }

  @Test
  fun `test build search query with keyword`() {
    val request = SearchUserRequest().apply { keyword = "jo" }
    val spec = UserSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(testUser.id, result[0].id)
  }

  @Test
  fun `test build search query with non-matching keyword`() {
    val request = SearchUserRequest().apply { keyword = "NonMatching" }
    val spec = UserSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `test build search query with role`() {
    val request = SearchUserRequest().apply { role = listOf(Role.admin) }
    val spec = UserSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(testUser.id, result.first().id)
  }

  @Test
  fun `test build search query excluding default role`() {
    val request = SearchUserRequest().apply { role = emptyList<Role>() }
    val spec = UserSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(testUser.id, result.first().id)
  }

  @Test
  fun `test build search query with partnerId`() {
    val request = SearchUserRequest().apply { partnerId = testUser.partner.id }

    val spec = UserSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(testUser.id, result.first().id)
  }

  @Test
  fun `test build search query with status`() {
    val request = SearchUserRequest().apply { status = Status.active }
    val spec = UserSpecification.buildSearchQuery(request)
    val result = repo.findAll(spec)
    assertNotNull(result)
    assertTrue(result.isNotEmpty())
    assertEquals(1, result.size)
    assertEquals(testUser.id, result.first().id)
  }
}
