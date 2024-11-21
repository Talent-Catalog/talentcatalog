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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateIds;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateReviewStatusItem;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCountry;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedList;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedOccupation;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSavedSearch;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSourceCountryIds;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.CandidateReviewStatusItem;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.ReviewStatus;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;

public class CandidateRepositoryIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateRepository repo;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SavedSearchRepository savedSearchRepository;

  @Autowired
  private CandidateReviewStatusRepository crsiRepository;

  @Autowired
  private CountryRepository countryRepository;

  @Autowired
  private CandidateOccupationRepository coRepository;

  @Autowired
  private OccupationRepository occupationRepository;

  private Candidate testCandidate;
  private LocalDate dateFrom;
  private LocalDate dateTo;
  private Country testCountry;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    testCountry = getSavedCountry(countryRepository);
    testCandidate = getSavedCandidate(repo, getSavedUser(userRepository));
    testCandidate.setCountry(testCountry);
    repo.save(testCandidate);

    dateFrom = OffsetDateTime.now().minusYears(4).toLocalDate();
    dateTo = OffsetDateTime.now().minusDays(10).toLocalDate();
  }

  @Test
  public void testFindByPhoneIgnoreCase() {
    Candidate result = repo.findByPhoneIgnoreCase(testCandidate.getPhone());
    assertNotNull(result);
    assertEquals(testCandidate.getId(), result.getId());
  }

  @Test
  public void testFindByPhoneIgnoreCaseFail() {
    Candidate result = repo.findByPhoneIgnoreCase("nothing");
    assertNull(result);
  }

  @Test
  public void testFindByWhatsappIgnoreCase() {
    Candidate result = repo.findByWhatsappIgnoreCase(testCandidate.getWhatsapp());
    assertNotNull(result);
    assertEquals(testCandidate.getId(), result.getId());
  }

  @Test
  public void testFindByWhatsappIgnoreCaseFail() {
    Candidate result = repo.findByWhatsappIgnoreCase("nothing");
    assertNull(result);
  }

  @Test
  public void testFindByIdLoadCandidateOccupations() {
    CandidateOccupation co = new CandidateOccupation();
    co.setId(99999999L);
    testCandidate.setCandidateOccupations(List.of(co));
    Candidate result = repo.findByIdLoadCandidateOccupations(testCandidate.getId());
    assertNotNull(result);
    assertEquals(testCandidate.getId(), result.getId());
    assertNotNull(result.getCandidateOccupations());
    assertFalse(result.getCandidateOccupations().isEmpty());
  }

  @Test
  public void testFindByIdLoadCertifications() {
    CandidateCertification cc = new CandidateCertification();
    cc.setId(99999999L);
    testCandidate.setCandidateCertifications(List.of(cc));
    Candidate result = repo.findByIdLoadCertifications(testCandidate.getId());
    assertNotNull(result);
    assertEquals(testCandidate.getId(), result.getId());
    assertNotNull(result.getCandidateCertifications());
    assertFalse(result.getCandidateCertifications().isEmpty());
  }

  @Test
  public void testFindByIdLoadCandidateLanguages() {
    CandidateLanguage cl = new CandidateLanguage();
    cl.setId(99999999L);
    testCandidate.setCandidateLanguages(List.of(cl));
    Candidate result = repo.findByIdLoadCandidateLanguages(testCandidate.getId());
    assertNotNull(result);
    assertEquals(testCandidate.getId(), result.getId());
    assertNotNull(result.getCandidateLanguages());
    assertFalse(result.getCandidateLanguages().isEmpty());
  }

  @Test
  public void testFindByUserId() {
    Candidate result = repo.findByUserId(testCandidate.getUser().getId());
    assertNotNull(result);
    assertEquals(testCandidate.getId(), result.getId());
  }

  @Test
  public void testFindByIds() {
    List<Long> ids = List.of(testCandidate.getId());
    List<Candidate> result = repo.findByIds(ids);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testFindByIdLoadSavedLists() {
    testCandidate.addSavedList(getSavedList());
    Candidate result = repo.findByIdLoadSavedLists(testCandidate.getId());
    assertNotNull(result);
    assertEquals(testCandidate.getId(), result.getId());
    assertNotNull(result.getCandidateSavedLists());
    assertFalse(result.getCandidateSavedLists().isEmpty());
  }

  @Test
  public void testFindCandidatesWhereStatusNotDeleted() {
    var result = repo.findCandidatesWhereStatusNotDeleted(Pageable.unpaged());
    assertNotNull(result);
    assertFalse(result.getContent().isEmpty());
    assertTrue(result.getContent().stream()
        .noneMatch(candidate -> candidate.getStatus() == CandidateStatus.deleted));
  }

  @Test
  public void testFindByStatuses() {
    var result = repo.findByStatuses(List.of(CandidateStatus.active));
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testFindByStatusesOrSfLinkIsNotNull() {
    var result = repo.findByStatusesOrSfLinkIsNotNull(List.of(CandidateStatus.active),
        Pageable.unpaged());
    assertNotNull(result);
    assertFalse(result.getContent().isEmpty());
    assertEquals(testCandidate.getId(), result.getContent().getFirst().getId());
  }

  @Test
  public void testFindByStatusesOrSfLinkIsNotNullStatusTest() {
    var result = repo.findByStatusesOrSfLinkIsNotNull(List.of(CandidateStatus.pending),
        Pageable.unpaged());
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
  }

  @Test
  public void testFindByStatusesOrSfLinkIsNotNullSfLinkTest() {
    testCandidate.setSflink("NOTNULL");
    repo.save(testCandidate);
    var result = repo.findByStatusesOrSfLinkIsNotNull(List.of(CandidateStatus.pending),
        Pageable.unpaged());
    assertNotNull(result);
    assertFalse(result.getContent().isEmpty());
    assertEquals(testCandidate.getId(), result.getContent().getFirst().getId());
  }

  @Test
  public void testClearAllCandidateTextSearchIds() {
    repo.clearAllCandidateTextSearchIds();
    var result = repo.findById(testCandidate.getId()).orElse(null);
    assertNotNull(result);
    assertNull(result.getTextSearchId());
  }

  @Test
  public void testFindReviewedCandidatesBySavedSearchId() {
    SavedSearch testSavedSearch = getSavedSavedSearch(savedSearchRepository);
    CandidateReviewStatusItem candidateReviewStatusItem = getCandidateReviewStatusItem();
    candidateReviewStatusItem.setCandidate(testCandidate);
    candidateReviewStatusItem.setSavedSearch(testSavedSearch);
    crsiRepository.save(candidateReviewStatusItem);

    Page<Candidate> result = repo.findReviewedCandidatesBySavedSearchId(
        testSavedSearch.getId(),
        List.of(ReviewStatus.rejected),
        Pageable.unpaged()
    );

    assertNotNull(result);
    assertFalse(result.getContent().isEmpty());
    assertEquals(testCandidate.getId(), result.getContent().getFirst().getId());
  }

  @Test
  public void testFindReviewedCandidatesBySavedSearchIdFailStatus() {
    var testSavedSearch = getSavedSavedSearch(savedSearchRepository);
    CandidateReviewStatusItem csri = getCandidateReviewStatusItem();
    csri.setCandidate(testCandidate);
    csri.setSavedSearch(testSavedSearch);
    crsiRepository.save(csri);
    var result = repo.findReviewedCandidatesBySavedSearchId(testSavedSearch.getId(),
        List.of(ReviewStatus.verified), Pageable.unpaged());
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
  }

  @Test
  public void testFindByCandidateNumberRestricted() {
    testCandidate.setCountry(testCountry);
    repo.save(testCandidate);
    var result = repo.findByCandidateNumberRestricted(testCandidate.getCandidateNumber(),
        Set.of(testCountry));
    assertTrue(result.isPresent());
    assertEquals(testCandidate.getId(), result.get().getId());
  }

  @Test
  public void testFindByCandidateNumberRestrictedFail() {
    testCandidate.setCountry(testCountry);
    repo.save(testCandidate);
    var result = repo.findByCandidateNumberRestricted("INVALID_NUMBER", Set.of(testCountry));
    assertFalse(result.isPresent());
  }

  @Test
  public void testSearchCandidateEmail() {
    testCandidate.setCountry(testCountry);
    repo.save(testCandidate);
    var result = repo.searchCandidateEmail(testCandidate.getUser().getEmail(), Set.of(testCountry),
        Pageable.unpaged());
    assertNotNull(result);
    assertFalse(result.getContent().isEmpty());
    assertEquals(testCandidate.getId(), result.getContent().getFirst().getId());
  }

  @Test
  public void testSearchCandidateEmailFail() {
    testCandidate.setCountry(testCountry);
    repo.save(testCandidate);
    var result = repo.searchCandidateEmail("invalid@example.com", Set.of(testCountry),
        Pageable.unpaged());
    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
  }

  @Test
  public void testFindByIdLoadUser() {
    testCandidate.setCountry(testCountry);
    repo.save(testCandidate);
    var result = repo.findByIdLoadUser(testCandidate.getId(), Set.of(testCountry));
    assertTrue(result.isPresent());
    assertEquals(testCandidate.getId(), result.get().getId());
  }

  @Test
  public void testFindByIdLoadUserFail() {
    var result = repo.findByIdLoadUser(99999L, Set.of(getSavedCountry(countryRepository)));
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindByNationalityIdFail() {
    var result = repo.findByNationalityId(99999L);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testFindByCountryId() {
    Country testCountry = getSavedCountry(countryRepository);
    testCandidate.setCountry(testCountry);
    repo.save(testCandidate);
    List<Candidate> result = repo.findByCountryId(testCountry.getId());
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testFindByCountryIdFail() {
    List<Candidate> result = repo.findByCountryId(99999L); // Assuming 99999L is an invalid ID
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testFindByCandidateNumber() {
    Candidate result = repo.findByCandidateNumber(testCandidate.getCandidateNumber());
    assertNotNull(result);
    assertEquals(testCandidate.getId(), result.getId());
  }

  @Test
  public void testFindByCandidateNumberFail() {
    Candidate result = repo.findByCandidateNumber("INVALID_NUMBER");
    assertNull(result);
  }

  @Test
  public void testCountByBirthYearOrderByYear() {
    List<Long> sourceIds = getSourceCountryIds(countryRepository, testCountry);
    List<?> result = repo.countByBirthYearOrderByYear(Gender.male.name(), sourceIds,
        dateFrom, dateTo);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByBirthYearOrderByYearWithCandidateIds() {
    List<Long> sourceIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<?> result = repo.countByBirthYearOrderByYear(Gender.male.name(), sourceIds,
        dateFrom, dateTo, candidateIds);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByCreatedDateOrderByCount() {
    List<Long> sourceIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countByCreatedDateOrderByCount(sourceIds, dateFrom,
        dateTo);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByCreatedDateOrderByCountWithCandidateIds() {
    List<Long> sourceIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByCreatedDateOrderByCount(sourceIds, dateFrom, dateTo,
        candidateIds);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountLinkedInByCreatedDateOrderByCount() {
    List<Long> sourceIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countLinkedInByCreatedDateOrderByCount(sourceIds, dateFrom,
        dateTo);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountLinkedInByCreatedDateOrderByCountWithCandidateIds() {
    List<Long> sourceIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countLinkedInByCreatedDateOrderByCount(sourceIds, dateFrom, dateTo,
        candidateIds);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByGenderOrderByCount() {
    List<Long> sourceIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countByGenderOrderByCount(sourceIds, dateFrom, dateTo);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByGenderOrderByCountWithCandidateIds() {
    List<Long> sourceIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByGenderOrderByCount(sourceIds, dateFrom, dateTo,
        candidateIds);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByStatusOrderByCount() {
    List<Long> sourceIds = Collections.singletonList(testCountry.getId());
    List<Object[]> result = repo.countByStatusOrderByCount(Gender.male.name(),
        testCountry.getName().toLowerCase(), sourceIds, dateFrom, dateTo);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByStatusOrderByCountWithCandidateIds() {
    List<Long> sourceIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByStatusOrderByCount(Gender.male.name(),
        testCountry.getName().toLowerCase(), sourceIds, dateFrom, dateTo, candidateIds);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByReferrerOrderByCount() {
    testCandidate.setRegoReferrerParam("REGOREFERRER");
    List<Long> sourceIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countByReferrerOrderByCount(Gender.male.name(),
        testCountry.getName().toLowerCase(), sourceIds, dateFrom, dateTo);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  // This test will fail, as the sql is checking for a lowercase name to be
  // passed in from the country rather than the query making like for like.
  // TODO (need to fix the query)
  @Test
  public void testCountByReferrerOrderByCountWithCandidateIds() {
    testCandidate.setRegoReferrerParam("REGOREFERRER");
    List<Long> sourceIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByReferrerOrderByCount(
        Gender.male.name(),
        testCountry.getName().toLowerCase(),
        sourceIds,
        dateFrom,
        dateTo,
        candidateIds
    );
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByUnhcrRegisteredOrderByCount() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countByUnhcrRegisteredOrderByCount(sourceCountryIds, dateFrom,
        dateTo);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByUnhcrRegisteredOrderByCountWithCandidateIds() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByUnhcrRegisteredOrderByCount(sourceCountryIds, dateFrom,
        dateTo, candidateIds);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByUnhcrStatusOrderByCount() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countByUnhcrStatusOrderByCount(sourceCountryIds, dateFrom, dateTo);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByUnhcrStatusOrderByCountWithCandidateIds() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByUnhcrStatusOrderByCount(sourceCountryIds, dateFrom, dateTo,
        candidateIds);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByLanguageOrderByCount() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countByLanguageOrderByCount(Gender.male.name(), sourceCountryIds,
        dateFrom, dateTo);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByLanguageOrderByCountWithCandidateIds() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByLanguageOrderByCount(Gender.male.name(), sourceCountryIds,
        dateFrom, dateTo, candidateIds);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByMaxEducationLevelOrderByCount() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countByMaxEducationLevelOrderByCount(Gender.male.name(),
        sourceCountryIds, dateFrom, dateTo);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByMaxEducationLevelOrderByCountWithCandidateIds() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByMaxEducationLevelOrderByCount(Gender.male.name(),
        sourceCountryIds, dateFrom, dateTo, candidateIds);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByMostCommonOccupationOrderByCount() {
    CandidateOccupation candidateOccupation = new CandidateOccupation();
    candidateOccupation.setOccupation(getSavedOccupation(occupationRepository));
    candidateOccupation.setCandidate(testCandidate);
    candidateOccupation.setYearsExperience(7L);
    coRepository.save(candidateOccupation);
    
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countByMostCommonOccupationOrderByCount(Gender.male.name(),
        sourceCountryIds, dateFrom, dateTo);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByMostCommonOccupationOrderByCountWithCandidateIds() {
    CandidateOccupation candidateOccupation = new CandidateOccupation();
    candidateOccupation.setOccupation(getSavedOccupation(occupationRepository));
    candidateOccupation.setCandidate(testCandidate);
    candidateOccupation.setYearsExperience(7L);
    coRepository.save(candidateOccupation);

    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByMostCommonOccupationOrderByCount(Gender.male.name(),
        sourceCountryIds, dateFrom, dateTo, candidateIds);
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  // This test will fail, as the SQL is checking for a lowercase name to be
  // passed in from the country rather than the query making like for like.
  // TODO: need to fix the query
  @Test
  public void testCountByNationalityOrderByCount() {
    testCandidate.setNationality(testCountry);
    repo.save(testCandidate);
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countByNationalityOrderByCount(
        Gender.male.name(),
        testCountry.getName(),
        sourceCountryIds,
        dateFrom,
        dateTo
    );
    fail("Expect to fail - lowercase hardcoded in query. Should be fixed?");
    // assertNotNull(result);
    // assertTrue(!result.isEmpty());
    // assertEquals(1, result.size());
  }

  // This test will fail, as the SQL is checking for a lowercase name to be
  // passed in from the country rather than the query making like for like.
  // TODO: need to fix the query
  @Test
  public void testCountByNationalityOrderByCountWithCandidateIds() {
    testCandidate.setNationality(testCountry);
    repo.save(testCandidate);
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByNationalityOrderByCount(
        Gender.male.name(),
        testCountry.getName(),
        sourceCountryIds,
        dateFrom,
        dateTo,
        candidateIds
    );
    fail("Expect to fail - lowercase hardcoded in query. Should be fixed?");
    // assertNotNull(result);
    // assertTrue(!result.isEmpty());
    // assertEquals(1, result.size());
  }

  @Test
  public void testCountBySourceCountryOrderByCount() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countBySourceCountryOrderByCount(
        Gender.male.name(),
        sourceCountryIds,
        dateFrom,
        dateTo
    );
    assertNotNull(result);
    assertFalse(result.isEmpty());
    // Additional assertions can be added as needed
  }

  @Test
  public void testCountBySourceCountryOrderByCountWithCandidateIds() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countBySourceCountryOrderByCount(
        Gender.male.name(),
        sourceCountryIds,
        dateFrom,
        dateTo,
        candidateIds
    );
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByOccupationOrderByCount() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countByOccupationOrderByCount(
        Gender.male.name(),
        sourceCountryIds,
        dateFrom,
        dateTo
    );
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByOccupationOrderByCountWithCandidateIds() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByOccupationOrderByCount(
        Gender.male.name(),
        sourceCountryIds,
        dateFrom,
        dateTo,
        candidateIds
    );
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByLanguagesOrderByCount() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    List<Object[]> result = repo.countByLanguageOrderByCount(
        Gender.male.name(),
        sourceCountryIds,
        dateFrom,
        dateTo
    );
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  public void testCountByLanguagesOrderByCountWithCandidateIds() {
    List<Long> sourceCountryIds = getSourceCountryIds(countryRepository, testCountry);
    Set<Long> candidateIds = getCandidateIds(repo, userRepository, testCandidate);
    List<Object[]> result = repo.countByLanguageOrderByCount(
        Gender.male.name(),
        sourceCountryIds,
        dateFrom,
        dateTo,
        candidateIds
    );
    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }
}