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
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class contains unit tests for verifying cache eviction behaviour in the users cache.
 *
 * <p>Annotations:
 * <ul>
 *   <li>@SpringBootTest - Loads the full application context for integration testing with a local
 *   database</li>
 *   <li>@Transactional - Ensures each test runs within a transaction that can be rolled back after
 *   the test</li>
 *   <li>@Rollback - Explicitly rolls back transactions between tests</li>
 * </ul>
 */
@SpringBootTest
class UserCacheEvictionTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PartnerRepository partnerRepository;

  @Autowired
  private EmployerRepository employerRepository;

  @Autowired
  private CountryRepository countryRepository;

  @Autowired
  private CandidateRepository candidateRepository;

  @Autowired
  private CacheManager cacheManager;

  private PartnerImpl tbb;
  private PartnerImpl hias;
  private Employer employer;
  private Country country;

  @BeforeEach
  void setUp() {

    tbb = partnerRepository.findByAbbreviation("TBB").get();
    hias = partnerRepository.findByAbbreviation("HIAS").get();
    employer = employerRepository.findById(1L).get();
    country = countryRepository.findByNameIgnoreCase("United Kingdom");

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

  @Test
  @Transactional
  @Rollback
  @DisplayName("save employer should evict all user cache entries")
  void whenSaveEmployer_thenCacheShouldBeEvictedAndUpdated() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving the employer should evict the user cache
    employer.setName("UpdatedEmployer");
    employerRepository.save(employer);

    // Verify that the employer updated and the user cache evicted
    verifyEmployerNameUpdated(tbb.getId(), "UpdatedEmployer");
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAll employers should evict the user cache")
  void whenSaveAllEmployers_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving the employer should evict the user cache
    employer.setName("UpdatedEmployer");
    employerRepository.saveAll(List.of(employer));

    // Verify that the employer updated and the user cache evicted
    verifyEmployerNameUpdated(tbb.getId(), "UpdatedEmployer");
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAndFlush employer should evict the user cache")
  void whenSaveAndFlushEmployer_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving the employer should evict the user cache
    employer.setName("UpdatedEmployer");
    employerRepository.saveAndFlush(employer);

    // Verify that the employer updated and the user cache evicted
    verifyEmployerNameUpdated(tbb.getId(), "UpdatedEmployer");
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAllAndFlush employers should evict the user cache")
  void whenSaveAllAndFlushEmployers_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving the employer should evict the user cache
    employer.setName("UpdatedEmployer");
    employerRepository.saveAllAndFlush(List.of(employer));

    // Verify that the employer updated and the user cache evicted
    verifyEmployerNameUpdated(tbb.getId(), "UpdatedEmployer");
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete employer should evict the user cache")
  void whenDeleteEmployer_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the employer should evict the user cache
    employerRepository.delete(employer);

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete employer by id should evict the user cache")
  void whenDeleteEmployerById_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the employer should evict the user cache
    employerRepository.deleteById(employer.getId());

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete all employer repository should evict the user cache")
  void whenDeleteAllEmployerRepo_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the employer should evict the user cache
    employerRepository.deleteAll();

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete all employers should evict the user cache")
  void whenDeleteAllEmployers_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the employer should evict the user cache
    employerRepository.deleteAll(List.of(employer));

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("save country should evict all user cache entries")
  void whenSaveCountry_thenCacheShouldBeEvictedAndUpdated() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving country should evict the user cache
    country.setName("UpdatedCountry");
    countryRepository.save(country);

    // Verify that the country updated and the user cache evicted
    verifyCountryNameUpdated(country.getId(), "UpdatedCountry");
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAll countries should evict the user cache")
  void whenSaveAllCountries_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving countries should evict the user cache
    country.setName("UpdatedCountry");
    countryRepository.saveAll(List.of(country));

    // Verify that the country updated and the user cache evicted
    verifyCountryNameUpdated(country.getId(), "UpdatedCountry");
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAndFlush country should evict the user cache")
  void whenSaveAndFlushCountry_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving country should evict the user cache
    country.setName("UpdatedCountry");
    countryRepository.saveAndFlush(country);

    // Verify that the country updated and the user cache evicted
    verifyCountryNameUpdated(country.getId(), "UpdatedCountry");
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAllAndFlush countries should evict the user cache")
  void whenSaveAllAndFlushCountries_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Updating and saving countries should evict the user cache
    country.setName("UpdatedCountry");
    countryRepository.saveAllAndFlush(List.of(country));

    // Verify that the country updated and the user cache evicted
    verifyCountryNameUpdated(country.getId(), "UpdatedCountry");
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete country should evict the user cache")
  void whenDeleteCountry_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the country should evict the user cache
    countryRepository.delete(country);

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete country by id should evict the user cache")
  void whenDeleteCountryById_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the country should evict the user cache
    countryRepository.deleteById(country.getId());

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete all country repository should evict the user cache")
  void whenDeleteAllCountryRepo_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the country should evict the user cache
    countryRepository.deleteAll();

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete all countries should evict the user cache")
  void whenDeleteAllCountries_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("testuser", "Talent Beyond Boundaries");

    // Deleting the counties should evict the user cache
    countryRepository.deleteAll(List.of(country));

    // Verify that the user cache evicted
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("save candidate should evict connected user from cache")
  void whenSaveCandidate_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("sadat.malik@test.org", "Talent Beyond Boundaries");

    // Calling save to update the candidate evicts the connected user from cache
    Candidate candidate = foundUser.getCandidate();
    candidate.setCity("Metropolis");
    candidateRepository.save(candidate);

    // Verify that the candidate updated and the user was evicted from cache
    verifyCandidateCityUpdated(candidate.getId(), "Metropolis");
    verifySingleUserEvictedFromCache(foundUser);

    // Verify that finding the user again will return and cache the updated candidate
    foundUser = findUserAndVerifyCache("sadat.malik@test.org", candidate);
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAll candidates should evict the user cache")
  void whenSaveAllCandidates_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("sadat.malik@test.org", "Talent Beyond Boundaries");

    // Calling saveAll to update the candidate clears the cache
    Candidate candidate = foundUser.getCandidate();
    candidate.setCity("Metropolis");
    candidateRepository.saveAll(List.of(candidate));

    // Verify that the candidate updated and cache was cleared
    verifyCandidateCityUpdated(candidate.getId(), "Metropolis");
    verifyCacheIsEmpty();

    // Verify that finding the user again will return and cache the updated candidate
    foundUser = findUserAndVerifyCache("sadat.malik@test.org", candidate);
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAndFlush candidate should evict the connected user from the cache")
  void whenSaveAndFlushCandidate_thenConnectedUserShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("sadat.malik@test.org", "Talent Beyond Boundaries");

    // Calling saveAndFlush to update the candidate should evict the connected user from cache
    Candidate candidate = foundUser.getCandidate();
    candidate.setCity("Metropolis");
    candidateRepository.saveAndFlush(candidate);

    // Verify that the candidate updated and the user was evicted from cache
    verifyCandidateCityUpdated(candidate.getId(), "Metropolis");
    verifySingleUserEvictedFromCache(foundUser);

    // Verify that finding the user again will return and cache the updated candidate
    foundUser = findUserAndVerifyCache("sadat.malik@test.org", candidate);
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("saveAllAndFlush candidates should evict the user cache")
  void whenSaveAllAndFlushCandidates_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("sadat.malik@test.org", "Talent Beyond Boundaries");

    // Calling saveAllAndFlush to update the candidate should clear the user cache
    Candidate candidate = foundUser.getCandidate();
    candidate.setCity("Metropolis");
    candidateRepository.saveAllAndFlush(List.of(candidate));

    // Verify that the candidate updated and cache was cleared
    verifyCandidateCityUpdated(candidate.getId(), "Metropolis");
    verifyCacheIsEmpty();

    // Verify that finding the user again will return and cache the updated candidate
    foundUser = findUserAndVerifyCache("sadat.malik@test.org", candidate);
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete candidate should evict the connected user from cache")
  void whenDeleteCandidate_thenConnectedUserShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("sadat.malik@test.org", "Talent Beyond Boundaries");

    // Calling delete candidate should evict the connected user from cache
    Candidate candidate = foundUser.getCandidate();
    candidateRepository.delete(candidate);

    // Verify that the connected user was evicted from cache
    verifySingleUserEvictedFromCache(foundUser);
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete candidate by id should evict the user cache")
  void whenDeleteCandidateById_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("sadat.malik@test.org", "Talent Beyond Boundaries");

    // Calling delete candidate by id should clear the user cache
    Candidate candidate = foundUser.getCandidate();
    candidateRepository.deleteById(candidate.getId());

    // Verify that the cache was cleared
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete all candidate repository should evict the user cache")
  void whenDeleteAllCandidateRepo_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("sadat.malik@test.org", "Talent Beyond Boundaries");

    // Calling delete all on candidate repository should clear the user cache
    candidateRepository.deleteAll();

    // Verify that the cache was cleared
    verifyCacheIsEmpty();
  }

  @Test
  @Transactional
  @Rollback
  @DisplayName("delete all candidates should evict the user cache")
  void whenDeleteAllCandidates_thenCacheShouldBeEvicted() {
    // Find the user to cache it initially
    User foundUser = findUserAndVerifyCache("sadat.malik@test.org", "Talent Beyond Boundaries");

    // Calling delete all candidate should clear the user cache
    Candidate candidate = foundUser.getCandidate();
    candidateRepository.deleteAll(List.of(candidate));

    // Verify that the cache was cleared
    verifyCacheIsEmpty();
  }

  private User findUserAndVerifyCache(String username, String expectedPartnerName) {
    User user = userRepository.findByUsernameIgnoreCase(username);
    verifyUserAndCachedEntry(user, expectedPartnerName);
    return user;
  }

  private User findUserAndVerifyCache(String username, Candidate expectedCandidate) {
    User user = userRepository.findByUsernameIgnoreCase(username);
    verifyUserAndCachedEntry(user, expectedCandidate);
    return user;
  }

  private void verifyUserAndCachedEntry(User user, String partnerName) {
    // Verify the user
    assertThat(user).isNotNull();
    assertThat(user.getPartner().getName()).isEqualTo(partnerName);

    // Verify the cached user
    User cachedUser = (User) cacheManager.getCache("users").get(user.getUsername()).get();
    assertThat(cachedUser).isEqualTo(user);
  }

  private void verifyUserAndCachedEntry(User user, Candidate candidate) {
    // Verify the user
    assertThat(user).isNotNull();
    assertThat(user.getCandidate().getCity()).isEqualTo(candidate.getCity());

    // Verify the cached user
    User cachedUser = (User) cacheManager.getCache("users").get(user.getUsername()).get();
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

  private void verifySingleUserEvictedFromCache(User user) {
    verifyCacheEviction(user.getUsername());
    assertThat(cacheManager.getCache("users").get("testuser2")).isNotNull();
  }

  private void verifyCacheEviction(String username) {
    assertThat(cacheManager.getCache("users").get(username)).isNull();
  }

  private void verifyPartnerNameUpdated(Long partnerId, String expectedName) {
    PartnerImpl updatedPartner = partnerRepository.findById(partnerId).get();
    assertThat(updatedPartner.getName()).isEqualTo(expectedName);
  }

  private void verifyEmployerNameUpdated(Long employerId, String expectedName) {
    Employer updatedEmployer = employerRepository.findById(employerId).get();
    assertThat(updatedEmployer.getName()).isEqualTo(expectedName);
  }

  private void verifyCountryNameUpdated(Long countryId, String expectedName) {
    Country updatedCountry = countryRepository.findById(countryId).get();
    assertThat(updatedCountry.getName()).isEqualTo(expectedName);
  }

  private void verifyCandidateCityUpdated(Long candidateId, String expectedCity) {
    Candidate updatedCandidate = candidateRepository.findById(candidateId).get();
    assertThat(updatedCandidate.getCity()).isEqualTo(expectedCity);
  }

}
