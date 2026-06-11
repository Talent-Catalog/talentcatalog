/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.tctalent.server.exception.CircularReferencedException;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ExportFailedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateFilterByOpps;
import org.tctalent.server.model.db.CandidateSavedList;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.ReviewStatus;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.SavedSearchType;
import org.tctalent.server.model.db.SearchJoin;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateReviewStatusRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.EducationLevelRepository;
import org.tctalent.server.repository.db.EducationMajorRepository;
import org.tctalent.server.repository.db.LanguageLevelRepository;
import org.tctalent.server.repository.db.LanguageRepository;
import org.tctalent.server.repository.db.OccupationRepository;
import org.tctalent.server.repository.db.PartnerRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.SavedSearchRepository;
import org.tctalent.server.repository.db.SearchJoinRepository;
import org.tctalent.server.repository.db.SurveyTypeRepository;
import org.tctalent.server.repository.db.UserRepository;
import org.tctalent.server.repository.db.read.dto.CandidateReadDto;
import org.tctalent.server.request.IdsRequest;
import org.tctalent.server.request.candidate.SavedSearchGetRequest;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.SearchJoinRequest;
import org.tctalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tctalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tctalent.server.request.candidate.source.UpdateCandidateSourceDescriptionRequest;
import org.tctalent.server.request.search.CreateFromDefaultSavedSearchRequest;
import org.tctalent.server.request.search.SearchSavedSearchRequest;
import org.tctalent.server.request.search.UpdateSavedSearchRequest;
import org.tctalent.server.request.search.UpdateSharingRequest;
import org.tctalent.server.request.search.UpdateWatchingRequest;
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateDtoFetchService;
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.LanguageService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.PublicIDService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;
import org.tctalent.server.util.PersistenceContextHelper;

@ExtendWith(MockitoExtension.class)
class SavedSearchServiceImplUnitTest {

  @Mock private CandidateRepository candidateRepository;
  @Mock private CandidateService candidateService;
  @Mock private CandidateDtoFetchService candidateDtoFetchService;
  @Mock private CandidateReviewStatusRepository candidateReviewStatusRepository;
  @Mock private CandidateSavedListService candidateSavedListService;
  @Mock private PartnerService partnerService;
  @Mock private EmailHelper emailHelper;
  @Mock private PublicIDService publicIDService;
  @Mock private UserRepository userRepository;
  @Mock private UserService userService;
  @Mock private SalesforceJobOppService salesforceJobOppService;
  @Mock private SavedListRepository savedListRepository;
  @Mock private SavedListService savedListService;
  @Mock private SavedSearchRepository savedSearchRepository;
  @Mock private SearchJoinRepository searchJoinRepository;
  @Mock private LanguageLevelRepository languageLevelRepository;
  @Mock private LanguageRepository languageRepository;
  @Mock private LanguageService languageService;
  @Mock private CountryRepository countryRepository;
  @Mock private PartnerRepository partnerRepository;
  @Mock private OccupationRepository occupationRepository;
  @Mock private SurveyTypeRepository surveyTypeRepository;
  @Mock private EducationMajorRepository educationMajorRepository;
  @Mock private EducationLevelRepository educationLevelRepository;
  @Mock private PersistenceContextHelper persistenceContextHelper;
  @Mock private AuthService authService;
  @Mock private EntityManager entityManager;
  @Mock private Query idsQuery;
  @Mock private Query countQuery;

  private SavedSearchServiceImpl service;
  private User user;

  @BeforeEach
  void setUp() {
    service = new SavedSearchServiceImpl(
        candidateRepository,
        candidateService,
        candidateDtoFetchService,
        candidateReviewStatusRepository,
        candidateSavedListService,
        partnerService,
        emailHelper,
        publicIDService,
        userRepository,
        userService,
        salesforceJobOppService,
        savedListRepository,
        savedListService,
        savedSearchRepository,
        searchJoinRepository,
        languageLevelRepository,
        languageRepository,
        languageService,
        countryRepository,
        partnerRepository,
        occupationRepository,
        surveyTypeRepository,
        educationMajorRepository,
        educationLevelRepository,
        persistenceContextHelper,
        authService
    );

    setPrivateField(service, "entityManager", entityManager);
    setPrivateField(service, "adminUrl", "https://admin.example.org");
    setPrivateField(service, "ENGLISH_LANGUAGE_ID", 100L);

    user = user(10L);
  }

  @Test
  @DisplayName("init sets English language id")
  void initSetsEnglishLanguageId() {
    Language english = language(100L, "english");
    given(languageService.getLanguage("english")).willReturn(english);

    service.init();

    assertEquals(100L, getPrivateLong(service, "ENGLISH_LANGUAGE_ID"));
  }

  @Test
  @DisplayName("init throws when English is missing")
  void initThrowsWhenEnglishMissing() {
    given(languageService.getLanguage("english")).willReturn(null);

    assertThrows(RuntimeException.class, () -> service.init());
  }

  @Test
  @DisplayName("search by ids delegates to repository")
  void searchByIdsDelegates() {
    IdsRequest request = new IdsRequest();
    request.setIds(Set.of(1L, 2L));

    List<SavedSearch> searches = List.of(savedSearch(1L, "A", user));
    given(savedSearchRepository.findByIds(request.getIds())).willReturn(searches);

    assertSame(searches, service.search(request));
  }

  @Test
  @DisplayName("search watched returns empty list when no logged in user")
  void searchWatchedNoUserReturnsEmptyList() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setWatched(true);

    given(userService.getLoggedInUser()).willReturn(null);

