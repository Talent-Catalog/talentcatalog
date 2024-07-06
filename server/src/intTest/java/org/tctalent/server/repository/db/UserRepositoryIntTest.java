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

package org.tctalent.server.repository.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class UserRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private UserRepository repo;
  private User user;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    user = getSavedUser(repo);
  }

  // This also tests for not deleted!!
  @Test
  public void testFindByUsernameAndRole() {
    User u = repo.findByUsernameAndRole(user.getUsername(), user.getRole());
    assertNotNull(u);
    assertEquals(user.getId(), u.getId());

    // add in deleted just in case the query changes.
    user.setStatus(Status.deleted);
    repo.save(user);
    u = repo.findByUsernameAndRole(user.getUsername(), user.getRole());
    assertNull(u);
  }

  @Test
  public void testFindByUsernameIgnoreCase() {
    User u = repo.findByUsernameIgnoreCase(user.getUsername());
    assertNotNull(u);
    assertEquals(user.getId(), u.getId());

    // add in deleted just in case the query changes.
    user.setStatus(Status.deleted);
    repo.save(user);
    u = repo.findByUsernameIgnoreCase(user.getUsername());
    assertNull(u);
  }

  @Test
  public void testFindByEmailIgnoreCase() {
    User u = repo.findByEmailIgnoreCase(user.getEmail());
    assertNotNull(u);
    assertEquals(user.getId(), u.getId());

    user.setStatus(Status.deleted);
    repo.save(user);
    u = repo.findByEmailIgnoreCase(user.getEmail());
    assertNull(u);
  }

  @Test
  public void testFindByResetToken() {
    user.setResetToken("YES");
    repo.save(user);
    User u = repo.findByResetToken(user.getResetToken());
    assertNotNull(u);
    assertEquals(user.getId(), u.getId());

    user.setStatus(Status.deleted);
    repo.save(user);
    u = repo.findByResetToken(user.getResetToken());
    assertNull(u);
  }

  @Test
  public void testSearchAdminUsersName() {
    user.setRole(Role.admin);
    repo.save(user);
    Page<User> u = repo.searchAdminUsersName(user.getUsername(), Pageable.unpaged());
    assertNotNull(u);

    // Will be content of 1 user in the page.
    assertEquals(1, u.getContent().size());
  }

  // This is an unusual query - data modified to make the test work.
  @Test
  public void testSearchAdminUsersNameFail() {
    Page<User> u = repo.searchAdminUsersName(user.getUsername(), Pageable.unpaged());
    assertNotNull(u);
    // Will be no users in the content.
    assertEquals(0, u.getContent().size());
  }

  @Test
  public void testSearchStaffNotUsingMfa() {
    user.setRole(Role.admin);
    repo.save(user);
    List<User> u = repo.searchStaffNotUsingMfa();
    assertNotNull(u);
    assertEquals(1, u.size());
    List<Long> ids = u.stream().map(User::getId).toList();
    assertEquals(user.getId(), ids.getFirst());
  }

  @Test
  public void testSearchStaffNotUsingMfaFailUsing() {
    user.setUsingMfa(true);
    repo.save(user);
    List<User> u = repo.searchStaffNotUsingMfa();
    assertNotNull(u);
    assertTrue(u.isEmpty());
  }

  @Test
  public void testSearchStaffNotUsingMfaFailDeleted() {
    user.setStatus(Status.deleted);
    repo.save(user);
    List<User> u = repo.searchStaffNotUsingMfa();
    assertNotNull(u);
    assertTrue(u.isEmpty());
  }
}
