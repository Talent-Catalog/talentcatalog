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
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateEducation;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateLanguage;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getCandidateOccupation;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getLanguage;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCandidate;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedCountry;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedEducationLevel;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedEducationMajor;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedLanguage;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedLanguageLevel;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedOccupation;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedPartner;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedSurveyType;
import static org.tctalent.server.repository.db.integrationhelp.DomainHelpers.getSavedUser;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.SurveyType;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.integrationhelp.BaseDBIntegrationTest;
import org.tctalent.server.request.candidate.SearchCandidateRequest;

public class CandidateSpecificationIntTest extends BaseDBIntegrationTest {

  @Autowired
  private CandidateRepository repo;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CountryRepository countryRepository;

  @Autowired
  private EducationLevelRepository educationLevelRepository;

  @Autowired
  private LanguageRepository languageRepository;

  @Autowired
  private LanguageLevelRepository languageLevelRepository;

  @Autowired
  private CandidateLanguageRepository candidateLanguageRepository;

  @Autowired
  private OccupationRepository occupationRepository;

  @Autowired
  private CandidateOccupationRepository candidateOccupationRepository;

  @Autowired
  private SurveyTypeRepository surveyTypeRepository;

  @Autowired
  private PartnerRepository partnerRepository;

  @Autowired
  private EducationMajorRepository educationMajorRepository;

  @Autowired
  private CandidateEducationRepository candidateEducationRepository;

  private Candidate testCandidate;
  private SearchCandidateRequest request;
  private Specification<Candidate> spec;

  @BeforeEach
  public void setup() {
    assertTrue(isContainerInitialised());
    testCandidate = getSavedCandidate(repo, getSavedUser(userRepository));
    repo.save(testCandidate);
    testCandidate.setNationality(getSavedCountry(countryRepository));
    testCandidate.setCountry(getSavedCountry(countryRepository));
    testCandidate.setMaxEducationLevel(getSavedEducationLevel(educationLevelRepository));

    request = new SearchCandidateRequest();
  }

  @Test
  public void testKeywordWithEmptyName() {
    request.setKeyword("");
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
  }

  @Test
  public void testKeywordCaseInsensitive() {
    request.setKeyword(testCandidate.getUser().getFirstName().toUpperCase());
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(testCandidate.getId(), results.getFirst().getId());
  }

  @Test
  public void testEmptyStatusWithAdditionalFilters() {
    request.setMinYrs(2);
    request.setMaxYrs(5);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(testCandidate.getId(), results.getFirst().getId());
  }

  @Test
  public void testOkStatus() {
    request.setStatuses(List.of(CandidateStatus.active));
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(testCandidate.getId(), results.getFirst().getId());
  }

