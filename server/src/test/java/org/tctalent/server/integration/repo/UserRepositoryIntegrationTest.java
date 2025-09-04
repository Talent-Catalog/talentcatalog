package org.tctalent.server.integration.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.tctalent.server.integration.helper.BaseJpaIntegrationTest;
import org.tctalent.server.integration.helper.PostgresTestContainer;
import org.tctalent.server.integration.helper.TestDataFactory;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.UserRepository;

/**
 * Integration tests for UserRepository, verifying user retrieval and search functionality.
 */
public class UserRepositoryIntegrationTest extends BaseJpaIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  private User testUser;

  @BeforeAll
  public static void setup() throws IOException, InterruptedException {
    PostgresTestContainer.startContainer();
  }

  @BeforeEach
  public void setUp() {
    assertTrue(isContainerInitialised(), "Database container should be initialized");
    testUser = TestDataFactory.createAndSaveUser(userRepository);
  }

  /**
   * Tests finding an active user by username and role, and ensures deleted users are not returned.
   */
  @Test
  public void shouldFindActiveUserByUsernameAndRole() {
    User foundUser = userRepository.findByUsernameAndRole(testUser.getUsername(), testUser.getRole());
    assertNotNull(foundUser, "User should be found");
    assertEquals(testUser.getId(), foundUser.getId(), "User IDs should match");

    // Mark user as deleted and verify it is not returned
    testUser.setStatus(Status.deleted);
    userRepository.save(testUser);
    foundUser = userRepository.findByUsernameAndRole(testUser.getUsername(), testUser.getRole());
    assertNull(foundUser, "Deleted user should not be found");
  }

  /**
   * Tests finding an active user by username (case-insensitive), and ensures deleted users are not returned.
   */
  @Test
  public void shouldFindActiveUserByUsernameIgnoreCase() {
    User foundUser = userRepository.findByUsernameIgnoreCase(testUser.getUsername());
    assertNotNull(foundUser, "User should be found");
    assertEquals(testUser.getId(), foundUser.getId(), "User IDs should match");

    // Mark user as deleted and verify it is not returned
    testUser.setStatus(Status.deleted);
    userRepository.save(testUser);
    foundUser = userRepository.findByUsernameIgnoreCase(testUser.getUsername());
    assertNull(foundUser, "Deleted user should not be found");
  }

  /**
   * Tests finding an active user by email (case-insensitive), and ensures deleted users are not returned.
   */
  @Test
  public void shouldFindActiveUserByEmailIgnoreCase() {
    User foundUser = userRepository.findByEmailIgnoreCase(testUser.getEmail());
    assertNotNull(foundUser, "User should be found");
    assertEquals(testUser.getId(), foundUser.getId(), "User IDs should match");

    // Mark user as deleted and verify it is not returned
    testUser.setStatus(Status.deleted);
    userRepository.save(testUser);
    foundUser = userRepository.findByEmailIgnoreCase(testUser.getEmail());
    assertNull(foundUser, "Deleted user should not be found");
  }

  /**
   * Tests finding an active user by reset token, and ensures deleted users are not returned.
   */
  @Test
  public void shouldFindActiveUserByResetToken() {
    testUser.setResetToken("YES");
    userRepository.save(testUser);
    User foundUser = userRepository.findByResetToken(testUser.getResetToken());
    assertNotNull(foundUser, "User should be found");
    assertEquals(testUser.getId(), foundUser.getId(), "User IDs should match");

    // Mark user as deleted and verify it is not returned
    testUser.setStatus(Status.deleted);
    userRepository.save(testUser);
    foundUser = userRepository.findByResetToken(testUser.getResetToken());
    assertNull(foundUser, "Deleted user should not be found");
  }

  /**
   * Tests searching for admin users by username, expecting a single user in the result.
   */
  @Test
  public void shouldFindAdminUsersByUsername() {
    testUser.setRole(Role.admin);
    userRepository.save(testUser);
    Page<User> foundUsers = userRepository.searchAdminUsersName(testUser.getUsername(), Pageable.unpaged());
    assertNotNull(foundUsers, "Search result should not be null");
    assertEquals(1, foundUsers.getContent().size(), "Should find exactly one admin user");
  }

  /**
   * Tests searching for admin users by username when no admin users match, expecting an empty result.
   */
  @Test
  public void shouldReturnEmptyResultForNonAdminUserSearch() {
    Page<User> foundUsers = userRepository.searchAdminUsersName(testUser.getUsername(), Pageable.unpaged());
    assertNotNull(foundUsers, "Search result should not be null");
    assertEquals(0, foundUsers.getContent().size(), "Should find no admin users");
  }
}