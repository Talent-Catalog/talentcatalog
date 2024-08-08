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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.repository.db.integrationhelp.DomainHelpers;
import org.tctalent.server.request.user.SearchUserRequest;

public class UserSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private UserRepository repo;
  private User testUser;
  private SearchUserRequest request;
  Specification<User> spec;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    request = new SearchUserRequest();
    testUser = DomainHelpers.getUser();
    testUser.setRole(Role.admin);
    repo.save(testUser);
    assertTrue(testUser.getId() > 0);
  }

  @Test
  public void testKeyword() {
    request.setKeyword("jo");
    spec = UserSpecification.buildSearchQuery(request);

    List<User> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(testUser.getId(), results.getFirst().getId());
  }

  @Test
  public void testKeyWordNonMatching() {
    request.setKeyword("NonMatching");
    spec = UserSpecification.buildSearchQuery(request);

    List<User> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testWithRole() {
    testUser.setRole(Role.user);
    List<Role> list = new ArrayList<>();
    list.add(Role.user);
    request.setRole(list);
    spec = UserSpecification.buildSearchQuery(request);

    List<User> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(testUser.getId(), results.getFirst().getId());
  }

  @Test
  public void testWithNoDefaultRole() {
    request.setRole(new ArrayList<>());
    spec = UserSpecification.buildSearchQuery(request);

    List<User> results = repo.findAll(spec);

    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(2, results.size());
    assertEquals(testUser.getId(), results.getLast().getId());
  }

  @Test
  public void testWithPartnerId() {
    request.setPartnerId(testUser.getPartner().getId());
    spec = UserSpecification.buildSearchQuery(request);

    List<User> results = repo.findAll(spec);

    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(2, results.size());
    assertEquals(testUser.getId(), results.getLast().getId());
  }

  @Test
  public void testWithStatus() {
    request.setStatus(Status.active);
    spec = UserSpecification.buildSearchQuery(request);

    List<User> results = repo.findAll(spec);

    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(2, results.size());
    assertEquals(testUser.getId(), results.getLast().getId());
  }

  @Test
  public void testWithStatusFail() {
    request.setStatus(Status.deleted);
    spec = UserSpecification.buildSearchQuery(request);

    List<User> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }
}