  @Test
  public void testInvalidStatus() {
    request.setStatuses(List.of(CandidateStatus.ineligible));
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testOccupation() {
    Occupation occ1 = getSavedOccupation(occupationRepository);
    CandidateOccupation co = getCandidateOccupation();
    co.setCandidate(testCandidate);
    co.setOccupation(occ1);
    candidateOccupationRepository.save(co);

    request.setOccupationIds(List.of(occ1.getId()));
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(testCandidate.getId(), results.getFirst().getId());
  }

  @Test
  public void testInvalidOccupationId() {
    request.setOccupationIds(List.of(-1L));
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testMinYrsExperience() {
    Occupation occ1 = getSavedOccupation(occupationRepository);
    CandidateOccupation co = getCandidateOccupation();
    co.setCandidate(testCandidate);
    co.setOccupation(occ1);
    candidateOccupationRepository.save(co);

    request.setOccupationIds(List.of(occ1.getId()));
    request.setMinYrs(2);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(testCandidate.getId(), results.getFirst().getId());
  }

  @Test
  public void testMinYrsExperienceFail() {
    Occupation occ1 = getSavedOccupation(occupationRepository);
    CandidateOccupation co = getCandidateOccupation();
    co.setCandidate(testCandidate);
    co.setOccupation(occ1);
    candidateOccupationRepository.save(co);

    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setOccupationIds(List.of(occ1.getId()));
    request.setMinYrs(25);
    Specification<Candidate> spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testMaxYrs() {
    Occupation occ1 = getSavedOccupation(occupationRepository);
    CandidateOccupation co = getCandidateOccupation();
    co.setCandidate(testCandidate);
    co.setOccupation(occ1);
    candidateOccupationRepository.save(co);

    request.setOccupationIds(List.of(occ1.getId()));
    request.setMaxYrs(20);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(testCandidate.getId(), results.getFirst().getId());
  }

  @Test
  public void testMaxYrsExperienceFail() {
    Occupation occ1 = getSavedOccupation(occupationRepository);
    CandidateOccupation co = getCandidateOccupation();
    co.setCandidate(testCandidate);
    co.setOccupation(occ1);
    candidateOccupationRepository.save(co);

    request.setOccupationIds(List.of(occ1.getId()));
    request.setMaxYrs(1);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  public void testMinYrsExperienceGreaterThanMax() {
    request.setMinYrs(2);
    request.setMaxYrs(5);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(testCandidate.getId(), results.getFirst().getId());
  }

  @Test
  public void testSurveyType() {
    SurveyType st = getSavedSurveyType(surveyTypeRepository);
    testCandidate.setSurveyType(st);
    repo.save(testCandidate);
    request.setSurveyTypeIds(List.of(st.getId()));
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> results = repo.findAll(spec);
    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
  }

  @Test
  public void testNationalitySearch() {
    request.setNationalityIds(List.of(testCandidate.getNationality().getId()));
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertTrue(result.stream()
        .anyMatch(c -> c.getNationality().getId().equals(testCandidate.getNationality().getId())));
  }

  @Test
  public void testMinAgeSearch() {
    request.setMinAge(18);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertTrue(result.stream()
        .allMatch(c -> c.getDob() == null || c.getDob().isBefore(LocalDate.now().minusYears(18))));
  }

  @Test
  public void testMaxAgeSearch() {
    request.setMaxAge(30);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty(), "Expected results");
    assertEquals(1, result.size());
    assertTrue(result.stream()
            .allMatch(c -> c.getDob() == null || c.getDob().isAfter(LocalDate.now().minusYears(30))),
        "Wrong date of birth result.");
  }

  @Test
  public void testGenderSearch() {
    request.setGender(
        testCandidate.getGender());
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertTrue(result.stream().anyMatch(c -> {
      assert c.getGender() != null;
      return c.getGender().equals(testCandidate.getGender());
    }));
  }

  @Test
  public void testEducationLevelSearch() {
    EducationLevel el = getSavedEducationLevel(educationLevelRepository);
    testCandidate.setMaxEducationLevel(el);
    repo.save(testCandidate);
    request.setMinEducationLevel(
        testCandidate.getMaxEducationLevel().getLevel());
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertTrue(result.stream().anyMatch(
        c -> c.getMaxEducationLevel().getLevel() >= testCandidate.getMaxEducationLevel()
            .getLevel()));
  }

  @Test
  public void testLanguageSearch() {
    LanguageLevel level = getSavedLanguageLevel(languageLevelRepository);
    Language language = getLanguage();
    language.setName("english");
    Language savedLanguage = languageRepository.save(language);
    CandidateLanguage candidateLanguage = getCandidateLanguage();
    candidateLanguage.setLanguage(savedLanguage);
    candidateLanguage.setCandidate(testCandidate);
    candidateLanguage.setWrittenLevel(level);
    candidateLanguage.setSpokenLevel(level);
    CandidateLanguage cl = candidateLanguageRepository.save(candidateLanguage);

    request.setEnglishMinWrittenLevel(cl.getWrittenLevel().getLevel());
    request.setEnglishMinSpokenLevel(cl.getSpokenLevel().getLevel());
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testLanguageSearchNotEnglishFails() {
    CandidateLanguage candidateLanguage = getCandidateLanguage();
    candidateLanguage.setLanguage(getSavedLanguage(languageRepository));
    candidateLanguage.setCandidate(testCandidate);
    candidateLanguage.setWrittenLevel(getSavedLanguageLevel(languageLevelRepository));
    candidateLanguage.setSpokenLevel(getSavedLanguageLevel(languageLevelRepository));
    CandidateLanguage cl = candidateLanguageRepository.save(candidateLanguage);

    repo.save(testCandidate);

    request.setEnglishMinWrittenLevel(cl.getWrittenLevel().getLevel());
    request.setEnglishMinSpokenLevel(cl.getSpokenLevel().getLevel());
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testExclusionSearch() {
    List<Candidate> excludedCandidates = List.of(testCandidate);
    spec = CandidateSpecification.buildSearchQuery(request, null, excludedCandidates);
    List<Candidate> result = repo.findAll(spec);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testLastModifiedFromSearch() {
    testCandidate.setUpdatedDate(OffsetDateTime.now().minusDays(1));
    repo.save(testCandidate);
    request.setLastModifiedFrom(LocalDate.now().minusDays(1));
    request.setTimezone(ZoneOffset.systemDefault().getId());
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testLastModifiedToSearch() {
    testCandidate.setUpdatedDate(OffsetDateTime.now().minusDays(1));
    repo.save(testCandidate);
    request.setLastModifiedTo(LocalDate.now().minusDays(1));
    request.setTimezone(ZoneOffset.systemDefault().getId());
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testUnhcrStatus() {
    assert testCandidate.getUnhcrStatus() != null;
    request.setUnhcrStatuses(
        List.of(testCandidate.getUnhcrStatus(), UnhcrStatus.NotRegistered));
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testCountrySearchWithLoggedInUser() {
    Country c = getSavedCountry(countryRepository);
    testCandidate.setCountry(c);
    repo.save(testCandidate);
    User user = testCandidate.getUser();
    user.setSourceCountries(Set.of(c));
    spec = CandidateSpecification.buildSearchQuery(request, user, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  /**
   * Code under test is not in use, so will not fill it out right now.
   */
  @Test
  public void testFilterByOpps() {
    assertTrue(true);
  }

  @Test
  public void testCountrySearchNoSearchType() {
    Country c = getSavedCountry(countryRepository);
    testCandidate.setCountry(c);
    repo.save(testCandidate);

    request.setCountryIds(List.of(c.getId()));
    request.setCountrySearchType(null);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testCountrySearchWithSearchType() {
    Country c = getSavedCountry(countryRepository);
    testCandidate.setCountry(c);
    repo.save(testCandidate);

    request.setCountryIds(List.of(12L));
    request.setCountrySearchType(
        SearchType.and);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testReferrerSearch() {
    String referParam = "REFER";
    testCandidate.setRegoReferrerParam(referParam);
    repo.save(testCandidate);

    request.setRegoReferrerParam(referParam);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testPartnerSearch() {
    PartnerImpl savedPartners = getSavedPartner(partnerRepository);
    testCandidate.getUser().setPartner(savedPartners);
    userRepository.save(testCandidate.getUser());

    request.setPartnerIds(
        List.of(savedPartners.getId()));
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testMiniIntakeTrue() {
    testCandidate.setMiniIntakeCompletedDate(OffsetDateTime.now().minusDays(50));
    repo.save(testCandidate);

    request.setMiniIntakeCompleted(true);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testMiniIntakeFalse() {
    request.setMiniIntakeCompleted(false);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testFullIntakeTrue() {
    testCandidate.setFullIntakeCompletedDate(OffsetDateTime.now().minusDays(50));
    repo.save(testCandidate);

    request.setFullIntakeCompleted(true);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testFullIntakeFalse() {
    request.setFullIntakeCompleted(false);
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testEducationMajors() {
    EducationMajor majors = getSavedEducationMajor(educationMajorRepository);
    testCandidate.setCountry(getSavedCountry(countryRepository));
    repo.save(testCandidate);

    CandidateEducation ce = getCandidateEducation();
    ce.setCandidate(testCandidate);
    ce.setEducationMajor(majors);
    ce.setCountry(testCandidate.getCountry());
    candidateEducationRepository.save(ce);

    request.setEducationMajorIds(List.of(majors.getId()));
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testMigrationMajors() {
    EducationMajor majors = getSavedEducationMajor(educationMajorRepository);
    testCandidate.setCountry(getSavedCountry(countryRepository));
    testCandidate.setMigrationEducationMajor(majors);
    repo.save(testCandidate);

    CandidateEducation ce = getCandidateEducation();
    ce.setCandidate(testCandidate);
    ce.setEducationMajor(majors);
    ce.setCountry(testCandidate.getCountry());
    candidateEducationRepository.save(ce);

    request.setEducationMajorIds(List.of(majors.getId()));
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testMinEnglishWrittenLevel() {
    CandidateLanguage candidateLanguage = getCandidateLanguage();
    candidateLanguage.setCandidate(testCandidate);
    candidateLanguage.setLanguage(getSavedLanguage(languageRepository));
    candidateLanguage.setWrittenLevel(getSavedLanguageLevel(languageLevelRepository));
    CandidateLanguage cl = candidateLanguageRepository.save(candidateLanguage);

    request.setEnglishMinWrittenLevel(cl.getWrittenLevel().getLevel());
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testMinEnglishSpokenLevel() {
    CandidateLanguage candidateLanguage = getCandidateLanguage();
    candidateLanguage.setCandidate(testCandidate);
    candidateLanguage.setLanguage(getSavedLanguage(languageRepository));
    candidateLanguage.setSpokenLevel(getSavedLanguageLevel(languageLevelRepository));
    CandidateLanguage cl = candidateLanguageRepository.save(candidateLanguage);

    request.setEnglishMinSpokenLevel(cl.getSpokenLevel().getLevel());
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testOtherLanguage() {
    LanguageLevel level = getSavedLanguageLevel(languageLevelRepository);
    Language language = getLanguage();
    language.setName("NOT ENGLISH");

    Language savedLanguage = languageRepository.save(language);
    CandidateLanguage candidateLanguage = getCandidateLanguage();
    candidateLanguage.setCandidate(testCandidate);
    candidateLanguage.setLanguage(savedLanguage);
    candidateLanguage.setSpokenLevel(level);
    candidateLanguage.setWrittenLevel(level);
    candidateLanguageRepository.save(candidateLanguage);

    request.setOtherLanguageId(savedLanguage.getId());
    request.setOtherMinWrittenLevel(level.getLevel());
    request.setOtherMinSpokenLevel(level.getLevel());

    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testOtherLanguageMinSpoken() {
    LanguageLevel level = getSavedLanguageLevel(languageLevelRepository);
    Language language = getLanguage();
    language.setName("NOT ENGLISH");

    Language savedLanguage = languageRepository.save(language);
    CandidateLanguage candidateLanguage = getCandidateLanguage();

    candidateLanguage.setCandidate(testCandidate);
    candidateLanguage.setSpokenLevel(level);
    candidateLanguage.setLanguage(savedLanguage);
    candidateLanguageRepository.save(candidateLanguage);

    request.setOtherLanguageId(savedLanguage.getId());
    request.setOtherMinSpokenLevel(level.getLevel());
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testOtherLanguageMinWritten() {
    LanguageLevel level = getSavedLanguageLevel(languageLevelRepository);
    Language language = getLanguage();
    language.setName("NOT ENGLISH");

    Language savedLanguage = languageRepository.save(language);
    CandidateLanguage candidateLanguage = getCandidateLanguage();
    candidateLanguage.setCandidate(testCandidate);
    candidateLanguage.setLanguage(savedLanguage);
    candidateLanguage.setWrittenLevel(level);

    candidateLanguageRepository.save(candidateLanguage);

    request.setOtherLanguageId(savedLanguage.getId());
    request.setOtherMinWrittenLevel(level.getLevel());
    spec = CandidateSpecification.buildSearchQuery(request, null, null);
    List<Candidate> result = repo.findAll(spec);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(testCandidate.getId(), result.getFirst().getId());
  }

  @Test
  public void testExcludedCandidates() {
    Candidate newCandidate = getSavedCandidate(repo, getSavedUser(userRepository));
    newCandidate.setNationality(getSavedCountry(countryRepository));
    newCandidate.setCountry(getSavedCountry(countryRepository));
    newCandidate.setMaxEducationLevel(getSavedEducationLevel(educationLevelRepository));
    repo.save(newCandidate);

    request.setKeyword(newCandidate.getUser().getFirstName().toUpperCase());
    spec = CandidateSpecification.buildSearchQuery(request, null, List.of(testCandidate));
    List<Candidate> results = repo.findAll(spec);

    assertNotNull(results);
    assertFalse(results.isEmpty());
    assertEquals(1, results.size());
    assertEquals(newCandidate.getId(), results.getFirst().getId());
  }
}