    assertTrue(service.search(request).isEmpty());
  }

  @Test
  @DisplayName("search watched returns user's watched searches")
  void searchWatchedReturnsUserWatches() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setWatched(true);

    SavedSearch savedSearch = savedSearch(1L, "Watched", user);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedSearchRepository.findUserWatchedSearches(user.getId()))
        .willReturn(Set.of(savedSearch));

    List<SavedSearch> result = service.search(request);

    assertEquals(1, result.size());
    assertSame(savedSearch, result.get(0));
  }

  @Test
  @DisplayName("search non-watched delegates to repository specification")
  @SuppressWarnings({"rawtypes", "unchecked"})
  void searchNonWatchedDelegates() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    SavedSearch savedSearch = savedSearch(1L, "Search", user);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedSearchRepository.findAll(any(Specification.class)))
        .willReturn(List.of(savedSearch));

    List<SavedSearch> result = service.search(request);

    assertEquals(List.of(savedSearch), result);
  }

  @Test
  @DisplayName("searchPaged watched creates page")
  void searchPagedWatchedCreatesPage() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setWatched(true);
    request.setPageNumber(0);
    request.setPageSize(10);

    SavedSearch savedSearch = savedSearch(1L, "Watched", user);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedSearchRepository.findUserWatchedSearches(user.getId()))
        .willReturn(Set.of(savedSearch));

    Page<SavedSearch> result = service.searchPaged(request);

    assertEquals(1, result.getTotalElements());
    assertEquals(savedSearch, result.getContent().get(0));
  }

  @Test
  @DisplayName("searchPaged non-watched delegates to repository")
  @SuppressWarnings({"rawtypes", "unchecked"})
  void searchPagedNonWatchedDelegates() {
    SearchSavedSearchRequest request = new SearchSavedSearchRequest();
    request.setPageNumber(0);
    request.setPageSize(10);

    Page<SavedSearch> page = new PageImpl<>(List.of(savedSearch(1L, "Search", user)));

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedSearchRepository.findAll(any(Specification.class), eq(request.getPageRequest())))
        .willReturn(page);

    assertSame(page, service.searchPaged(request));
  }

  @Test
  @DisplayName("setPublicIds assigns missing public ids and saves non-empty list")
  void setPublicIdsAssignsMissingIds() {
    SavedSearch missing = savedSearch(1L, "Missing", user);
    SavedSearch existing = savedSearch(2L, "Existing", user);
    existing.setPublicId("existing-public-id");

    given(publicIDService.generatePublicID()).willReturn("new-public-id");

    service.setPublicIds(List.of(missing, existing));

    assertEquals("new-public-id", missing.getPublicId());
    assertEquals("existing-public-id", existing.getPublicId());
    verify(savedSearchRepository).saveAll(List.of(missing, existing));
  }

  @Test
  @DisplayName("setPublicIds does not save empty list")
  void setPublicIdsDoesNotSaveEmptyList() {
    service.setPublicIds(List.of());

    verify(savedSearchRepository, never()).saveAll(anyList());
  }

  @Test
  @DisplayName("loadSavedSearch converts saved search to request")
  void loadSavedSearchConvertsSavedSearch() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    savedSearch.setKeyword("developer");
    savedSearch.setStatuses("active");
    savedSearch.setSearchJoins(new HashSet<>());

    given(savedSearchRepository.findByIdLoadSearchJoins(1L))
        .willReturn(Optional.of(savedSearch));

    SearchCandidateRequest result = service.loadSavedSearch(1L);

    assertEquals(1L, result.getSavedSearchId());
    assertEquals("developer", result.getKeyword());
    assertEquals(List.of(CandidateStatus.active), result.getStatuses());
  }

  @Test
  @DisplayName("loadSavedSearch throws when missing")
  void loadSavedSearchThrowsWhenMissing() {
    given(savedSearchRepository.findByIdLoadSearchJoins(99L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.loadSavedSearch(99L));
  }

  @Test
  @DisplayName("getSavedSearch populates transient display names")
  void getSavedSearchPopulatesNames() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    savedSearch.setCountryIds("1,2");
    savedSearch.setPartnerIds("3");
    savedSearch.setNationalityIds("4");
    savedSearch.setOccupationIds("5");
    savedSearch.setEducationMajorIds("6");
    savedSearch.setSurveyTypeIds("7");
    savedSearch.setEnglishMinWrittenLevel(1);
    savedSearch.setEnglishMinSpokenLevel(2);
    savedSearch.setOtherMinSpokenLevel(3);
    savedSearch.setOtherMinWrittenLevel(4);
    savedSearch.setMinEducationLevel(5);

    given(savedSearchRepository.findByIdLoadUsers(1L)).willReturn(Optional.of(savedSearch));
    given(languageLevelRepository.findAllActive()).willReturn(List.of(
        languageLevel(1, "A1"),
        languageLevel(2, "A2"),
        languageLevel(3, "B1"),
        languageLevel(4, "B2")
    ));
    given(educationLevelRepository.findAllActive()).willReturn(List.of(educationLevel(5, "Bachelor")));
    given(countryRepository.getNamesForIds(List.of(1L, 2L))).willReturn(List.of("Afghanistan", "Iran"));
    given(partnerRepository.getNamesForIds(List.of(3L))).willReturn(List.of("Partner"));
    given(countryRepository.getNamesForIds(List.of(4L))).willReturn(List.of("Syria"));
    given(occupationRepository.getNamesForIds(List.of(5L))).willReturn(List.of("Developer"));
    given(educationMajorRepository.getNamesForIds(List.of(6L))).willReturn(List.of("CS"));
    given(surveyTypeRepository.getNamesForIds(List.of(7L))).willReturn(List.of("Survey"));

    SavedSearch result = service.getSavedSearch(1L);

    assertSame(savedSearch, result);
    assertEquals(List.of("Afghanistan", "Iran"), result.getCountryNames());
    assertEquals("A1", result.getEnglishWrittenLevel());
    assertEquals("A2", result.getEnglishSpokenLevel());
    assertEquals("B1", result.getOtherSpokenLevel());
    assertEquals("B2", result.getOtherWrittenLevel());
    assertEquals("Bachelor", result.getMinEducationLevelName());
  }

  @Test
  @DisplayName("getSavedSearch throws when missing")
  void getSavedSearchThrowsWhenMissing() {
    given(savedSearchRepository.findByIdLoadUsers(99L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.getSavedSearch(99L));
  }

  @Test
  @DisplayName("clearSelection clears user's selection list")
  void clearSelectionClearsSelectionList() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    SavedList list = savedList(20L, "Selection", user);

    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));
    given(userRepository.findById(10L)).willReturn(Optional.of(user));
    given(savedListRepository.findSelectionList(1L, 10L)).willReturn(Optional.of(list));

    service.clearSelection(1L, 10L);

    verify(candidateSavedListService).clearSavedList(20L);
  }

  @Test
  @DisplayName("createSavedSearch creates from default search and copies selections")
  void createSavedSearchCopiesFromDefault() {
    SavedSearch defaultSearch = savedSearch(1L, "Default", user);
    defaultSearch.setDefaultSearch(true);
    defaultSearch.setSearchJoins(new HashSet<>());

    SavedList fromList = savedList(100L, "From", user);
    SavedList toList = savedList(101L, "To", user);

    UpdateSavedSearchRequest request = updateSearchRequest("Created");
    request.setSearchCandidateRequest(new SearchCandidateRequest());

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedSearchRepository.findDefaultSavedSearch(user.getId()))
        .willReturn(Optional.of(defaultSearch));
    given(publicIDService.generatePublicID()).willReturn("public-id");
    given(savedSearchRepository.findByNameIgnoreCase("Created", user.getId())).willReturn(null);
    given(savedSearchRepository.save(any(SavedSearch.class))).willAnswer(invocation -> {
      SavedSearch saved = invocation.getArgument(0);
      if (saved.getId() == null) {
        saved.setId(2L);
      }
      return saved;
    });
    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(defaultSearch));
    given(savedSearchRepository.findById(2L))
        .willReturn(Optional.of(savedSearch(2L, "Created", user)));
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(savedListRepository.findSelectionList(1L, user.getId())).willReturn(Optional.of(fromList));
    given(savedListRepository.findSelectionList(2L, user.getId())).willReturn(Optional.of(toList));

    SavedSearch result = service.createSavedSearch(request);

    assertEquals("Created", result.getName());
    assertEquals("public-id", result.getPublicId());
    verify(candidateSavedListService).copyContents(fromList, toList, false);
  }

  @Test
  @DisplayName("getDefaultSavedSearch throws when not logged in")
  void getDefaultSavedSearchThrowsWhenNotLoggedIn() {
    given(userService.getLoggedInUser()).willReturn(null);

    assertThrows(InvalidSessionException.class, () -> service.getDefaultSavedSearch());
  }

  @Test
  @DisplayName("getDefaultSavedSearch returns existing default search")
  void getDefaultSavedSearchReturnsExisting() {
    SavedSearch savedSearch = savedSearch(1L, "Default", user);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedSearchRepository.findDefaultSavedSearch(user.getId()))
        .willReturn(Optional.of(savedSearch));

    assertSame(savedSearch, service.getDefaultSavedSearch());
  }

  @Test
  @DisplayName("getDefaultSavedSearch creates default when missing")
  void getDefaultSavedSearchCreatesMissingDefault() {
    given(userService.getLoggedInUser()).willReturn(user);
    given(savedSearchRepository.findDefaultSavedSearch(user.getId())).willReturn(Optional.empty());
    given(publicIDService.generatePublicID()).willReturn("default-public-id");
    given(savedSearchRepository.findByNameIgnoreCase(anyString(), eq(user.getId()))).willReturn(null);
    given(savedSearchRepository.save(any(SavedSearch.class))).willAnswer(invocation -> {
      SavedSearch saved = invocation.getArgument(0);
      saved.setId(1L);
      return saved;
    });

    SavedSearch result = service.getDefaultSavedSearch();

    assertEquals("_DefaultSavedSearchForUser10", result.getName());
    assertTrue(result.getDefaultSearch());
    assertEquals("default-public-id", result.getPublicId());
  }

  @Test
  @DisplayName("createFromDefaultSavedSearch throws when not logged in")
  void createFromDefaultSavedSearchThrowsWhenNotLoggedIn() {
    given(userService.getLoggedInUser()).willReturn(null);

    assertThrows(
        InvalidSessionException.class,
        () -> service.createFromDefaultSavedSearch(new CreateFromDefaultSavedSearchRequest())
    );
  }

  @Test
  @DisplayName("updateSavedSearch throws when not logged in")
  void updateSavedSearchThrowsWhenNotLoggedIn() {
    given(userService.getLoggedInUser()).willReturn(null);

    assertThrows(
        InvalidSessionException.class,
        () -> service.updateSavedSearch(1L, updateSearchRequest("Name"))
    );
  }

  @Test
  @DisplayName("updateSavedSearch updates metadata when request has no candidate request")
  void updateSavedSearchUpdatesMetadataOnly() {
    SavedSearch savedSearch = savedSearch(1L, "Old", user);
    savedSearch.setFixed(false);

    SalesforceJobOpp job = new SalesforceJobOpp();
    job.setId(30L);

    UpdateSavedSearchRequest request = updateSearchRequest("New");
    request.setSearchCandidateRequest(null);
    request.setJobId(30L);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));
    given(salesforceJobOppService.getJobOpp(30L)).willReturn(job);
    given(savedSearchRepository.save(savedSearch)).willReturn(savedSearch);

    SavedSearch result = service.updateSavedSearch(1L, request);

    assertSame(savedSearch, result);
    assertEquals("New", savedSearch.getName());
    assertSame(job, savedSearch.getSfJobOpp());
  }

  @Test
  @DisplayName("updateSavedSearch returns fixed search unchanged when user is not owner")
  void updateSavedSearchRefusesFixedOtherUser() {
    User owner = user(99L);
    SavedSearch savedSearch = savedSearch(1L, "Fixed", owner);
    savedSearch.setFixed(true);

    UpdateSavedSearchRequest request = updateSearchRequest("New");
    request.setSearchCandidateRequest(null);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));

    SavedSearch result = service.updateSavedSearch(1L, request);

    assertSame(savedSearch, result);
    assertEquals("Fixed", savedSearch.getName());
    verify(savedSearchRepository, never()).save(savedSearch);
  }

  @Test
  @DisplayName("updateSavedSearch with candidate request replaces joins and saves new search")
  void updateSavedSearchWithCandidateRequestReplacesJoins() {
    SavedSearch original = savedSearch(1L, "Old", user);
    original.setDescription("description");
    original.setDisplayedFieldsLong(List.of("long"));
    original.setDisplayedFieldsShort(List.of("short"));

    SavedSearch child = savedSearch(2L, "Child", user);

    SearchCandidateRequest candidateRequest = new SearchCandidateRequest();
    candidateRequest.setSearchJoinRequests(List.of(new SearchJoinRequest(2L, "Child", SearchType.and)));

    UpdateSavedSearchRequest request = updateSearchRequest("Updated");
    request.setSearchCandidateRequest(candidateRequest);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(original));
    given(savedSearchRepository.findById(2L)).willReturn(Optional.of(child));
    given(savedSearchRepository.findByNameIgnoreCase("Updated", user.getId())).willReturn(null);
    given(savedSearchRepository.save(any(SavedSearch.class))).willAnswer(invocation -> invocation.getArgument(0));

    SavedSearch result = service.updateSavedSearch(1L, request);

    assertEquals(1L, result.getId());
    assertEquals("Updated", result.getName());
    assertEquals("description", result.getDescription());
    assertEquals(List.of("long"), result.getDisplayedFieldsLong());
    assertEquals(List.of("short"), result.getDisplayedFieldsShort());
    verify(searchJoinRepository).deleteBySearchId(1L);
    verify(searchJoinRepository).save(any(SearchJoin.class));
  }

  @Test
  @DisplayName("updateSavedSearch throws duplicate name")
  void updateSavedSearchThrowsDuplicateName() {
    SavedSearch original = savedSearch(1L, "Old", user);
    SavedSearch duplicate = savedSearch(2L, "Duplicate", user);
    duplicate.setStatus(Status.active);

    SearchCandidateRequest candidateRequest = new SearchCandidateRequest();

    UpdateSavedSearchRequest request = updateSearchRequest("Duplicate");
    request.setSearchCandidateRequest(candidateRequest);

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(original));
    given(savedSearchRepository.findByNameIgnoreCase("Duplicate", user.getId()))
        .willReturn(duplicate);

    assertThrows(EntityExistsException.class, () -> service.updateSavedSearch(1L, request));
  }

  @Test
  @DisplayName("deleteSavedSearch marks owner's search deleted and renames it")
  void deleteSavedSearchMarksDeleted() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);

    given(savedSearchRepository.findByIdLoadAudit(1L)).willReturn(Optional.of(savedSearch));
    given(userService.getLoggedInUser()).willReturn(user);

    assertTrue(service.deleteSavedSearch(1L));
    assertEquals(Status.deleted, savedSearch.getStatus());
    assertEquals("__deleted__Search", savedSearch.getName());
    verify(savedSearchRepository).save(savedSearch);
  }

  @Test
  @DisplayName("deleteSavedSearch throws when deleting another user's search")
  void deleteSavedSearchThrowsForOtherUser() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user(99L));

    given(savedSearchRepository.findByIdLoadAudit(1L)).willReturn(Optional.of(savedSearch));
    given(userService.getLoggedInUser()).willReturn(user);

    assertThrows(InvalidRequestException.class, () -> service.deleteSavedSearch(1L));
  }

  @Test
  @DisplayName("deleteSavedSearch returns false when missing")
  void deleteSavedSearchReturnsFalseWhenMissing() {
    given(savedSearchRepository.findByIdLoadAudit(1L)).willReturn(Optional.empty());
    given(userService.getLoggedInUser()).willReturn(user);

    assertFalse(service.deleteSavedSearch(1L));
  }

  @Test
  @DisplayName("deleteSavedSearch returns false when not logged in")
  void deleteSavedSearchReturnsFalseWhenNotLoggedIn() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);

    given(savedSearchRepository.findByIdLoadAudit(1L)).willReturn(Optional.of(savedSearch));
    given(userService.getLoggedInUser()).willReturn(null);

    assertFalse(service.deleteSavedSearch(1L));
  }

  @Test
  @DisplayName("addSharedUser and removeSharedUser update users")
  void addAndRemoveSharedUser() {
    SavedSearch savedSearch = savedSearch(1L, "Shared", user);
    User sharedUser = user(20L);

    UpdateSharingRequest request = new UpdateSharingRequest();
    request.setUserId(20L);

    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));
    given(userRepository.findById(20L)).willReturn(Optional.of(sharedUser));
    given(savedSearchRepository.save(savedSearch)).willReturn(savedSearch);

    assertSame(savedSearch, service.addSharedUser(1L, request));
    assertTrue(savedSearch.getUsers().contains(sharedUser));

    assertSame(savedSearch, service.removeSharedUser(1L, request));
    assertFalse(savedSearch.getUsers().contains(sharedUser));

    verify(savedSearchRepository, times(2)).save(savedSearch);
  }

  @Test
  @DisplayName("addSharedUser throws when search missing")
  void addSharedUserThrowsWhenSearchMissing() {
    UpdateSharingRequest request = new UpdateSharingRequest();
    request.setUserId(20L);

    given(savedSearchRepository.findById(1L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.addSharedUser(1L, request));
  }

  @Test
  @DisplayName("addSharedUser throws when user missing")
  void addSharedUserThrowsWhenUserMissing() {
    SavedSearch savedSearch = savedSearch(1L, "Shared", user);

    UpdateSharingRequest request = new UpdateSharingRequest();
    request.setUserId(20L);

    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));
    given(userRepository.findById(20L)).willReturn(Optional.empty());

    assertThrows(NoSuchObjectException.class, () -> service.addSharedUser(1L, request));
  }

  @Test
  @DisplayName("addWatcher adds watcher when under limit")
  void addWatcherAddsWatcher() {
    SavedSearch savedSearch = savedSearch(1L, "Watched", user);

    UpdateWatchingRequest request = new UpdateWatchingRequest();
    request.setUserId(20L);

    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));
    given(savedSearchRepository.findUserWatchedSearches(20L)).willReturn(Set.of());
    given(savedSearchRepository.save(savedSearch)).willReturn(savedSearch);

    SavedSearch result = service.addWatcher(1L, request);

    assertSame(savedSearch, result);
    assertTrue(savedSearch.getWatcherUserIds().contains(20L));
  }

  @Test
  @DisplayName("addWatcher throws when user already watches ten searches")
  void addWatcherThrowsWhenLimitReached() {
    SavedSearch savedSearch = savedSearch(99L, "Target", user);
    Set<SavedSearch> watches = new HashSet<>();
    for (long i = 0; i < 10; i++) {
      watches.add(savedSearch(i, "Watch" + i, user));
    }

    UpdateWatchingRequest request = new UpdateWatchingRequest();
    request.setUserId(20L);

    given(savedSearchRepository.findById(99L)).willReturn(Optional.of(savedSearch));
    given(savedSearchRepository.findUserWatchedSearches(20L)).willReturn(watches);

    assertThrows(InvalidRequestException.class, () -> service.addWatcher(99L, request));
  }

  @Test
  @DisplayName("removeWatcher removes watcher")
  void removeWatcherRemovesWatcher() {
    SavedSearch savedSearch = savedSearch(1L, "Watched", user);
    savedSearch.addWatcher(20L);

    UpdateWatchingRequest request = new UpdateWatchingRequest();
    request.setUserId(20L);

    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));
    given(savedSearchRepository.save(savedSearch)).willReturn(savedSearch);

    service.removeWatcher(1L, request);

    assertFalse(savedSearch.getWatcherUserIds().contains(20L));
  }

  @Test
  @DisplayName("getSelectionList creates missing selection list")
  void getSelectionListCreatesMissingList() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    SalesforceJobOpp job = new SalesforceJobOpp();
    job.setSfId("JOB-1");
    savedSearch.setSfJobOpp(job);

    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));
    given(userRepository.findById(10L)).willReturn(Optional.of(user));
    given(savedListRepository.findSelectionList(1L, 10L)).willReturn(Optional.empty());
    given(savedListRepository.save(any(SavedList.class))).willAnswer(invocation -> {
      SavedList saved = invocation.getArgument(0);
      saved.setId(100L);
      return saved;
    });

    SavedList result = service.getSelectionList(1L, 10L);

    assertEquals(100L, result.getId());
    assertEquals("_SelectionListUser10Search1", result.getName());
    assertSame(savedSearch, result.getSavedSearch());
    assertSame(savedSearch, result.getSavedSearchSource());
    assertSame(job, result.getSfJobOpp());
  }

  @Test
  @DisplayName("getSelectionList clears stale job from existing list")
  void getSelectionListClearsStaleJob() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    SavedList savedList = savedList(2L, "Selection", user);

    SalesforceJobOpp staleJob = new SalesforceJobOpp();
    staleJob.setSfId("OLD");
    savedList.setSfJobOpp(staleJob);

    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));
    given(userRepository.findById(10L)).willReturn(Optional.of(user));
    given(savedListRepository.findSelectionList(1L, 10L)).willReturn(Optional.of(savedList));
    given(savedListRepository.save(savedList)).willReturn(savedList);

    SavedList result = service.getSelectionList(1L, 10L);

    assertSame(savedList, result);
    assertNull(savedList.getSfJobOpp());
    verify(savedListRepository).save(savedList);
  }

  @Test
  @DisplayName("getSelectionList syncs changed job onto existing list")
  void getSelectionListSyncsChangedJob() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    SavedList savedList = savedList(2L, "Selection", user);

    SalesforceJobOpp newJob = new SalesforceJobOpp();
    newJob.setSfId("NEW");
    SalesforceJobOpp oldJob = new SalesforceJobOpp();
    oldJob.setSfId("OLD");

    savedSearch.setSfJobOpp(newJob);
    savedList.setSfJobOpp(oldJob);

    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));
    given(userRepository.findById(10L)).willReturn(Optional.of(user));
    given(savedListRepository.findSelectionList(1L, 10L)).willReturn(Optional.of(savedList));
    given(savedListRepository.save(savedList)).willReturn(savedList);

    SavedList result = service.getSelectionList(1L, 10L);

    assertSame(savedList, result);
    assertSame(newJob, savedList.getSfJobOpp());
    verify(savedListRepository).save(savedList);
  }

  @Test
  @DisplayName("getSelectionList does not save when existing job is already synced")
  void getSelectionListDoesNotSaveWhenJobAlreadySynced() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    SavedList savedList = savedList(2L, "Selection", user);

    SalesforceJobOpp job = new SalesforceJobOpp();
    job.setSfId("SAME");

    savedSearch.setSfJobOpp(job);
    savedList.setSfJobOpp(job);

    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));
    given(userRepository.findById(10L)).willReturn(Optional.of(user));
    given(savedListRepository.findSelectionList(1L, 10L)).willReturn(Optional.of(savedList));

    SavedList result = service.getSelectionList(1L, 10L);

    assertSame(savedList, result);
    verify(savedListRepository, never()).save(savedList);
  }

  @Test
  @DisplayName("getSelectionListForLoggedInUser throws when not logged in")
  void getSelectionListForLoggedInUserThrowsWhenNotLoggedIn() {
    given(userService.getLoggedInUser()).willReturn(null);

    assertThrows(InvalidSessionException.class, () -> service.getSelectionListForLoggedInUser(1L));
  }

