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

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserCacheEvictionTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PartnerRepository partnerRepository;

  @Autowired
  private CacheManager cacheManager;

  private PartnerImpl tbb;
  private PartnerImpl hias;

  @BeforeEach
  void setUp() {

    tbb = partnerRepository.findByAbbreviation("TBB").get();
    hias = partnerRepository.findByAbbreviation("HIAS").get();

    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@user.com");
    user.setRole(Role.user);
    user.setStatus(Status.active);
    user.setPartner(tbb);

    // Save user to initialize the test data
    userRepository.save(user);

    user = new User();
    user.setId(2L);
    user.setUsername("testuser2");
    user.setEmail("test2@user.com");
    user.setRole(Role.user);
    user.setStatus(Status.active);
    user.setPartner(hias);

    // Save user to initialize the test data
    userRepository.save(user);

    // calling find by user to cache testuser2
    userRepository.findByUsernameIgnoreCase("testuser2");
  }

  @AfterEach
  void tearDown() {
    // Clear the test user entries from the cache
    cacheManager.getCache("users").evict("testuser");
    cacheManager.getCache("users").evict("testuser2");
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("findByUsernameIgnoreCase should cache the user")
  void whenFindByUsernameIgnoreCase_thenUserShouldBeCached() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Assert that the user is found
    assertThat(foundUser).isNotNull();
    assertThat(foundUser.getUsername()).isEqualTo("testuser");

    // Assert that the user is cached
    User cachedUser = (User) cacheManager.getCache("users").get("testuser").get();
    assertThat(cachedUser).isEqualTo(foundUser);
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("save user should evict all cache entries")
  void whenSaveUser_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Calling save to update the user evicts the cache
    foundUser.setEmail("updated@user.com");
    userRepository.save(foundUser);

    // Verify that the cache is fully evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAll users should evict the cache")
  void whenSaveAllUser_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Calling save all to update the user should evict the cache
    foundUser.setEmail("updated@user.com");
    userRepository.saveAll(List.of(foundUser));

    // Verify that the cache is fully evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAndFlush user should evict the cache")
  void whenSaveAndFlushUser_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Call save and flush to update the user and evict the cache
    foundUser.setEmail("updated@user.com");
    userRepository.saveAndFlush(foundUser);

    // Verify that the cache is fully evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAllAndFlush user should evict the cache")
  void whenSaveAllAndFlushUser_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Call save all and flush to update the user and evict the cache
    foundUser.setEmail("updated@user.com");
    userRepository.saveAllAndFlush(List.of(foundUser));

    // Verify that the cache is fully evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete user should evict that user from the cache")
  void whenDeleteUser_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Calling delete user should evict the user from the cache
    userRepository.delete(foundUser);

    // Verify that only the deleted user is evicted from the cache
    verifyCacheIsPartiallyEvicted();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete user by id should evict the cache")
  void whenDeleteUserById_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Calling delete user by id should evict the cache
    userRepository.deleteById(foundUser.getId());

    // Verify that the cache is fully evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete all should evict the cache")
  void whenDeleteAll_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Calling delete all users should evict the cache
    userRepository.deleteAll();

    // Verify that the cache is fully evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete all users should evict the cache")
  void whenDeleteAllUsers_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Calling delete all users should evict the cache
    userRepository.deleteAll(List.of(foundUser));

    // Verify that the cache is fully evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("save partner should evict all user cache entries")
  void whenSavePartner_thenCacheShouldBeEvictedAndUpdated() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving the partner should evict the user cache
    tbb.setName("UpdatedPartner");
    partnerRepository.save(tbb);

    // Verify that the partner updated and the user cache evicted
    verifyPartnerNameUpdated(tbb.getId(), "UpdatedPartner");
    verifyCacheIsEmpty();

    // Verify that finding the user again will return and cache the updated partner
    foundUser = findUserAndVerifyCache("testuser", "UpdatedPartner");
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAll partners should evict the user cache")
  void whenSaveAllPartners_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving the partner should evict the user cache
    tbb.setName("UpdatedPartner");
    hias.setName("UpdatedHIAS");
    partnerRepository.saveAll(List.of(tbb, hias));

    // Verify that the partner updated and the user cache evicted
    verifyPartnerNameUpdated(tbb.getId(), "UpdatedPartner");
    verifyCacheIsEmpty();

    // Verify that finding the user again will return and cache the updated partner
    foundUser = findUserAndVerifyCache("testuser", "UpdatedPartner");
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAndFlush partner should evict the user cache")
  void whenSaveAndFlushPartner_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving the partner should evict the user cache
    tbb.setName("UpdatedPartner");
    partnerRepository.saveAndFlush(tbb);

    // Verify that the partner updated and the user cache evicted
    verifyPartnerNameUpdated(tbb.getId(), "UpdatedPartner");
    verifyCacheIsEmpty();

    // Verify that finding the user again will return and cache the updated partner
    foundUser = findUserAndVerifyCache("testuser", "UpdatedPartner");
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAllAndFlush partners should evict the user cache")
  void whenSaveAllAndFlushPartner_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving the partner should evict the user cache
    tbb.setName("UpdatedPartner");
    hias.setName("UpdatedHIAS");
    partnerRepository.saveAllAndFlush(List.of(tbb, hias));

    // Verify that the partner updated and the user cache evicted
    verifyPartnerNameUpdated(tbb.getId(), "UpdatedPartner");
    verifyCacheIsEmpty();

    // Verify that finding the user again will return and cache the updated partner
    foundUser = findUserAndVerifyCache("testuser", "UpdatedPartner");
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete partner should evict the user cache")
  void whenDeletePartner_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the partner should evict the user cache
    partnerRepository.delete(tbb);

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete partner by id should evict the user cache")
  void whenDeletePartnerById_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the partner should evict the user cache
    partnerRepository.deleteById(tbb.getId());

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete all partner repository should evict the user cache")
  void whenDeleteAllPartnerRepo_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the partner should evict the user cache
    partnerRepository.deleteAll();

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete all partners should evict the user cache")
  void whenDeleteAllPartners_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the partner should evict the user cache
    partnerRepository.deleteAll(List.of(tbb, hias));

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  private User findUserAndVerifyCache(String username, String expectedPartnerName) {
    User user = userRepository.findByUsernameIgnoreCase(username);
    verifyUserAndCachedEntry(user, expectedPartnerName);
    return user;
  }

  private void verifyUserAndCachedEntry(User user, String partnerName) {
    // Verify the user
    assertThat(user).isNotNull();
    assertThat(user.getPartner().getName()).isEqualTo(partnerName);

    // Verify the cached user
    User cachedUser = (User) cacheManager.getCache("users").get("testuser").get();
    assertThat(cachedUser).isEqualTo(user);
  }

  private void verifyCacheIsEmpty() {
    verifyCacheEviction("testuser");
    verifyCacheEviction("testuser2");
  }

  private void verifyCacheIsPartiallyEvicted() {
    verifyCacheEviction("testuser");
    assertThat(cacheManager.getCache("users").get("testuser2")).isNotNull();
  }

  private void verifyCacheEviction(String username) {
    assertThat(cacheManager.getCache("users").get(username)).isNull();
  }

  private void verifyPartnerNameUpdated(Long partnerId, String expectedName) {
    PartnerImpl updatedPartner = partnerRepository.findById(partnerId).get();
    assertThat(updatedPartner.getName()).isEqualTo(expectedName);
  }

}
