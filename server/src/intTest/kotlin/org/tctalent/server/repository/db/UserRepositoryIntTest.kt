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

import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.tctalent.server.model.db.Role
import org.tctalent.server.model.db.Status
import org.tctalent.server.model.db.User
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest
import org.tctalent.server.repository.db.integrationhelp.getSavedUser
import kotlin.test.*

class UserRepositoryIntTest : BaseDBIntegrationTest() {
  @Autowired lateinit var repo: UserRepository
  private lateinit var user: User

  @BeforeTest
  fun setup() {
    assertTrue { isContainerInitialized() }
    user = getSavedUser(repo)
  }

  // This also tests for not deleted!!
  @Test
  fun `test find by username and role`() {
    var u = repo.findByUsernameAndRole(user.username, user.role)
    assertNotNull(u)
    assertEquals(user.id, u.id)

    // add in deleted just in case the query changes.
    repo.save(user.apply { status = Status.deleted })
    u = repo.findByUsernameAndRole(user.username, user.role)
    assertNull(u)
  }

  @Test
  fun `test find by username ignorecase`() {
    var u = repo.findByUsernameIgnoreCase(user.username)
    assertNotNull(u)
    assertEquals(user.id, u.id)

    // add in deleted just in case the query changes.
    repo.save(user.apply { status = Status.deleted })
    u = repo.findByUsernameIgnoreCase(user.username)
    assertNull(u)
  }

  @Test
  fun `test find by email ignorecase`() {
    var u = repo.findByEmailIgnoreCase(user.email)
    assertNotNull(u)
    assertEquals(user.id, u.id)

    repo.save(user.apply { status = Status.deleted })
    u = repo.findByEmailIgnoreCase(user.email)
    assertNull(u)
  }

  @Test
  fun `test find by reset token`() {
    repo.save(user.apply { resetToken = "YES" })
    var u = repo.findByResetToken(user.resetToken)
    assertNotNull(u)
    assertEquals(user.id, u.id)

    repo.save(user.apply { status = Status.deleted })
    u = repo.findByResetToken(user.resetToken)
    assertNull(u)
  }

  @Test
  fun `test search admin users name`() {
    repo.save(user.apply { role = Role.admin })
    val u = repo.searchAdminUsersName(user.username, Pageable.unpaged())
    assertNotNull(u)

    // Will be content of 1 user in the page.
    assertEquals(1, u.content.size)
  }

  // This is an unusual query - data modified to make the test work.
  @Test
  fun `test search admin users name fail`() {
    val u = repo.searchAdminUsersName(user.username, Pageable.unpaged())
    assertNotNull(u)
    // Will be no users in the content.
    assertEquals(0, u.content.size)
  }

  @Test
  fun `test search staff not using mfa`() {
    repo.save(user.apply { role = Role.admin })
    val u = repo.searchStaffNotUsingMfa()
    assertNotNull(u)
    assertEquals(1, u.size)
    val ids = u.map { it.id }
    assertEquals(user.id, ids.first())
  }

  @Test
  fun `test search staff not using mfa fail using`() {
    repo.save(user.apply { usingMfa = true })
    val u = repo.searchStaffNotUsingMfa()
    assertNotNull(u)
    assertTrue { u.isEmpty() }
  }

  @Test
  fun `test search staff not using mfa fail deleted`() {
    repo.save(user.apply { status = Status.deleted })
    val u = repo.searchStaffNotUsingMfa()
    assertNotNull(u)
    assertTrue { u.isEmpty() }
  }
}