//    @Test
//    @DisplayName("setCandidateContext marks only selected candidates")
//    void setCandidateContextMarksOnlySelectedCandidates() {
//        Candidate selected = candidate(1L);
//        Candidate notSelected = candidate(2L);
//
//        SavedSearch savedSearch = savedSearch(10L, "Search", user);
//        SavedList selectionList = savedList(20L, "Selection", user);
//        addCandidateToSavedList(selectionList, selected);
//
//        given(userService.getLoggedInUser()).willReturn(user);
//        given(savedSearchRepository.findById(10L)).willReturn(Optional.of(savedSearch));
//        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
//        given(savedListRepository.findSelectionList(10L, user.getId())).willReturn(Optional.of(selectionList));
//
//        service.setCandidateContext(10L, List.of(selected, notSelected));
//
//        assertEquals(20L, selected.getContextSavedListId());
//        assertNull(notSelected.getContextSavedListId());
//    }

  @Test
  @DisplayName("updateCandidateContextNote delegates only when selection list exists")
  void updateCandidateContextNoteDelegatesWhenSelectionListExists() {
    SavedList list = savedList(20L, "Selection", user);
    UpdateCandidateContextNoteRequest request = new UpdateCandidateContextNoteRequest();

    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.findSelectionList(1L, user.getId())).willReturn(Optional.of(list));

    service.updateCandidateContextNote(1L, request);

    verify(candidateSavedListService).updateCandidateContextNote(20L, request);
  }

  @Test
  @DisplayName("updateCandidateContextNote does nothing without logged in user")
  void updateCandidateContextNoteDoesNothingWithoutUser() {
    given(userService.getLoggedInUser()).willReturn(null);

    service.updateCandidateContextNote(1L, new UpdateCandidateContextNoteRequest());

    verify(candidateSavedListService, never()).updateCandidateContextNote(anyLong(), any());
  }

  @Test
  @DisplayName("updateCandidateContextNote does nothing without selection list")
  void updateCandidateContextNoteDoesNothingWithoutSelectionList() {
    given(userService.getLoggedInUser()).willReturn(user);
    given(savedListRepository.findSelectionList(1L, user.getId())).willReturn(Optional.empty());

    service.updateCandidateContextNote(1L, new UpdateCandidateContextNoteRequest());

    verify(candidateSavedListService, never()).updateCandidateContextNote(anyLong(), any());
  }

  @Test
  @DisplayName("updateDescription saves description")
  void updateDescriptionSavesDescription() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    UpdateCandidateSourceDescriptionRequest request =
        new UpdateCandidateSourceDescriptionRequest();
    request.setDescription("new description");

    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));

    service.updateDescription(1L, request);

    assertEquals("new description", savedSearch.getDescription());
    verify(savedSearchRepository).save(savedSearch);
  }

  @Test
  @DisplayName("updateDescription throws when saved search missing")
  void updateDescriptionThrowsWhenMissing() {
    given(savedSearchRepository.findById(1L)).willReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> service.updateDescription(1L, new UpdateCandidateSourceDescriptionRequest())
    );
  }

  @Test
  @DisplayName("updateDisplayedFieldPaths updates non-null fields only")
  void updateDisplayedFieldPathsUpdatesNonNullFields() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    savedSearch.setDisplayedFieldsShort(List.of("old-short"));

    UpdateDisplayedFieldPathsRequest request = new UpdateDisplayedFieldPathsRequest();
    request.setDisplayedFieldsLong(List.of("new-long"));

    given(savedSearchRepository.findById(1L)).willReturn(Optional.of(savedSearch));

    service.updateDisplayedFieldPaths(1L, request);

    assertEquals(List.of("new-long"), savedSearch.getDisplayedFieldsLong());
    assertEquals(List.of("old-short"), savedSearch.getDisplayedFieldsShort());
    verify(savedSearchRepository).save(savedSearch);
  }

  @Test
  @DisplayName("updateDisplayedFieldPaths throws when missing")
  void updateDisplayedFieldPathsThrowsWhenMissing() {
    given(savedSearchRepository.findById(1L)).willReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> service.updateDisplayedFieldPaths(1L, new UpdateDisplayedFieldPathsRequest())
    );
  }

  @Test
  @DisplayName("includesElasticSearch returns true for root simple query")
  void includesElasticSearchReturnsTrueForRootSimpleQuery() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    savedSearch.setSimpleQueryString("developer");
    savedSearch.setSearchJoins(new HashSet<>());

    given(savedSearchRepository.findByIdLoadSearchJoins(1L)).willReturn(Optional.of(savedSearch));

    assertTrue(service.includesElasticSearch(1L));
  }

  @Test
  @DisplayName("includesElasticSearch follows joined saved searches")
  void includesElasticSearchFollowsJoinedSearch() {
    SavedSearch root = savedSearch(1L, "Root", user);
    SavedSearch child = savedSearch(2L, "Child", user);
    child.setSimpleQueryString("nurse");

    SearchJoin join = new SearchJoin();
    join.setChildSavedSearch(child);
    join.setSearchType(SearchType.and);
    root.setSearchJoins(Set.of(join));
    child.setSearchJoins(new HashSet<>());

    given(savedSearchRepository.findByIdLoadSearchJoins(1L)).willReturn(Optional.of(root));
    given(savedSearchRepository.findByIdLoadSearchJoins(2L)).willReturn(Optional.of(child));

    assertTrue(service.includesElasticSearch(1L));
  }

  @Test
  @DisplayName("includesElasticSearch returns false when no saved search has simple query")
  void includesElasticSearchReturnsFalse() {
    SavedSearch root = savedSearch(1L, "Root", user);
    root.setSearchJoins(new HashSet<>());

    given(savedSearchRepository.findByIdLoadSearchJoins(1L)).willReturn(Optional.of(root));

    assertFalse(service.includesElasticSearch(1L));
  }

  @Test
  @DisplayName("status and UNHCR list converters cover null and non-null values")
  void statusConvertersCoverNullAndValues() {
    assertNull(service.getStatusListAsString(null));
    assertEquals(
        "active,pending",
        service.getStatusListAsString(List.of(CandidateStatus.active, CandidateStatus.pending))
    );
    assertEquals(
        List.of(CandidateStatus.active, CandidateStatus.pending),
        service.getStatusListFromString("active,pending")
    );
    assertNull(service.getStatusListFromString(null));

    assertNull(service.getUnhcrStatusListAsString(null));
    assertEquals(
        "MandateRefugee,NotRegistered",
        service.getUnhcrStatusListAsString(List.of(
            UnhcrStatus.MandateRefugee,
            UnhcrStatus.NotRegistered
        ))
    );
    assertEquals(
        List.of(UnhcrStatus.MandateRefugee, UnhcrStatus.NotRegistered),
        service.getUnhcrStatusListFromString("MandateRefugee,NotRegistered")
    );
    assertNull(service.getUnhcrStatusListFromString(null));
  }

  @Test
  @DisplayName("extractFetchSQL includes major filters")
  void extractFetchSqlIncludesMajorFilters() {
    SearchCandidateRequest request = richSearchRequest();
    request.setSavedSearchId(1L);

    Candidate excluded = candidate(99L);
    User restrictedUser = user(10L);

    String sql = service.extractFetchSQL(request, restrictedUser, Set.of(excluded), true);

    assertTrue(sql.startsWith("select distinct candidate.id"));
    assertTrue(sql.contains("candidate.status in ('active','pending')"));
    assertTrue(sql.contains("candidate.candidate_number in ('CN1','CN''2')"));
    assertTrue(sql.contains("candidate_occupation.occupation_id in (11,12)"));
    assertTrue(sql.contains("candidate_occupation.years_experience >= 2"));
    assertTrue(sql.contains("candidate_occupation.years_experience <= 7"));
    assertTrue(sql.contains("candidate.id not in (99)"));
    assertTrue(sql.contains("candidate.nationality_id not in (21,22)"));
    assertTrue(sql.contains("candidate.country_id in (31,32)"));
    assertTrue(sql.contains("users.partner_id in (41)"));
    assertTrue(sql.contains("candidate.survey_type_id in (51)"));
    assertTrue(sql.contains("lower(candidate.rego_referrer_param) like 'ref'"));
    assertTrue(sql.contains("lower(candidate.rego_utm_campaign) like 'campaign'"));
    assertTrue(sql.contains("lower(candidate.rego_utm_source) like 'source'"));
    assertTrue(sql.contains("lower(candidate.rego_utm_medium) like 'medium'"));
    assertTrue(sql.contains("candidate.unhcr_status in ('MandateRefugee')"));
    assertTrue(sql.contains("education_level.level >= 3"));
    assertTrue(sql.contains("education_level.level <= 5"));
    assertTrue(sql.contains("mini_intake_completed_date is not null"));
    assertTrue(sql.contains("full_intake_completed_date is null"));
    assertTrue(sql.contains("candidate.potential_duplicate = true"));
    assertTrue(sql.contains("major_id in (61,62)"));
    assertTrue(sql.contains(
        "candidate.id in (select candidate_id from candidate_saved_list where saved_list_id in (71))"
    ));
    assertTrue(sql.contains(
        "not (candidate.id in (select candidate_id from candidate_saved_list where saved_list_id = 81)"
    ));
    assertTrue(sql.contains("exists (select 1 from candidate_language"));
  }

  @Test
  @DisplayName("extractFetchSQL uses source countries when request has no countries")
  void extractFetchSqlUsesUserSourceCountries() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setSavedSearchId(1L);
    request.setIncludePendingTermsCandidates(true);

    User restrictedUser = user(10L);
    restrictedUser.setSourceCountries(Set.of(country(1L), country(2L)));

    String sql = service.extractFetchSQL(request, restrictedUser, null, false);

    assertTrue(sql.contains("candidate.country_id in (1,2)"));
  }

  @Test
  @DisplayName("extractFetchSQL throws circular reference for self join")
  void extractFetchSqlThrowsCircularReference() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setSavedSearchId(1L);
    request.setSearchJoinRequests(List.of(new SearchJoinRequest(1L, "Self", SearchType.and)));

    assertThrows(CircularReferencedException.class, () -> service.extractFetchSQL(request));
  }

  @Test
  @DisplayName("convertToSavedSearch copies search request fields")
  void convertToSavedSearchCopiesFields() {
    SearchCandidateRequest searchRequest = richSearchRequest();

    Language otherLanguage = language(200L, "Dari");
    SavedList exclusionList = savedList(300L, "Exclude", user);

    UpdateSavedSearchRequest request = updateSearchRequest("Converted");
    request.setSearchCandidateRequest(searchRequest);
    request.setJobId(-1L);

    SavedSearch original = savedSearch(1L, "Original", user);
    original.setDescription("description");
    original.setDisplayedFieldsLong(List.of("long"));
    original.setDisplayedFieldsShort(List.of("short"));

    given(languageRepository.findById(200L)).willReturn(Optional.of(otherLanguage));
    given(savedListRepository.findById(300L)).willReturn(Optional.of(exclusionList));

    SavedSearch result = invokePrivate(
        "convertToSavedSearch",
        new Class<?>[] {SavedSearch.class, UpdateSavedSearchRequest.class},
        original,
        request
    );

    CandidateFilterByOpps expectedOppFilter = searchRequest.getCandidateFilterByOpps();

    assertEquals("Converted", result.getName());
    assertEquals("description", result.getDescription());
    assertEquals(List.of("long"), result.getDisplayedFieldsLong());
    assertEquals(List.of("short"), result.getDisplayedFieldsShort());
    assertNull(result.getSfJobOpp());
    assertEquals("developer", result.getKeyword());
    assertSame(otherLanguage, result.getOtherLanguage());
    assertSame(exclusionList, result.getExclusionList());
    assertEquals(expectedOppFilter.getAnyOpps(), result.getAnyOpps());
    assertEquals(expectedOppFilter.getClosedOpps(), result.getClosedOpps());
    assertEquals(expectedOppFilter.getRelocatedOpps(), result.getRelocatedOpps());
  }

  @Test
  @DisplayName("convertToSearchCandidateRequest restricts countries to user source countries")
  void convertToSearchCandidateRequestRestrictsCountries() {
    Country allowed = country(31L);
    user.setSourceCountries(Set.of(allowed));

    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    savedSearch.setCountryIds("31,999");
    savedSearch.setSearchJoins(new HashSet<>());

    given(userService.getLoggedInUser()).willReturn(user);

    SearchCandidateRequest result = invokePrivate(
        "convertToSearchCandidateRequest",
        new Class<?>[] {SavedSearch.class},
        savedSearch
    );

    assertEquals(List.of(31L), result.getCountryIds());
  }

  @Test
  @DisplayName("searchCandidates by saved search uses reviewed branch for unverified filter")
  void searchCandidatesBySavedSearchUsesReviewedBranch() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    savedSearch.setSearchJoins(new HashSet<>());

    SavedSearchGetRequest request = new SavedSearchGetRequest();
    request.setReviewStatusFilter(List.of(ReviewStatus.unverified));
    request.setPageNumber(0);
    request.setPageSize(10);

    Page<Candidate> page = new PageImpl<>(List.of(candidate(1L)));

    given(savedSearchRepository.findByIdLoadSearchJoins(1L)).willReturn(Optional.of(savedSearch));
    given(candidateRepository.findReviewedCandidatesBySavedSearchId(
        eq(1L), any(), eq(request.getPageRequestWithoutSort())))
        .willReturn(page);

    assertSame(page, service.searchCandidates(1L, request));
  }

  @Test
  @DisplayName("searchCandidateDtos by saved search uses reviewed dto branch")
  void searchCandidateDtosBySavedSearchUsesReviewedBranch() {
    SavedSearch savedSearch = savedSearch(1L, "Search", user);
    savedSearch.setSearchJoins(new HashSet<>());

    Candidate candidate = candidate(1L);
    CandidateReadDto dto = mock(CandidateReadDto.class);

    SavedSearchGetRequest request = new SavedSearchGetRequest();
    request.setReviewStatusFilter(List.of(ReviewStatus.unverified));
    request.setPageNumber(0);
    request.setPageSize(10);

    Page<Candidate> page = new PageImpl<>(List.of(candidate));

    given(savedSearchRepository.findByIdLoadSearchJoins(1L)).willReturn(Optional.of(savedSearch));
    given(candidateRepository.findReviewedCandidatesBySavedSearchId(
        eq(1L), any(), eq(request.getPageRequestWithoutSort())))
        .willReturn(page);
    given(candidateDtoFetchService.fetchByIds(List.of(1L))).willReturn(Map.of(1L, dto));

    Page<CandidateReadDto> result = service.searchCandidateDtos(1L, request);

    assertEquals(1, result.getTotalElements());
    assertSame(dto, result.getContent().get(0));
  }

  @Test
  @DisplayName("doSQL-backed search returns sorted candidate page")
  void sqlBackedSearchReturnsCandidates() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setSavedSearchId(1L);
    request.setPageNumber(0);
    request.setPageSize(10);
    request.setIncludePendingTermsCandidates(true);

    Candidate candidate = candidate(10L);

    stubSqlCandidateSearch(List.of(10L), List.of(candidate), 1L);

    Page<Candidate> result = service.searchCandidates(request);

    assertEquals(1, result.getTotalElements());
    assertSame(candidate, result.getContent().get(0));
  }

  @Test
  @DisplayName("searchCandidateDtos delegates SQL and count SQL to dto fetch service")
  void searchCandidateDtosDelegatesToDtoFetchService() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setSavedSearchId(1L);
    request.setPageNumber(0);
    request.setPageSize(10);
    request.setIncludePendingTermsCandidates(true);

    Page<CandidateReadDto> page = new PageImpl<>(List.of(mock(CandidateReadDto.class)));

    given(userService.getLoggedInUser()).willReturn(null);
    given(candidateDtoFetchService.fetchPage(anyString(), anyString(), eq(request.getPageRequest())))
        .willReturn(page);

    assertSame(page, service.searchCandidateDtos(request));
  }

  @Test
  @DisplayName("exportToCsv writes title and candidate rows")
  void exportToCsvWritesRows() throws ExportFailedException {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setSavedSearchId(1L);
    request.setIncludePendingTermsCandidates(true);

    Candidate candidate = candidate(10L);

    stubSqlCandidateSearch(List.of(10L), List.of(candidate), 1L);

    given(candidateService.getExportTitles()).willReturn(new String[] {"id"});
    given(candidateService.getExportCandidateStrings(candidate)).willReturn(new String[] {"10"});

    StringWriter stringWriter = new StringWriter();

    service.exportToCsv(request, new PrintWriter(stringWriter));

    assertTrue(stringWriter.toString().contains("id"));
    assertTrue(stringWriter.toString().contains("10"));
  }

  @Test
  @DisplayName("notifySearchWatchers sends alert when search processing fails")
  void notifySearchWatchersSendsAlertOnFailure() {
    given(savedSearchRepository.findByWatcherIdsIsNotNull())
        .willThrow(new RuntimeException("boom"));

    service.notifySearchWatchers();

    verify(emailHelper).sendAlert(contains("Watcher notification failure"), any(RuntimeException.class));
  }

  @Test
  @DisplayName("updateSuggestedSearchesNames renames suggested searches")
  void updateSuggestedSearchesNamesRenamesSearches() {
    SalesforceJobOpp job = new SalesforceJobOpp();
    job.setName("New Job");

    SavedSearch search = savedSearch(1L, "Old Job - candidates", user);
    job.setSuggestedSearches(Set.of(search));

    service.updateSuggestedSearchesNames(job, "Old Job");

    assertEquals("New Job - candidates", search.getName());
    verify(savedSearchRepository).save(search);
  }

  private void stubSqlCandidateSearch(List<Long> ids, List<Candidate> candidates, long total) {
    given(userService.getLoggedInUser()).willReturn(null);

    given(entityManager.createNativeQuery(startsWith("select distinct candidate.id")))
        .willReturn(idsQuery);
    given(idsQuery.setFirstResult(anyInt())).willReturn(idsQuery);
    given(idsQuery.setMaxResults(anyInt())).willReturn(idsQuery);

    // Important:
    // First page returns the test ids.
    // All later pages return empty results, so exportToCsv can terminate.
    given(idsQuery.getResultList()).willReturn(ids, List.of());

    given(candidateRepository.findByIds(anyList())).willAnswer(invocation -> {
      List<Long> requestedIds = invocation.getArgument(0);
      return requestedIds.isEmpty() ? List.of() : candidates;
    });

    given(entityManager.createNativeQuery(startsWith("select count")))
        .willReturn(countQuery);

    // Keep returning the real total for each page-count query.
    given(countQuery.getSingleResult()).willReturn(total);
  }

  private SearchCandidateRequest richSearchRequest() {
    SearchCandidateRequest request = new SearchCandidateRequest();
    request.setSavedSearchId(1L);
    request.setSimpleQueryString("software developer");
    request.setKeyword("developer");
    request.setStatuses(List.of(CandidateStatus.active, CandidateStatus.pending));
    request.setCandidateNumbers(List.of(" CN1 ", "CN'2", " "));
    request.setOccupationIds(List.of(11L, 12L));
    request.setMinYrs(2);
    request.setMaxYrs(7);
    request.setNationalityIds(List.of(21L, 22L));
    request.setNationalitySearchType(SearchType.not);
    request.setCountryIds(List.of(31L, 32L));
    request.setCountrySearchType(SearchType.or);
    request.setPartnerIds(List.of(41L));
    request.setSurveyTypeIds(List.of(51L));
    request.setRegoReferrerParam(" Ref ");
    request.setRegoUtmCampaign(" Campaign ");
    request.setRegoUtmSource(" Source ");
    request.setRegoUtmMedium(" Medium ");
    request.setGender(Gender.female);
    request.setUnhcrStatuses(List.of(UnhcrStatus.MandateRefugee));
    request.setMinEducationLevel(3);
    request.setMaxEducationLevel(5);
    request.setMiniIntakeCompleted(true);
    request.setFullIntakeCompleted(false);
    request.setPotentialDuplicate(true);
    request.setEducationMajorIds(List.of(61L, 62L));
    request.setEnglishMinSpokenLevel(1);
    request.setEnglishMinWrittenLevel(2);
    request.setOtherLanguageId(200L);
    request.setOtherMinSpokenLevel(3);
    request.setOtherMinWrittenLevel(4);
    request.setListAnyIds(List.of(71L));
    request.setListAnySearchType(SearchType.or);
    request.setListAllIds(List.of(81L, 82L));
    request.setListAllSearchType(SearchType.not);
    request.setExclusionListId(300L);
    request.setIncludePendingTermsCandidates(false);
    request.setCandidateFilterByOpps(CandidateFilterByOpps.values()[0]);
    request.setPageNumber(0);
    request.setPageSize(10);
    return request;
  }

  private UpdateSavedSearchRequest updateSearchRequest(String name) {
    UpdateSavedSearchRequest request = new UpdateSavedSearchRequest();
    request.setName(name);
    request.setDefaultSearch(false);
    request.setFixed(false);
    request.setReviewable(false);
    request.setSavedSearchType(SavedSearchType.other);
    return request;
  }

  private User user(Long id) {
    User user = new User();
    user.setId(id);
    user.setSourceCountries(new HashSet<>());
    user.setSharedSearches(new HashSet<>());
    user.setSharedLists(new HashSet<>());
    return user;
  }

  private SavedSearch savedSearch(Long id, String name, User createdBy) {
    SavedSearch savedSearch = new SavedSearch();
    savedSearch.setId(id);
    savedSearch.setName(name);
    savedSearch.setCreatedBy(createdBy);
    savedSearch.setStatus(Status.active);
    savedSearch.setDefaultSearch(false);
    savedSearch.setFixed(false);
    savedSearch.setReviewable(false);
    savedSearch.setSavedSearchType(SavedSearchType.other);
    savedSearch.setSearchJoins(new HashSet<>());
    savedSearch.setWatcherUserIds(new HashSet<>());
    return savedSearch;
  }

  private SavedList savedList(Long id, String name, User createdBy) {
    SavedList savedList = new SavedList();
    savedList.setId(id);
    savedList.setName(name);
    savedList.setCreatedBy(createdBy);
    savedList.setCandidateSavedLists(new HashSet<>());
    return savedList;
  }

  private void addCandidateToSavedList(SavedList savedList, Candidate candidate) {
    CandidateSavedList candidateSavedList = new CandidateSavedList();
    candidateSavedList.setSavedList(savedList);
    candidateSavedList.setCandidate(candidate);
    savedList.setCandidateSavedLists(Set.of(candidateSavedList));
  }

  private Candidate candidate(Long id) {
    Candidate candidate = new Candidate();
    candidate.setId(id);
    candidate.setCreatedDate(OffsetDateTime.now());
    return candidate;
  }

  private Language language(Long id, String name) {
    Language language = new Language();
    language.setId(id);
    language.setName(name);
    return language;
  }

  private LanguageLevel languageLevel(int level, String name) {
    LanguageLevel languageLevel = new LanguageLevel();
    languageLevel.setLevel(level);
    languageLevel.setName(name);
    return languageLevel;
  }

  private EducationLevel educationLevel(int level, String name) {
    EducationLevel educationLevel = new EducationLevel();
    educationLevel.setLevel(level);
    educationLevel.setName(name);
    return educationLevel;
  }

  private Country country(Long id) {
    Country country = new Country();
    country.setId(id);
    return country;
  }

  @SuppressWarnings("unchecked")
  private <T> T invokePrivate(String name, Class<?>[] parameterTypes, Object... args) {
    try {
      Method method = SavedSearchServiceImpl.class.getDeclaredMethod(name, parameterTypes);
      method.setAccessible(true);
      return (T) method.invoke(service, args);
    } catch (Exception e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException runtimeException) {
        throw runtimeException;
      }
      if (cause instanceof Error error) {
        throw error;
      }
      throw new RuntimeException(e);
    }
  }

  private static void setPrivateField(Object target, String fieldName, Object value) {
    try {
      Field field = SavedSearchServiceImpl.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private static long getPrivateLong(Object target, String fieldName) {
    try {
      Field field = SavedSearchServiceImpl.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field.getLong(target);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}