/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.service.db.impl;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.opencsv.CSVWriter;
import io.jsonwebtoken.lang.Collections;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.tctalent.server.exception.CircularReferencedException;
import org.tctalent.server.exception.CountryRestrictionException;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ExportFailedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateFilterByOpps;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.EducationLevel;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.LanguageLevel;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.ReviewStatus;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.SavedSearchType;
import org.tctalent.server.model.db.SearchJoin;
import org.tctalent.server.model.db.SearchType;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.model.es.CandidateEs;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CandidateReviewStatusRepository;
import org.tctalent.server.repository.db.CandidateSpecification;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.repository.db.EducationLevelRepository;
import org.tctalent.server.repository.db.EducationMajorRepository;
import org.tctalent.server.repository.db.LanguageLevelRepository;
import org.tctalent.server.repository.db.LanguageRepository;
import org.tctalent.server.repository.db.OccupationRepository;
import org.tctalent.server.repository.db.PartnerRepository;
import org.tctalent.server.repository.db.SavedListRepository;
import org.tctalent.server.repository.db.SavedSearchRepository;
import org.tctalent.server.repository.db.SavedSearchSpecification;
import org.tctalent.server.repository.db.SearchJoinRepository;
import org.tctalent.server.repository.db.SurveyTypeRepository;
import org.tctalent.server.repository.db.UserRepository;
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
import org.tctalent.server.service.db.CandidateSavedListService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.EducationMajorService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.SalesforceJobOppService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;

@Service
@RequiredArgsConstructor
public class SavedSearchServiceImpl implements SavedSearchService {

  private static final Logger log = LoggerFactory.getLogger(SavedSearchServiceImpl.class);

  private final CandidateRepository candidateRepository;
  private final CandidateService candidateService;
  private final CandidateReviewStatusRepository candidateReviewStatusRepository;
  private final CandidateSavedListService candidateSavedListService;
  private final CountryService countryService;
  private final PartnerService partnerService;
  private final ElasticsearchOperations elasticsearchOperations;
  private final EmailHelper emailHelper;
  private final UserRepository userRepository;
  private final UserService userService;
  private final SalesforceJobOppService salesforceJobOppService;
  private final SavedListRepository savedListRepository;
  private final SavedListService savedListService;
  private final SavedSearchRepository savedSearchRepository;
  private final SearchJoinRepository searchJoinRepository;
  private final LanguageLevelRepository languageLevelRepository;
  private final LanguageRepository languageRepository;
  private final CountryRepository countryRepository;
  private final PartnerRepository partnerRepository;
  private final OccupationRepository occupationRepository;
  private final OccupationService occupationService;
  private final SurveyTypeRepository surveyTypeRepository;
  private final EducationMajorRepository educationMajorRepository;
  private final EducationMajorService educationMajorService;
  private final EducationLevelRepository educationLevelRepository;

  /**
   * These are the default candidate statuses to included in searches when no statuses are
   * specified. Basically all "inactive" statuses such as draft, deleted, employed and ineligible.
   */
  private static final List<CandidateStatus> defaultSearchStatuses = new ArrayList<>(
      EnumSet.complementOf(EnumSet.of(
          CandidateStatus.autonomousEmployment,
          CandidateStatus.deleted,
          CandidateStatus.draft,
          CandidateStatus.employed,
          CandidateStatus.ineligible,
          CandidateStatus.withdrawn
      )));

  @Override
  public List<SavedSearch> search(SearchSavedSearchRequest request) {
    final User loggedInUser = userService.getLoggedInUser();

    List<SavedSearch> savedSearches;
    //If requesting watches
    if (request.getWatched() != null && request.getWatched()) {
      Set<SavedSearch> watches;
      if (loggedInUser == null) {
        //Just provide empty set
        watches = new HashSet<>();
      } else {
        watches = savedSearchRepository.findUserWatchedSearches(loggedInUser.getId());
      }
      savedSearches = new ArrayList<>(watches);
    } else {
      savedSearches = savedSearchRepository.findAll(
          SavedSearchSpecification.buildSearchQuery(request, loggedInUser));
    }
    logFoundSearches(savedSearches.size() + " savedSearches");

    for (SavedSearch savedSearch : savedSearches) {
      savedSearch.parseType();
    }

    return savedSearches;
  }

  @Override
  public Page<SavedSearch> searchPaged(SearchSavedSearchRequest request) {
    final User loggedInUser = userService.getLoggedInUser();

    Page<SavedSearch> savedSearches;
    //If requesting watches
    if (request.getWatched() != null && request.getWatched()) {
      Set<SavedSearch> watches;
      if (loggedInUser == null) {
        //Just provide empty set
        watches = new HashSet<>();
      } else {
        watches = savedSearchRepository.findUserWatchedSearches(loggedInUser.getId());
      }
      savedSearches = new PageImpl<>(
          new ArrayList<>(watches), request.getPageRequest(),
          watches.size());
    } else {
      savedSearches = savedSearchRepository.findAll(
          SavedSearchSpecification.buildSearchQuery(request, loggedInUser),
          request.getPageRequest());
    }
    logFoundSearches(savedSearches.getTotalElements() + " savedSearches");

    savedSearches.forEach(SavedSearch::parseType);

    return savedSearches;
  }

  @Override
  public Page<Candidate> searchCandidates(
      long savedSearchId, SavedSearchGetRequest request)
      throws NoSuchObjectException {

    // Get saved searches and merge requested into it (notably the page request)
    SearchCandidateRequest searchRequest = loadSavedSearch(savedSearchId);
    searchRequest.merge(request);

    //If user filters on unverified statuses we bypass performing a full search
    //Simply return candidates that the user has already reviewed as verified and/or rejected
    if (unverifiedCandidates(request)) {
      return reviewedCandidates(searchRequest);
    }

    // Do the search and add in any selections.
    final Page<Candidate> candidates = doSearchCandidates(searchRequest);
    markUserSelectedCandidates(savedSearchId, candidates);

    return candidates;
  }

  /**
   * Determines if the status filter is unverified.
   */
  private boolean unverifiedCandidates(SavedSearchGetRequest req) {
    List<ReviewStatus> l = req.getReviewStatusFilter();
    return l != null && l.contains(ReviewStatus.unverified);
  }

  private Page<Candidate> reviewedCandidates(SearchCandidateRequest request) {
    return candidateRepository.findReviewedCandidatesBySavedSearchId(
        request.getSavedSearchId(),
        request.getReviewStatusFilter(),
        request.getPageRequestWithoutSort());
  }

  @Override
  public @NotNull Set<Long> searchCandidates(long savedSearchId)
      throws NoSuchObjectException {

    SearchCandidateRequest searchRequest = loadSavedSearch(savedSearchId);

    Set<Candidate> excludedCandidates =
        computeCandidatesExcludedFromSearchCandidateRequest(searchRequest);

    // Modify request, doing standard defaults
    addDefaultsToSearchCandidateRequest(searchRequest);

    Set<Long> candidateIds = new HashSet<>();
    String simpleQueryString = searchRequest.getSimpleQueryString();
    if (simpleQueryString != null && !simpleQueryString.isEmpty()) {
      // This is an elasticsearch request

      // Combine any joined searches (which will all be processed as elastic)
      BoolQueryBuilder boolQueryBuilder = processElasticRequest(searchRequest,
          simpleQueryString, excludedCandidates);

      NativeSearchQuery query = new NativeSearchQueryBuilder()
          .withQuery(boolQueryBuilder)
          .build()
          .setPageable(Pageable.unpaged());

      SearchHits<CandidateEs> hits = elasticsearchOperations.search(
          query, CandidateEs.class, IndexCoordinates.of("candidates"));

      //Get candidate ids from the returned results
      for (SearchHit<CandidateEs> hit : hits) {
        candidateIds.add(hit.getContent().getMasterId());
      }
    } else {
      //Compute the non-elastic search query
      final Specification<Candidate> query = computeQuery(searchRequest, excludedCandidates);

      List<Candidate> candidates = candidateRepository.findAll(query);

      for (Candidate candidate : candidates) {
        candidateIds.add(candidate.getId());
      }
    }
    logFoundSearches(candidateIds.size() + " candidates");
    return candidateIds;
  }

  /**
   * Added @Transactional to this method as it is calling another method (updateSavedSearch) which
   * requires the @Transactional annotation. Transaction needs to wrap the database modifying
   * operation (searchJoinRepository.deleteBySearchId(id)) or else an exception will be thrown.
   * See:
   * <a href="https://www.baeldung.com/jpa-transaction-required-exception">...</a>
   */
  @Override
  @Transactional
  public Page<Candidate> searchCandidates(SearchCandidateRequest request) {
    Page<Candidate> candidates;
    User user = userService.getLoggedInUser();
    if (user == null) {
      candidates = doSearchCandidates(request);
    } else {
      //Update default search
      SavedSearch defaultSavedSearch = getDefaultSavedSearch();
      Long savedSearchId = defaultSavedSearch.getId();
      UpdateSavedSearchRequest updateRequest = getUpdateSavedSearchRequest(
          request, defaultSavedSearch);

      //todo Need special method which only updates search part. Then don't need the above "no changes there" stuff
      updateSavedSearch(savedSearchId, updateRequest);

      candidates = doSearchCandidates(request);

      //Add in any selections
      markUserSelectedCandidates(savedSearchId, candidates);
    }

    return candidates;
  }

  private static UpdateSavedSearchRequest getUpdateSavedSearchRequest(
      SearchCandidateRequest request, SavedSearch defaultSavedSearch) {
    UpdateSavedSearchRequest updateRequest = new UpdateSavedSearchRequest();
    updateRequest.setSearchCandidateRequest(request);
    //Set other fields - no changes there
    updateRequest.setName(defaultSavedSearch.getName());
    updateRequest.setDefaultSearch(defaultSavedSearch.getDefaultSearch());
    updateRequest.setFixed(defaultSavedSearch.getFixed());
    updateRequest.setReviewable(defaultSavedSearch.getReviewable());
    updateRequest.setSavedSearchType(defaultSavedSearch.getSavedSearchType());
    updateRequest.setSavedSearchSubtype(defaultSavedSearch.getSavedSearchSubtype());
    return updateRequest;
  }

  /**
   * Mark the Candidate objects with any context associated with the selection list of the saved
   * search. This means that context fields (ie ContextNote) associated with the saved search will
   * be returned through the DtoBuilder if present.
   */
  @Override
  public void setCandidateContext(long savedSearchId, Iterable<Candidate> candidates) {
    User user = userService.getLoggedInUser();
    SavedList selectionList = null;
    if (user != null) {
      selectionList = getSelectionList(savedSearchId, user.getId());
    }
    if (selectionList != null) {
      //Only set context of selected candidates
      Set<Candidate> selectedCandidates = selectionList.getCandidates();
      //Loop through candidates we are considering
      for (Candidate candidate : candidates) {
        //Only selected candidates
        if (selectedCandidates.contains(candidate)) {
          candidate.setContextSavedListId(selectionList.getId());
        }
      }
    }
  }

  @Override
  public SearchCandidateRequest loadSavedSearch(long id) {
    SavedSearch savedSearch = this.savedSearchRepository.findByIdLoadSearchJoins(id)
        .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));
    savedSearch.parseType();
    return convertToSearchCandidateRequest(savedSearch);
  }

  @Override
  public SavedSearch getSavedSearch(long id) {
    SavedSearch savedSearch = this.savedSearchRepository
        .findByIdLoadUsers(id)
        .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));

    savedSearch.parseType();

    Map<Integer, String> languageLevelMap = languageLevelRepository.findAllActive().stream()
        .collect(
            Collectors.toMap(LanguageLevel::getLevel, LanguageLevel::getName, (l1, l2) -> l1));
    Map<Integer, String> educationLevelMap = educationLevelRepository.findAllActive().stream()
        .collect(
            Collectors.toMap(EducationLevel::getLevel, EducationLevel::getName, (l1, l2) -> l1));

    if (!StringUtils.isEmpty(savedSearch.getCountryIds())) {
      savedSearch.setCountryNames(
          countryRepository.getNamesForIds(getIdsFromString(savedSearch.getCountryIds())));
    }
    if (!StringUtils.isEmpty(savedSearch.getPartnerIds())) {
      savedSearch.setPartnerNames(
          partnerRepository.getNamesForIds(getIdsFromString(savedSearch.getPartnerIds())));
    }
    if (!StringUtils.isEmpty(savedSearch.getNationalityIds())) {
      savedSearch.setNationalityNames(
          countryRepository.getNamesForIds(getIdsFromString(savedSearch.getNationalityIds())));
    }
    if (!StringUtils.isEmpty(savedSearch.getOccupationIds())) {
      savedSearch.setOccupationNames(
          occupationRepository.getNamesForIds(getIdsFromString(savedSearch.getOccupationIds())));
    }
    if (!StringUtils.isEmpty(savedSearch.getEducationMajorIds())) {
      savedSearch.setEducationMajors(educationMajorRepository.getNamesForIds(
          getIdsFromString(savedSearch.getEducationMajorIds())));
    }
    if (!StringUtils.isEmpty(savedSearch.getSurveyTypeIds())) {
      savedSearch.setSurveyTypeNames(
          surveyTypeRepository.getNamesForIds(getIdsFromString(savedSearch.getSurveyTypeIds())));
    }
    if (savedSearch.getEnglishMinWrittenLevel() != null) {
      savedSearch.setEnglishWrittenLevel(
          languageLevelMap.get(savedSearch.getEnglishMinWrittenLevel()));
    }
    if (savedSearch.getEnglishMinSpokenLevel() != null) {
      savedSearch.setEnglishSpokenLevel(
          languageLevelMap.get(savedSearch.getEnglishMinSpokenLevel()));
    }
    if (savedSearch.getOtherMinSpokenLevel() != null) {
      savedSearch.setOtherSpokenLevel(languageLevelMap.get(savedSearch.getOtherMinSpokenLevel()));
    }
    if (savedSearch.getOtherMinWrittenLevel() != null) {
      savedSearch.setOtherWrittenLevel(languageLevelMap.get(savedSearch.getOtherMinWrittenLevel()));
    }
    if (savedSearch.getMinEducationLevel() != null) {
      savedSearch.setMinEducationLevelName(
          educationLevelMap.get(savedSearch.getMinEducationLevel()));
    }
    return savedSearch;

  }

  @Override
  public void clearSelection(long id, Long userId)
      throws InvalidRequestException, NoSuchObjectException {
    //Get the selection list for this user and saved search.
    SavedList selectionList = getSelectionList(id, userId);

    //Clear the list.
    candidateSavedListService.clearSavedList(selectionList.getId());
  }

  @Override
  public SavedSearch createFromDefaultSavedSearch(
      CreateFromDefaultSavedSearchRequest request)
      throws NoSuchObjectException {

    final User loggedInUser = userService.getLoggedInUser();
    if (loggedInUser == null) {
      throw new InvalidSessionException("Not logged in");
    }

    String name;
    //Get name either from the specified saved list, or the specified name.
    final long savedListId = request.getSavedListId();
    if (savedListId == 0) {
      name = request.getName();
    } else {
      SavedList savedList = savedListService.get(savedListId);
      name = savedList.getName();
    }

    SavedSearch defaultSavedSearch = getDefaultSavedSearch();

    //Delete any existing saved search with the given name.
    SavedSearch existing =
        savedSearchRepository.findByNameIgnoreCase(
            name, loggedInUser.getId());
    if (existing != null) {
      deleteSavedSearch(existing.getId());
    }

    UpdateSavedSearchRequest createRequest = new UpdateSavedSearchRequest();
    createRequest.setName(name);
    createRequest.setSfJoblink(request.getSfJoblink());

    //Default to job type
    createRequest.setSavedSearchType(SavedSearchType.job);

    //Other fields eg fixed, reviewable etc to default values

    //Copy search params from default search
    createRequest.setSearchCandidateRequest(
        convertToSearchCandidateRequest(defaultSavedSearch));

    SavedSearch createdSearch = createSavedSearch(createRequest);

    //Clear search attributes and selections of default saved search
    clearSavedSearch(defaultSavedSearch, loggedInUser);

    return createdSearch;
  }

  private void clearSavedSearch(SavedSearch savedSearch, User user) {
    //Clear defaultSavedSearch search attributes and clear its selection.
    clearSelection(savedSearch.getId(), user.getId());

    //Clear search attributes by passing in an empty SearchCandidateRequest
    populateSearchAttributes(savedSearch, new SearchCandidateRequest());

    savedSearchRepository.save(savedSearch);
  }

  @Override
  @Transactional
  public SavedSearch createSavedSearch(UpdateSavedSearchRequest request)
      throws EntityExistsException {
    SavedSearch defaultSavedSearch = getDefaultSavedSearch();
    return createSavedSearchBase(request, defaultSavedSearch);
  }

  private SavedSearch createDefaultSavedSearch(User user) {
    UpdateSavedSearchRequest request = new UpdateSavedSearchRequest();
    request.setName(constructDefaultSearchName(user));
    request.setSavedSearchType(SavedSearchType.other);
    request.setDefaultSearch(true);
    return createSavedSearchBase(request, null);
  }

  private SavedSearch createSavedSearchBase(
      UpdateSavedSearchRequest request, @Nullable SavedSearch template) {
    SavedSearch savedSearch = convertToSavedSearch(template, request);
    final User loggedInUser = userService.getLoggedInUser();
    if (loggedInUser != null) {
      checkDuplicates(null, request.getName(), loggedInUser.getId());
      savedSearch.setAuditFields(loggedInUser);
    }

    savedSearch = savedSearchRepository.save(savedSearch);
    addSearchJoins(request, savedSearch);

    if (template != null) {
      //Copy across the user's selections (including context notes)
      //of the template saved search.
      copySelectionsAndContextNotes(template, savedSearch, true);
    }

    return savedSearch;
  }

  private void copySelectionsAndContextNotes(@NonNull SavedSearch fromSavedSearch,
      SavedSearch toSavedSearch, boolean clearFromSavedSearch) {
    final User loggedInUser = userService.getLoggedInUser();
    if (loggedInUser != null) {
      Long fromSavedSearchId = fromSavedSearch.getId();

      //Get the original selection list.
      SavedList fromSelectionList =
          getSelectionList(fromSavedSearchId, loggedInUser.getId());

      //Get the selection list of the new saved search
      SavedList toSelectionList =
          getSelectionList(toSavedSearch.getId(), loggedInUser.getId());

      //Copy the contents of the original selection list to the selection list of the
      //destination search.
      candidateSavedListService.copyContents(
          fromSelectionList, toSelectionList, false);

      if (clearFromSavedSearch) {
        //Clear search attributes and selections of original saved search
        clearSavedSearch(fromSavedSearch, loggedInUser);
      }
    }
  }

  @Override
  @Transactional
  public SavedSearch updateSavedSearch(long id, UpdateSavedSearchRequest request)
      throws EntityExistsException {
    final User loggedInUser = userService.getLoggedInUser();
    if (loggedInUser == null) {
      throw new InvalidSessionException("Not logged in");
    }

    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));
    if (request.getSearchCandidateRequest() == null) {
      // If a saved search isn't global and belongs to loggedInUser, allow changes
      if (!savedSearch.getFixed() || savedSearch.getCreatedBy().getId()
          .equals(loggedInUser.getId())) {
        savedSearch.setName(request.getName());
        savedSearch.setFixed(request.getFixed());
        savedSearch.setReviewable(request.getReviewable());
        savedSearch.setSfJobOpp(
            salesforceJobOppService.getOrCreateJobOppFromLink(request.getSfJoblink()));

        savedSearch.setType(request.getSavedSearchType(), request.getSavedSearchSubtype());
        return savedSearchRepository.save(savedSearch);
      } else {
        log.warn(
            "Can't update saved search " + savedSearch.getId() + " - " + savedSearch.getName());
        return savedSearch;
      }
    }

    SavedSearch newSavedSearch = convertToSavedSearch(savedSearch, request);

    //delete and recreate all joined searches
    searchJoinRepository.deleteBySearchId(id);

    newSavedSearch.setId(id);
    newSavedSearch = addSearchJoins(request, newSavedSearch);

    newSavedSearch.setAuditFields(loggedInUser);
    checkDuplicates(id, request.getName(), loggedInUser.getId());
    return savedSearchRepository.save(newSavedSearch);
  }

  @Override
  @Transactional
  public boolean deleteSavedSearch(long id) {
    SavedSearch savedSearch = savedSearchRepository.findByIdLoadAudit(id).orElse(null);
    final User loggedInUser = userService.getLoggedInUser();

    if (savedSearch != null && loggedInUser != null) {

      // Check if saved search was created by the user deleting.
      if (savedSearch.getCreatedBy().getId().equals(loggedInUser.getId())) {
        savedSearch.setStatus(Status.deleted);

        //Change name so that that name can be reused
        savedSearch.setName("__deleted__" + savedSearch.getName());
        savedSearchRepository.save(savedSearch);
        return true;
      } else {
        throw new InvalidRequestException("You can't delete other user's saved searches.");
      }
    }
    return false;
  }

  // Search export
  @Override
  public void exportToCsv(
      long savedSearchId, SavedSearchGetRequest request, PrintWriter writer)
      throws ExportFailedException {
    SearchCandidateRequest searchRequest = loadSavedSearch(savedSearchId);

    //Merge the SavedSearchGetRequest - notably the page request - in to
    //the standard saved search request.
    searchRequest.merge(request);
    exportToCsv(searchRequest, writer);
  }

  @Override
  public void exportToCsv(SearchCandidateRequest request, PrintWriter writer)
      throws ExportFailedException {
    try (CSVWriter csvWriter = new CSVWriter(writer)) {
      csvWriter.writeNext(candidateService.getExportTitles());

      request.setPageNumber(0);
      request.setPageSize(500);
      boolean hasMore = true;
      while (hasMore) {
        Page<Candidate> result = doSearchCandidates(request);
        setCandidateContext(request.getSavedSearchId(), result);
        for (Candidate candidate : result.getContent()) {
          csvWriter.writeNext(candidateService.getExportCandidateStrings(candidate));
        }

        if ((long) result.getNumber() * request.getPageSize() < result.getTotalElements()) {
          request.setPageNumber(request.getPageNumber() + 1);
        } else {
          hasMore = false;
        }
      }
    } catch (IOException e) {
      throw new ExportFailedException(e);
    }
  }

  @Override
  @Transactional
  public SavedSearch addSharedUser(long id, UpdateSharingRequest request) {
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));

    savedSearch.parseType();

    final Long userID = request.getUserId();
    User user = userRepository.findById(userID)
        .orElseThrow(() -> new NoSuchObjectException(User.class, userID));

    savedSearch.addUser(user);

    return savedSearchRepository.save(savedSearch);
  }

  @Override
  @Transactional
  public SavedSearch removeSharedUser(long id, UpdateSharingRequest request) {
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));

    savedSearch.parseType();

    final Long userID = request.getUserId();
    User user = userRepository.findById(userID)
        .orElseThrow(() -> new NoSuchObjectException(User.class, userID));

    savedSearch.removeUser(user);

    return savedSearchRepository.save(savedSearch);
  }

  @Override
  public SavedSearch addWatcher(long id, UpdateWatchingRequest request) {
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));

    savedSearch.parseType();

    Set<SavedSearch> searches = savedSearchRepository.findUserWatchedSearches(request.getUserId());
    if (searches.size() >= 10) {
      String s = searches.stream().map(SavedSearch::getName).sorted()
          .collect(Collectors.joining(","));
      throw new InvalidRequestException("More than 10 watches. Currently watching " + s);
    }

    savedSearch.addWatcher(request.getUserId());

    return savedSearchRepository.save(savedSearch);
  }

  @Override
  public SavedSearch removeWatcher(long id, UpdateWatchingRequest request) {
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));

    savedSearch.parseType();
    savedSearch.removeWatcher(request.getUserId());

    return savedSearchRepository.save(savedSearch);
  }

  @Override
  public @NotNull SavedSearch getDefaultSavedSearch()
      throws NoSuchObjectException {
    //Check that we have a logged in user.
    final User loggedInUser = userService.getLoggedInUser();
    if (loggedInUser == null) {
      throw new InvalidSessionException("Not logged in");
    }

    SavedSearch savedSearch =
        savedSearchRepository.findDefaultSavedSearch(loggedInUser.getId())
            .orElse(null);
    if (savedSearch == null) {
      //Create a default saved search for logged in user
      savedSearch = createDefaultSavedSearch(loggedInUser);
    } else {
      savedSearch.parseType();
    }

    return savedSearch;
  }

  @Override
  public @NotNull SavedList getSelectionList(long id, Long userId)
      throws NoSuchObjectException {
    //Check that saved search and user are valid.
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchObjectException(User.class, userId));

    SavedList savedList = savedListRepository.findSelectionList(id, userId)
        .orElse(null);
    if (savedList == null) {
      savedList = new SavedList();
      savedList.setSavedSearch(savedSearch);

      //Setting the savedSearchSource on all selection lists allows
      //the standard list savedSearchSource copy logic to work for
      //selection lists as well as normal lists.
      //See code in SavedListServiceImpl.createSavedList
      savedList.setSavedSearchSource(savedSearch);

      savedList.setCreatedBy(user);
      savedList.setCreatedDate(OffsetDateTime.now());
      savedList.setName(constructSelectionListName(user, savedSearch));
      savedList.setSfJobOpp(savedSearch.getSfJobOpp());

      savedList = savedListRepository.save(savedList);
    } else {
      //Keep SavedSearch sfJobLink in sync with its selection list.
      if (savedSearch.getSfJobOpp() == null) {
        //If not both null
        if (savedList.getSfJobOpp() != null) {
          savedList.setSfJobOpp(null);
          savedList = savedListRepository.save(savedList);
        }
      } else {
        //Non null SavedSearch Job Opp. Check if it is different from Saved List JobOpp
        final SalesforceJobOpp savedListJobOpp = savedList.getSfJobOpp();
        if (savedListJobOpp == null ||
            !savedSearch.getSfJobOpp().getSfId().equals(savedListJobOpp.getSfId())) {
          savedList.setSfJobOpp(savedSearch.getSfJobOpp());
          savedList = savedListRepository.save(savedList);
        }
      }
    }

    return savedList;
  }

  @Override
  public @NotNull SavedList getSelectionListForLoggedInUser(long id)
      throws InvalidSessionException {

    final User loggedInUser = userService.getLoggedInUser();
    if (loggedInUser == null) {
      throw new InvalidSessionException("Not logged in");
    }

    return getSelectionList(id, loggedInUser.getId());
  }

  @Override
  public boolean isEmpty(long id) throws NoSuchObjectException {
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));

    final Set<Long> candidateIds = searchCandidates(savedSearch.getId());
    return candidateIds.isEmpty();
  }

  @Override
  public void updateCandidateContextNote(long id, UpdateCandidateContextNoteRequest request) {
    final User loggedInUser = userService.getLoggedInUser();
    if (loggedInUser != null) {
      SavedList savedList = savedListRepository
          .findSelectionList(id, loggedInUser.getId())
          .orElse(null);
      if (savedList != null) {
        candidateSavedListService
            .updateCandidateContextNote(savedList.getId(), request);
      }
    }
  }

  @Override
  public void updateDescription(long id, UpdateCandidateSourceDescriptionRequest request)
      throws NoSuchObjectException {
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));
    savedSearch.setDescription(request.getDescription());
    savedSearchRepository.save(savedSearch);
  }

  @Override
  public void updateDisplayedFieldPaths(
      long id, UpdateDisplayedFieldPathsRequest request)
      throws NoSuchObjectException {
    SavedSearch savedSearch = savedSearchRepository.findById(id)
        .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));
    if (request.getDisplayedFieldsLong() != null) {
      savedSearch.setDisplayedFieldsLong(request.getDisplayedFieldsLong());
    }
    if (request.getDisplayedFieldsShort() != null) {
      savedSearch.setDisplayedFieldsShort(request.getDisplayedFieldsShort());
    }
    savedSearchRepository.save(savedSearch);
  }

  private Specification<Candidate> computeQuery(
      SearchCandidateRequest request, @Nullable Collection<Candidate> excludedCandidates) {
    //There may be no logged in user if the search is called by the
    //overnight Watcher process.
    User user = userService.getLoggedInUser();

    //This list is initialized with the main saved search id, but can be
    //added to by addQuery below when the search is built on other
    //searches. The idea is to avoid circular dependencies between searches.
    //For example, in the simplest case we don't want a saved search
    //to be based on itself.
    List<Long> searchIds = new ArrayList<>();
    if (request.getSavedSearchId() != null) {
      searchIds.add(request.getSavedSearchId());
    }

    Specification<Candidate> query = CandidateSpecification
        .buildSearchQuery(request, user, excludedCandidates);
    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(
        request.getSearchJoinRequests())) {
      for (SearchJoinRequest searchJoinRequest : request.getSearchJoinRequests()) {
        query = addQuery(query, searchJoinRequest, searchIds);
      }
    }
    return query;
  }

  private BoolQueryBuilder computeElasticQuery(
      SearchCandidateRequest request, @Nullable String simpleQueryString,
      @Nullable Collection<Candidate> excludedCandidates) {
    /*
       Constructing a filtered simple query that looks like this:

       GET /candidates/_search
        {
          "query": {
            "bool": {
              "must": [
                { "simple_query_string": {"query":"the +jet+ engine"}}
              ],
              "filter": [
                { "term":  { "status": "pending" }},
                { "range":  { "minEnglishSpokenLevel": {"gte": 2}}}
              ]
            }
          }
        }
     */

    User user = userService.getLoggedInUser();

    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

    // Not every base search will contain an elastic search term, since we're processing
    // joined regular searches here too — so we need a safe escape here
    if (simpleQueryString != null && simpleQueryString.length() > 0) {
      // Create a simple query string builder from the given string
      SimpleQueryStringBuilder simpleQueryStringBuilder =
          QueryBuilders.simpleQueryStringQuery(simpleQueryString);

      // The simple query will be part of a composite query containing filters
      boolQueryBuilder.must(simpleQueryStringBuilder);
    }

    //Add filters - each filter must return true for a hit
    //(Note: Filters are different from "Must" entries only in that
    //they don't affect the Elasticsearch score)

    //Add a TermsQuery filter for each multiselect request - eg
    //countries and nationalities. A match against any one of the
    //multiselected values will result in the filter returning true.
    //There is also a TermQuery which takes only one value.

    //English levels
    // done
    Integer minSpokenLevel = request.getEnglishMinSpokenLevel();
    if (minSpokenLevel != null) {
      boolQueryBuilder =
          addElasticRangeFilter(boolQueryBuilder,
              "minEnglishSpokenLevel",
              minSpokenLevel, null);
    }
    // done
    Integer minWrittenLevel = request.getEnglishMinWrittenLevel();
    if (minWrittenLevel != null) {
      boolQueryBuilder =
          addElasticRangeFilter(boolQueryBuilder,
              "minEnglishWrittenLevel",
              minWrittenLevel, null);
    }

    //Other languages
    Long otherLanguageId = request.getOtherLanguageId();
    if (otherLanguageId != null) {
      Optional<Language> otherLanguage = languageRepository.findById(request.getOtherLanguageId());
      if (otherLanguage.isPresent()) {

        BoolQueryBuilder nestedQueryBuilder = QueryBuilders.boolQuery().must(
            QueryBuilders.termQuery("otherLanguages.name.keyword", otherLanguage.get().getName()));

        // done
        Integer minOtherSpokenLevel = request.getOtherMinSpokenLevel();
        if (minOtherSpokenLevel != null) {
          nestedQueryBuilder =
              addElasticRangeFilter(nestedQueryBuilder,
                  "otherLanguages.minSpokenLevel",
                  minOtherSpokenLevel, null);
        }
        // done
        Integer minOtherWrittenLevel = request.getOtherMinWrittenLevel();
        if (minOtherWrittenLevel != null) {
          nestedQueryBuilder =
              addElasticRangeFilter(nestedQueryBuilder,
                  "otherLanguages.minWrittenLevel",
                  minOtherWrittenLevel, null);
        }

        boolQueryBuilder = boolQueryBuilder.filter(
            QueryBuilders.nestedQuery("otherLanguages", nestedQueryBuilder, ScoreMode.Avg));
      }

    }

    //Exclude given candidates
    // done.
    if (excludedCandidates != null && excludedCandidates.size() > 0) {
      List<Object> candidateIds = excludedCandidates.stream()
          .map(Candidate::getId).collect(Collectors.toList());
      boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
          SearchType.not, "masterId", candidateIds);
    }

    //Occupations
    // done
    final List<Long> occupationIds = request.getOccupationIds();
    if (occupationIds != null) {
      //Look up names from ids.
      List<Object> reqOccupations = new ArrayList<>();
      for (Long id : occupationIds) {
        final Occupation occupation = occupationService.getOccupation(id);
        reqOccupations.add(occupation.getName());
      }
      boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
          null, "occupations.keyword", reqOccupations);
    }

    //Countries - need to take account of source country restrictions
    // done
    final List<Long> countryIds = request.getCountryIds();
    List<Object> reqCountries = new ArrayList<>();
    // If countryIds is NOT EMPTY we can just accept them because the options
    // presented to the user will be limited to the allowed source countries
    if (!Collections.isEmpty(countryIds)) {
      //Look up country names from ids.
      for (Long countryId : countryIds) {
        final Country country = countryService.getCountry(countryId);
        reqCountries.add(country.getName());
      }
    } else if (user != null && !Collections.isEmpty(user.getSourceCountries())) {
      user.getSourceCountries().forEach(c -> reqCountries.add(c.getName()));
    }

    if (!reqCountries.isEmpty()) {
      boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
          request.getCountrySearchType(),
          "country.keyword", reqCountries);
    }

    //Partners
    //done
    final List<Long> partnerIds = request.getPartnerIds();
    if (partnerIds != null) {
      //Look up names from ids.
      List<Object> reqPartners = new ArrayList<>();
      for (Long id : partnerIds) {
        final Partner partner = partnerService.getPartner(id);
        reqPartners.add(partner.getAbbreviation());
      }
      boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
          null, "partner.keyword", reqPartners);
    }

    //Nationalities
    //done
    final List<Long> nationalityIds = request.getNationalityIds();
    if (nationalityIds != null) {
      //Look up names from ids.
      List<Object> reqNationalities = new ArrayList<>();
      for (Long id : nationalityIds) {
        final Country nationality = countryService.getCountry(id);
        reqNationalities.add(nationality.getName());
      }
      boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
          request.getNationalitySearchType(),
          "nationality.keyword", reqNationalities);
    }

    //Statuses
    //done
    List<CandidateStatus> statuses = request.getStatuses();
    if (statuses != null) {
      List<String> reqStatuses = statuses.stream()
          .map(CandidateStatus::name)
          .toList();

      boolQueryBuilder =
          addElasticTermFilter(boolQueryBuilder,
              null, "status.keyword", reqStatuses);
    }

    //Referrer
    // done
    String referrer = request.getRegoReferrerParam();
    if (referrer != null) {
      boolQueryBuilder = boolQueryBuilder.filter(
          QueryBuilders.termQuery("regoReferrerParam", referrer));
    }

    //Gender
    // done
    Gender gender = request.getGender();
    if (gender != null) {
      boolQueryBuilder = boolQueryBuilder.filter(
          QueryBuilders.termQuery("gender", gender.name()));
    }

    //Education Level (minimum)
    // done
    Integer minEducationLevel = request.getMinEducationLevel();
    if (minEducationLevel != null) {
      boolQueryBuilder =
          addElasticRangeFilter(boolQueryBuilder,
              "maxEducationLevel",
              minEducationLevel, null);
    }

    //Educations
    // done
    final List<Long> educationMajorIds = request.getEducationMajorIds();
    if (educationMajorIds != null) {
      //Look up names from ids.
      List<Object> reqEducations = new ArrayList<>();
      for (Long id : educationMajorIds) {
        final EducationMajor educationMajor = educationMajorService.getEducationMajor(id);
        reqEducations.add(educationMajor.getName());
      }
      boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
          null, "educationMajors.keyword", reqEducations);
    }
    return boolQueryBuilder;
  }

  private static String constructDefaultSearchName(User user) {
    return "_DefaultSavedSearchForUser" + user.getId();
  }

  private static String constructSelectionListName(
      User user, SavedSearch savedSearch) {
    return "_SelectionListUser" + user.getId() +
        "Search" + savedSearch.getId();
  }

  /**
   * Checks whether the given user already has another saved search with this name - throwing
   * exception if it does
   *
   * @param savedSearchId Existing saved search - or null if none
   * @param name          Saved search name
   * @param userId        User
   * @throws EntityExistsException if saved search with this name already exists for this user
   */
  private void checkDuplicates(
      @Nullable Long savedSearchId, String name, Long userId) {
    SavedSearch existing =
        savedSearchRepository.findByNameIgnoreCase(name, userId);
    if (existing != null && existing.getStatus() != Status.deleted) {
      if (!existing.getId().equals(savedSearchId)) {
        throw new EntityExistsException("savedSearch");
      }
    }
  }

  private BoolQueryBuilder addElasticQuery(BoolQueryBuilder boolQueryBuilder,
      SearchJoinRequest searchJoinRequest, List<Long> savedSearchIds) {
    // We don't want searches built on themselves - this is also guarded against in frontend
    if (savedSearchIds.contains(searchJoinRequest.getSavedSearchId())) {
      throw new CircularReferencedException(searchJoinRequest.getSavedSearchId());
    }
    savedSearchIds.add(searchJoinRequest.getSavedSearchId());

    SearchCandidateRequest request = loadSavedSearch(searchJoinRequest.getSavedSearchId());

    // Get the keyword search term, if any
    String simpleStringQuery = request.getSimpleQueryString();

    // Compute the candidates that should be excluded from search
    Set<Candidate> excludeCandidates =
        computeCandidatesExcludedFromSearchCandidateRequest(request);

    // Each recursion, if any, is added to the query as an additional must clause
    boolQueryBuilder.must(
        computeElasticQuery(request, simpleStringQuery, excludeCandidates)
    );

    // Like addQuery(), this method uses recursion to get every nested SearchJoinRequest
    if (!request.getSearchJoinRequests().isEmpty()) {
      for (SearchJoinRequest joinRequest : request.getSearchJoinRequests()) {
        boolQueryBuilder = addElasticQuery(boolQueryBuilder, joinRequest, savedSearchIds);
      }
    }
    return boolQueryBuilder;
  }

  private Specification<Candidate> addQuery(Specification<Candidate> query,
      SearchJoinRequest searchJoinRequest, List<Long> savedSearchIds) {
    if (savedSearchIds.contains(searchJoinRequest.getSavedSearchId())) {
      throw new CircularReferencedException(searchJoinRequest.getSavedSearchId());
    }
    User user = userService.getLoggedInUser();
    //add id to list as do not want circular references
    savedSearchIds.add(searchJoinRequest.getSavedSearchId());
    //load saved search
    SearchCandidateRequest request = loadSavedSearch(searchJoinRequest.getSavedSearchId());
    Specification<Candidate> joinQuery = CandidateSpecification.buildSearchQuery(request, user,
        null);
    if (searchJoinRequest.getSearchType().equals(SearchType.and)) {
      query = Specification.where(query.and(joinQuery));
    } else {
      query = Specification.where(query.or(joinQuery));
    }
    if (!request.getSearchJoinRequests().isEmpty()) {
      for (SearchJoinRequest joinRequest : request.getSearchJoinRequests()) {
        query = addQuery(query, joinRequest, savedSearchIds);
      }
    }
    return query;

  }

  private SavedSearch addSearchJoins(UpdateSavedSearchRequest request, SavedSearch savedSearch) {
    Set<SearchJoin> searchJoins = new HashSet<>();
    if (request.getSearchCandidateRequest() != null) {
      if (!CollectionUtils.isEmpty(request.getSearchCandidateRequest().getSearchJoinRequests())) {
        for (SearchJoinRequest searchJoinRequest : request.getSearchCandidateRequest()
            .getSearchJoinRequests()) {
          SearchJoin searchJoin = new SearchJoin();
          searchJoin.setSavedSearch(savedSearch);
          searchJoin.setChildSavedSearch(
              savedSearchRepository.findById(searchJoinRequest.getSavedSearchId()).orElseThrow(
                  () -> new NoSuchObjectException(SavedSearch.class,
                      searchJoinRequest.getSavedSearchId())));
          searchJoin.setSearchType(searchJoinRequest.getSearchType());
          this.searchJoinRepository.save(searchJoin);
        }
        savedSearch.setSearchJoins(searchJoins);
      }
    }

    return savedSearch;
  }


  //---------------------------------------------------------------------------------------------------
  private SavedSearch convertToSavedSearch(
      @Nullable SavedSearch origSavedSearch, UpdateSavedSearchRequest request) {

    SavedSearch savedSearch = new SavedSearch();
    savedSearch.setName(request.getName());
    savedSearch.setFixed(request.getFixed());
    savedSearch.setDefaultSearch(request.getDefaultSearch());
    savedSearch.setReviewable(request.getReviewable());
    if (origSavedSearch != null) {
      savedSearch.setDescription(origSavedSearch.getDescription());
      savedSearch.setDisplayedFieldsLong(origSavedSearch.getDisplayedFieldsLong());
      savedSearch.setDisplayedFieldsShort(origSavedSearch.getDisplayedFieldsShort());
    }
    savedSearch.setSfJobOpp(
        salesforceJobOppService.getOrCreateJobOppFromLink(request.getSfJoblink()));

    savedSearch.setType(request.getSavedSearchType(), request.getSavedSearchSubtype());

    final SearchCandidateRequest searchCandidateRequest = request.getSearchCandidateRequest();
    populateSearchAttributes(savedSearch, searchCandidateRequest);

    return savedSearch;
  }

  private void markUserSelectedCandidates(@Nullable Long savedSearchId,
      Page<Candidate> candidates) {
    if (savedSearchId != null) {
      //Check for selection list to set the selected attribute on returned
      // candidates.
      SavedList selectionList = null;
      User user = userService.getLoggedInUser();
      if (user != null) {
        selectionList = getSelectionList(savedSearchId, user.getId());
      }
      if (selectionList != null) {
        Set<Candidate> selectedCandidates = selectionList.getCandidates();
        if (!selectedCandidates.isEmpty()) {
          for (Candidate candidate : candidates) {
            if (selectedCandidates.contains(candidate)) {
              candidate.setSelected(true);
            }
          }
        }
      }
    }
  }

  /**
   * Sends emails to any users watching searches who had new results over night.
   * <p/>
   * Has to be annotated as Transactional in order to create the "persistence context" (what the
   * underlying Hibernate calls a Session). This context is used to fetch lazily loaded attributes
   * (by auto generating other SQL calls on the database).
   * <p/>
   * When running searches from requests through the REST API, Spring automatically creates this
   * context - so you don't have to annotate all your REST API methods as Transactional. - JC
   */
  //Midnight GMT
  @Scheduled(cron = "0 1 0 * * ?", zone = "GMT")
  @SchedulerLock(name = "SavedSearchService_notifySearchWatchers", lockAtLeastFor = "PT23H", lockAtMostFor = "PT23H")
  @Transactional
  public void notifySearchWatchers() {
    String currentSearch = "";
    try {
      Set<SavedSearch> searches = savedSearchRepository.findByWatcherIdsIsNotNull();
      Map<Long, Set<SavedSearch>> userNotifications = new HashMap<>();

      log.info("Notify watchers: running " + searches.size() + " searches");

      int count = 0;

      OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);

      //Look through all watched searches looking for any that have candidates that were
      //created since yesterday.
      //Those are the searches that need to notify their watchers.
      for (SavedSearch savedSearch : searches) {

        count++;
        currentSearch = savedSearch.getName() + " (" + savedSearch.getId() + ")";
        log.info("Running search " + count + ": " + currentSearch);

        SearchCandidateRequest searchCandidateRequest =
            convertToSearchCandidateRequest(savedSearch);

        //Set up paging
        searchCandidateRequest.setPageNumber(0);
        //Short page is all we need - we are only going to look at first element
        searchCandidateRequest.setPageSize(1);

        Page<Candidate> candidates =
            doSearchCandidates(searchCandidateRequest);

        boolean newCandidates = false;
        if (candidates.getNumberOfElements() > 0) {
          //Get first (latest) candidate
          Candidate candidate = candidates.getContent().get(0);
          OffsetDateTime createdDate = candidate.getCreatedDate();
          newCandidates = createdDate.isAfter(yesterday);
        }

        if (newCandidates) {
          //Query has new results. Need to let watchers know
          Set<Long> watcherUserIds = savedSearch.getWatcherUserIds();
          for (Long watcherUserId : watcherUserIds) {
            Set<SavedSearch> userWatches = userNotifications
                .computeIfAbsent(watcherUserId, k -> new HashSet<>());
            userWatches.add(savedSearch);
          }
        }
      }

      //Construct and send emails
      for (Long userId : userNotifications.keySet()) {
        final Set<SavedSearch> savedSearches = userNotifications.get(userId);
        String s = savedSearches.stream()
            .map(SavedSearch::getName)
            .collect(Collectors.joining("/"));
        log.info("Tell user " + userId + " about searches " + s);
        User user = this.userRepository.findById(userId).orElse(null);
        if (user == null) {
          final String mess = "Unknown user watcher id " + userId + " watching searches " + s;
          log.warn(mess);
          emailHelper.sendAlert(mess);
        } else {
          emailHelper.sendWatcherEmail(user, savedSearches);
        }
      }
    } catch (Exception ex) {
      String mess = "Watcher notification failure (" + currentSearch + ")";
      log.error(mess, ex);
      emailHelper.sendAlert(mess, ex);
    }
  }

  private void populateSearchAttributes(
      SavedSearch savedSearch, SearchCandidateRequest request) {
    if (request != null) {
      savedSearch.setSimpleQueryString(request.getSimpleQueryString());
      savedSearch.setKeyword(request.getKeyword());
      savedSearch.setStatuses(getStatusListAsString(request.getStatuses()));
      savedSearch.setGender(request.getGender());
      savedSearch.setOccupationIds(getListAsString(request.getOccupationIds()));
      savedSearch.setMinYrs(request.getMinYrs());
      savedSearch.setMaxYrs(request.getMaxYrs());
      savedSearch.setRegoReferrerParam(request.getRegoReferrerParam());
      savedSearch.setPartnerIds(getListAsString(request.getPartnerIds()));
      savedSearch.setNationalityIds(
          getListAsString(request.getNationalityIds()));
      savedSearch.setNationalitySearchType(request.getNationalitySearchType());
      savedSearch.setCountryIds(getListAsString(request.getCountryIds()));
      savedSearch.setCountrySearchType(request.getCountrySearchType());
      savedSearch.setSurveyTypeIds(getListAsString(request.getSurveyTypeIds()));
      savedSearch.setEnglishMinSpokenLevel(request.getEnglishMinSpokenLevel());
      savedSearch.setEnglishMinWrittenLevel(request.getEnglishMinWrittenLevel());
      Optional<Language> language =
          request.getOtherLanguageId() != null ?
              languageRepository.findById(
                  request.getOtherLanguageId()) : Optional.empty();
      if (language.isPresent()) {
        savedSearch.setOtherLanguage(language.get());
      }

      Optional<SavedList> exclusionList =
          request.getExclusionListId() != null ?
              savedListRepository.findById(
                  request.getExclusionListId()) : Optional.empty();
      if (exclusionList.isPresent()) {
        savedSearch.setExclusionList(exclusionList.get());
      }

      savedSearch.setOtherMinSpokenLevel(request.getOtherMinSpokenLevel());
      savedSearch.setOtherMinWrittenLevel(request.getOtherMinWrittenLevel());
      savedSearch.setLastModifiedFrom(request.getLastModifiedFrom());
      savedSearch.setLastModifiedTo(request.getLastModifiedTo());
//        savedSearch.setCreatedFrom(request.getSearchCandidateRequest().getRegisteredFrom());
//        savedSearch.setCreatedTo(request.getSearchCandidateRequest().getRegisteredTo());
      savedSearch.setMinAge(request.getMinAge());
      savedSearch.setMaxAge(request.getMaxAge());
      savedSearch.setMinEducationLevel(request.getMinEducationLevel());
      savedSearch.setEducationMajorIds(
          getListAsString(request.getEducationMajorIds()));
      savedSearch.setMiniIntakeCompleted(request.getMiniIntakeCompleted());
      savedSearch.setFullIntakeCompleted(request.getFullIntakeCompleted());

      //Save Boolean filters corresponding to enum name
      final CandidateFilterByOpps candidateFilterByOpps = request.getCandidateFilterByOpps();
      if (candidateFilterByOpps == null) {
        savedSearch.setAnyOpps(null);
        savedSearch.setClosedOpps(null);
        savedSearch.setRelocatedOpps(null);
      } else {
        savedSearch.setAnyOpps(candidateFilterByOpps.getAnyOpps());
        savedSearch.setClosedOpps(candidateFilterByOpps.getClosedOpps());
        savedSearch.setRelocatedOpps(candidateFilterByOpps.getRelocatedOpps());
      }
    }
  }

  private SearchCandidateRequest convertToSearchCandidateRequest(SavedSearch search)
      throws CountryRestrictionException {
    User user = userService.getLoggedInUser();
    SearchCandidateRequest searchCandidateRequest = new SearchCandidateRequest();
    searchCandidateRequest.setSavedSearchId(search.getId());
    searchCandidateRequest.setSimpleQueryString(search.getSimpleQueryString());
    searchCandidateRequest.setKeyword(search.getKeyword());
    searchCandidateRequest.setStatuses(getStatusListFromString(search.getStatuses()));
    searchCandidateRequest.setGender(search.getGender());
    searchCandidateRequest.setOccupationIds(getIdsFromString(search.getOccupationIds()));
    searchCandidateRequest.setMinYrs(search.getMinYrs());
    searchCandidateRequest.setMaxYrs(search.getMaxYrs());
    searchCandidateRequest.setRegoReferrerParam(search.getRegoReferrerParam());
    searchCandidateRequest.setPartnerIds(getIdsFromString(search.getPartnerIds()));
    searchCandidateRequest.setNationalityIds(getIdsFromString(search.getNationalityIds()));
    searchCandidateRequest.setSurveyTypeIds(getIdsFromString(search.getSurveyTypeIds()));
    searchCandidateRequest.setNationalitySearchType(search.getNationalitySearchType());
    searchCandidateRequest.setCountrySearchType(search.getCountrySearchType());

    // Check if the saved search countries match the source countries of the user
    List<Long> requestCountries = getIdsFromString(search.getCountryIds());

    // if a user has source country restrictions AND IF the request has countries selected
    if (user != null
        && user.getSourceCountries().size() > 0
        && search.getCountryIds() != null) {
      List<Long> sourceCountries = user.getSourceCountries().stream()
          .map(Country::getId)
          .collect(Collectors.toList());
      //find the users source countries in the saved search countries
      requestCountries.retainAll(sourceCountries);
      //todo removed to fix default search showing source countries that no longer belong. Find an alternative solution.
//            if(requestCountries.size() == 0){
//                //if no source countries in the saved search countries throw an error
//                throw new CountryRestrictionException("You don't have access to any of the countries in the Saved Search: " + request.getName());
//            }
    }
    searchCandidateRequest.setCountryIds(requestCountries);

    searchCandidateRequest.setEnglishMinSpokenLevel(search.getEnglishMinSpokenLevel());
    searchCandidateRequest.setEnglishMinWrittenLevel(search.getEnglishMinWrittenLevel());
    searchCandidateRequest.setExclusionListId(
        search.getExclusionList() != null ? search.getExclusionList().getId() : null);
    searchCandidateRequest.setOtherLanguageId(
        search.getOtherLanguage() != null ? search.getOtherLanguage().getId() : null);
    searchCandidateRequest.setOtherMinSpokenLevel(search.getOtherMinSpokenLevel());
    searchCandidateRequest.setOtherMinWrittenLevel(search.getOtherMinWrittenLevel());
    searchCandidateRequest.setLastModifiedFrom(search.getLastModifiedFrom());
    searchCandidateRequest.setLastModifiedTo(search.getLastModifiedTo());
//        searchCandidateRequest.setRegisteredFrom(request.getCreatedFrom());
//        searchCandidateRequest.setRegisteredTo(request.getCreatedTo());
    searchCandidateRequest.setMinAge(search.getMinAge());
    searchCandidateRequest.setMaxAge(search.getMaxAge());
    searchCandidateRequest.setMinEducationLevel(search.getMinEducationLevel());
    searchCandidateRequest.setEducationMajorIds(getIdsFromString(search.getEducationMajorIds()));
    searchCandidateRequest.setMiniIntakeCompleted(search.getMiniIntakeCompleted());
    searchCandidateRequest.setFullIntakeCompleted(search.getFullIntakeCompleted());

    CandidateFilterByOpps candidateFilterByOpps = CandidateFilterByOpps.mapToEnum(
        search.getAnyOpps(), search.getClosedOpps(), search.getRelocatedOpps());
    searchCandidateRequest.setCandidateFilterByOpps(candidateFilterByOpps);

    List<SearchJoinRequest> searchJoinRequests = new ArrayList<>();
    for (SearchJoin searchJoin : search.getSearchJoins()) {
      searchJoinRequests.add(new SearchJoinRequest(searchJoin.getChildSavedSearch().getId(),
          searchJoin.getChildSavedSearch().getName(), searchJoin.getSearchType()));
    }
    searchCandidateRequest.setSearchJoinRequests(searchJoinRequests);

    return searchCandidateRequest;

  }


  String getListAsString(List<Long> ids) {
    return !CollectionUtils.isEmpty(ids) ? ids.stream().map(String::valueOf)
        .collect(Collectors.joining(",")) : null;
  }

  List<Long> getIdsFromString(String listIds) {
    return listIds != null ? Stream.of(listIds.split(","))
        .map(Long::parseLong)
        .collect(Collectors.toList()) : null;
  }

  String getStatusListAsString(List<CandidateStatus> statuses) {
    return !CollectionUtils.isEmpty(statuses) ? statuses.stream().map(String::valueOf)
        .collect(Collectors.joining(",")) : null;
  }

  List<CandidateStatus> getStatusListFromString(String statusList) {
    return statusList != null ? Stream.of(statusList.split(","))
        .map(s -> CandidateStatus.valueOf(s))
        .collect(Collectors.toList()) : null;
  }

  private Page<Candidate> doSearchCandidates(SearchCandidateRequest searchRequest) {

    Page<Candidate> candidates;

    // Compute the candidates which should be excluded from search
    Set<Candidate> excludedCandidates =
        computeCandidatesExcludedFromSearchCandidateRequest(searchRequest);

    // Modify request, doing standard defaults
    addDefaultsToSearchCandidateRequest(searchRequest);

    String simpleQueryString = searchRequest.getSimpleQueryString();
    if (simpleQueryString != null && simpleQueryString.length() > 0) {
      // This is an elasticsearch request

      // Combine any joined searches (which will all be processed as elastic)
      BoolQueryBuilder boolQueryBuilder = processElasticRequest(searchRequest,
          simpleQueryString, excludedCandidates);

      //Define sort from request
      PageRequest req = CandidateEs.convertToElasticSortField(searchRequest);

      log.info("Elasticsearch query:\n" + boolQueryBuilder);
      log.info("Elasticsearch sort:\n" + req);

      NativeSearchQuery query = new NativeSearchQueryBuilder()
          .withQuery(boolQueryBuilder)
          .withPageable(req)
          .build();

      SearchHits<CandidateEs> hits = elasticsearchOperations.search(
          query, CandidateEs.class, IndexCoordinates.of("candidates"));

      //Get candidate ids from the returned results - maintaining the sort
      //Avoid duplicates, but maintaining order by using a LinkedHashSet
      LinkedHashSet<Long> candidateIds = new LinkedHashSet<>();
      for (SearchHit<CandidateEs> hit : hits) {
        candidateIds.add(hit.getContent().getMasterId());
      }

      //Now fetch those candidates from the normal database
      //They will come back in random order
      List<Candidate> unsorted = candidateService.findByIds(candidateIds);
      //Put the results in a map indexed by the id
      Map<Long, Candidate> mapById = new HashMap<>();
      for (Candidate candidate : unsorted) {
        mapById.put(candidate.getId(), candidate);
      }
      //Now construct a candidate list sorted according to the original list of ids.
      List<Candidate> candidateList = new ArrayList<>();

      for (Long candidateId : candidateIds) {
        candidateList.add(mapById.get(candidateId));
      }

      candidates = new PageImpl<>(candidateList, searchRequest.getPageRequest(),
          hits.getTotalHits());

    } else {
      //Compute the non-elastic query
      Specification<Candidate> query = computeQuery(searchRequest, excludedCandidates);
      candidates = candidateRepository.findAll(query, searchRequest.getPageRequestWithoutSort());
    }
    logFoundSearches(candidates.getTotalElements() + " candidates");
    return candidates;
  }

  @NonNull
  private Set<Candidate> computeCandidatesExcludedFromSearchCandidateRequest(
      SearchCandidateRequest request) {
    Set<Candidate> excludedCandidates = new HashSet<>();

    final Long exclusionListId = request.getExclusionListId();
    if (exclusionListId != null) {
      SavedList exclusionList = savedListService.get(exclusionListId);
      excludedCandidates.addAll(exclusionList.getCandidates());
    }

    if (isNotEmpty(request.getReviewStatusFilter())) {
      //Exclude candidates who have been reviewed with statuses given in filter
      final Set<Candidate> candidatesToFilterOut =
          candidateReviewStatusRepository.findReviewedCandidatesForSearch(
              request.getSavedSearchId(), request.getReviewStatusFilter());

      excludedCandidates.addAll(candidatesToFilterOut);
    }
    return excludedCandidates;
  }

  private void addDefaultsToSearchCandidateRequest(SearchCandidateRequest request) {
    //Modify request, defaulting blank statuses
    List<CandidateStatus> requestedStatuses = request.getStatuses();
    if (requestedStatuses == null || requestedStatuses.isEmpty()) {
      request.setStatuses(defaultSearchStatuses);
    }

    //Modify request, defaulting blank partners
    List<Long> requestedPartners = request.getPartnerIds();
    if (requestedPartners == null || requestedPartners.isEmpty()) {
      Partner partner = userService.getLoggedInPartner();
      if (partner != null) {
        //Non source partners (eg destination partners) and default partners see candidates from all
        //partners - not just their own partner.

        final boolean isDefaultPartner =
            partner.isDefaultSourcePartner() || partner.isDefaultJobCreator();

        //A source partner defaults to just seeing their own candidates - unless they are the default partner
        if (partner.isSourcePartner() && !isDefaultPartner) {
          request.setPartnerIds(List.of(partner.getId()));
        } else {
          //Every one else defaults to seeing candidates from all partners
          List<PartnerImpl> sourcePartners = partnerService.listSourcePartners();
          List<Long> partnerIds =
              sourcePartners.stream().map(PartnerImpl::getId).collect(Collectors.toList());
          request.setPartnerIds(partnerIds);
        }
      }
    }
  }

  // done (handleElasticRequest)
  private BoolQueryBuilder processElasticRequest(SearchCandidateRequest searchRequest,
      String simpleQueryString, Set<Candidate> excludedCandidates) {
    // If saved search, add to searchIds to guard against circular dependencies
    List<Long> searchIds = new ArrayList<>();
    if (searchRequest.getSavedSearchId() != null) {
      searchIds.add(searchRequest.getSavedSearchId());
    }

    BoolQueryBuilder boolQueryBuilder = computeElasticQuery(searchRequest,
        simpleQueryString, excludedCandidates);

    // Add any joined searches to the builder
    if (!searchRequest.getSearchJoinRequests().isEmpty()) {
      for (SearchJoinRequest searchJoinRequest : searchRequest.getSearchJoinRequests()) {
        boolQueryBuilder = addElasticQuery(boolQueryBuilder, searchJoinRequest, searchIds);
      }
    }
    return boolQueryBuilder;
  }

  /*
  -----------------------------------------------------------------------------------------------
  NEW FUNCTIONS POST UPGRADE
  -----------------------------------------------------------------------------------------------
   */

  /**
   * Current replacement for the processelasticrequest function.
   */
  private BoolQuery.Builder handleElasticRequest(SearchCandidateRequest searchRequest,
      String simpleQueryString, Set<Candidate> excludedCandidates) {

    // Avoid circular dependencies by adding search id's in to check.
    List<Long> searchIds = new ArrayList<>();
    Long savedSearchId = searchRequest.getSavedSearchId();
    if (savedSearchId != null) {
      searchIds.add(savedSearchId);
    }

    // Build up the search query.
    BoolQuery.Builder boolBuilder = buildElasticQuery();

    // Add join searches in and return the boolQueryBuilder
    return addJoinRequests(searchRequest.getSearchJoinRequests(), boolBuilder, searchIds);
  }

  /* If there are requests to join process them and add to builder. */
  private BoolQuery.Builder addJoinRequests(List<SearchJoinRequest> joinsReqs,
      BoolQuery.Builder boolQryBuilder, List<Long> searchIds) {

    if (joinsReqs.isEmpty()) {
      return boolQryBuilder;
    } else {
      return joinsReqs.stream()
          .reduce(new BoolQuery.Builder(), (curBqb, joinReq) ->
              addQuery(curBqb, joinReq, searchIds), (acc1, acc2) -> acc2);
    }
  }

  /**
   * Current replacement for computeElasticQuery. Not every request will actually be an elastic
   * query, so this will handle both elastic and other search types. It will add a load of filters.
   * The filters are unlike the "must" score - they don't affect the elastic score.
   */
  private BoolQuery.Builder buildElasticQuery(
      SearchCandidateRequest req,
      @Nullable String simpleQueryString,
      @Nullable List<Candidate> excludedCandidates
  ) {

    BoolQuery.Builder boolBuilder = QueryBuilders.bool();
    if (simpleQueryStringExists(simpleQueryString)) {
      boolBuilder = addSimpleQryString(simpleQueryString);
    }

    return addFilters(boolBuilder, req, excludedCandidates);
  }

  // todo (need to check with the use of null returning in the filters/queries here)
  private BoolQuery.Builder addFilters(Builder boolBuilder, SearchCandidateRequest req,
      List<Candidate> excludedCandidates) {
    BoolQuery.Builder bBuilder = boolBuilder;
    User user = userService.getLoggedInUser();

    bBuilder.filter(addMinSpokenLevel(req));
    bBuilder.filter(addMinWrittenLevel(req));

    bBuilder.filter(excludeProvidedCandidates(excludedCandidates));
    bBuilder.filter(addOccupations(req));
    bBuilder.filter(addCountries(req, user));
    bBuilder.filter(addPartners(req));
    bBuilder.filter(addNationalities(req));
    bBuilder.filter(addStatuses(req));
    bBuilder.filter(addReferrer(req));
    bBuilder.filter(addGender(req));
    bBuilder.filter(addMinEducationLevel(req));
    bBuilder.filter(addEducation(req));
    return bBuilder;
  }

  // todo (need to have an overloaded one to set a term - see below)..
  // i think create the term query and then use must not in a bool...
  private Query excludeProvidedCandidates(List<Candidate> excludedCandidates) {
    if (excludedCandidates == null || excludedCandidates.isEmpty()) return null;

    List<Long> ids = getCandidateIds(excludedCandidates);
    return getStringTermsQuery(SearchType.not, "masterId", ids);
  }

  private List<Long> getCandidateIds(List<Candidate> candidates) {
    return candidates.stream().map(Candidate::getId).toList();
  }

  private Query addOccupations(SearchCandidateRequest req) {
    List<Long> ids = req.getOccupationIds();
    if (ids == null || ids.isEmpty()) return null;

    List<String> occupations = getOccupationNames(ids);
    return getStringTermsQuery("occupations.keyword", occupations);
  }

  private List<String> getOccupationNames(List<Long> ids) {
    return getValues(ids, id -> occupationService.getOccupation(id).getName());
  }

  private Query addCountries(SearchCandidateRequest req, User user) {
    List<Long> countryIds = req.getCountryIds();
    List<String> countries = new ArrayList<>();

    if (countryIds == null || countryIds.isEmpty()) {
      countries = user.getSourceCountries().stream().map(Country::getName).toList();
    } else {
      countries = getCountryNames(countryIds);
    }

    return countries == null ? null : getStringTermsQuery("country.keyword", countries);
  }

  private List<String> getCountryNames(List<Long> ids) {
    return getValues(ids, id -> countryService.getCountry(id).getName());
  }

  private Query addPartners(SearchCandidateRequest req) {
    List<Long> ids = req.getPartnerIds();
    if (ids == null || ids.isEmpty()) return null;

    List<String> partners = getPartnerAbbreviations(ids);
    return getStringTermsQuery("partner.keyword", partners);
  }

  private List<String> getPartnerAbbreviations(List<Long> ids) {
    return getValues(ids, id -> partnerService.getPartner(id).getAbbreviation());
  }

  private Query addNationalities(SearchCandidateRequest req) {
    List<Long> ids = req.getNationalityIds();
    if (ids == null || ids.isEmpty()) return null;

    List<String> nationalities = getNationalityNames(ids);
    return getStringTermsQuery("nationality.keyword", nationalities);
  }

  private @NotNull List<String> getNationalityNames(List<Long> ids) {
    return getValues(ids, id -> countryService.getCountry(id).getName());
  }

  /* Gets education majors. If not on the request it will go to db, so unfortunately
  it calls out to the member variable educationMajorService making it less than self-sufficient.
   */
  private Query addEducation(SearchCandidateRequest req) {
    List<Long> ids = req.getEducationMajorIds();
    if (ids == null || ids.isEmpty()) return null;

    // There are some ids so look them up to get the name.
    List<String> majors = getMajorNames(ids);
    return getStringTermsQuery("educationMajors.keyword", majors);
  }

  private @NotNull List<String> getMajorNames(List<Long> ids) {
    return getValues(ids, id -> educationMajorService.getEducationMajor(id).getName());
  }

  private boolean simpleQueryStringExists(String qryString) {
    return qryString != null && !qryString.isEmpty();
  }

  private Builder addSimpleQryString(String qryString) {
    return QueryBuilders.bool().must(getSimpleStringAsQuery(qryString));
  }

  /**
   * Current replacement for addElasticQuery
   */
  private BoolQuery.Builder addQuery(BoolQuery.Builder bqb, SearchJoinRequest sjr,
      List<Long> searchIds) {

    // We don't want searches built on themselves - this is also guarded against in frontend
    if (searchIds.contains(sjr.getSavedSearchId())) {
      throw new CircularReferencedException(sjr.getSavedSearchId());
    }
    List<Long> newSearchIds = new ArrayList<>(searchIds);
    newSearchIds.add(sjr.getSavedSearchId());

    SearchCandidateRequest scr = loadSavedSearch(sjr.getSavedSearchId());
    // Get the keyword search term, if any and get excluded candidates.
    String sqs = scr.getSimpleQueryString();
    Set<Candidate> exclCandidates = computeCandidatesExcludedFromSearchCandidateRequest(scr);

    // Each time through this is added to the "must" of the query.
    BoolQuery.Builder newBqb = bqb;
    bqb.must(buildElasticQuery(scr, sqs, exclCandidates.stream().toList())
        .build()
        ._toQuery());

    return addJoinRequests(scr.getSearchJoinRequests(), bqb, newSearchIds);
  }

  private Query getSimpleStringAsQuery(String simpleQueryString) {
    if (simpleQueryString == null || simpleQueryString.isEmpty()) {
      return null;
    }
    return QueryBuilders.simpleQueryString().query(simpleQueryString).build()._toQuery();
  }

  private Query getRangeFilter(String field, Object min, Object max) {
    return RangeQuery.of(r -> r.field(field).from(min).to(max))._toQuery();
  }

  private Query getTermFilter(SearchType searchType, String field, List<String> values) {
    if (values.isEmpty()) {
      return null;
    }

    // Required to convert the list of objects into FieldValues...
    TermsQueryField valuesToUse = new TermsQueryField.Builder()
        .value(values.stream().map(FieldValue::of).toList())
        .build();
    Query qry = TermsQuery.of(t -> t
        .field(field)
        .terms(valuesToUse))._toQuery();

//    var list = new ArrayList<Object>();
//    SearchRequest.of(s -> s
//        .index("test")
//        .query(q -> q
//            .terms(t -> t
//                .terms(tt -> tt.value(list.stream().map(FieldValue::of).toList())))));

    BoolQuery.Builder builder = QueryBuilders.bool();
    if (searchType == SearchType.not) {
      builder.mustNot(qry);
    } else {
      builder.filter(qry);
    }
    return builder.build()._toQuery();
  }

  private void logFoundSearches(String msg) {
    log.info("Found {} in search.", msg);
  }

  private Query addGender(SearchCandidateRequest req) {
    Gender g = req.getGender();
    return g == null ? null : getStringTermQuery("gender", g.name());
  }

  // Check for bug - says min level but passes max as field...
  private Query addMinEducationLevel(SearchCandidateRequest req) {
    Integer level = req.getMinEducationLevel();
    return level == null ? null : getRangeFilter("maxEducationLevel", level, null);
  }

  private Query addMinSpokenLevel(SearchCandidateRequest req) {
    Integer min = req.getEnglishMinSpokenLevel();
    return min == null ? null : getIntTermQuery("minEnglishSpokenLevel", min);
  }

  private Query addMinWrittenLevel(SearchCandidateRequest req) {
    Integer min = req.getEnglishMinWrittenLevel();
    return min == null ? null : getIntTermQuery("minEnglishWrittenLevel", min);
  }

  private Query addMinOtherSpokenLevel(SearchCandidateRequest req) {
    Integer min = req.getOtherMinSpokenLevel();
    return min == null ? null : getIntTermQuery("otherLanguages.minSpokenLevel", min);
  }

  private Query addMinOtherWrittenLevel(SearchCandidateRequest req) {
    Integer min = req.getOtherMinWrittenLevel();
    return min == null ? null : getIntTermQuery("otherLanguages.minWrittenLevel", min);
  }

  // Todo( complete this - requires consideration of logic. )
  private Query addOtherLanguageName(SearchCandidateRequest req) {
    return new Query.Builder().build();
  }

  private Query addStatuses(SearchCandidateRequest req) {
    List<CandidateStatus> statuses = req.getStatuses();
    if (statuses == null) return null;
    List<String> names = getStatusNames(statuses);
    return getStringTermsQuery("status.keyword", names);
  }

  private @NotNull List<String> getStatusNames(List<CandidateStatus> statuses) {
     return statuses.stream()
        .map(CandidateStatus::name)
         .toList();
  }

  private Query addReferrer(SearchCandidateRequest req) {
    String referrer = req.getRegoReferrerParam();
    return referrer == null ? null : getStringTermQuery("regoReferrerParam", referrer);
  }

  private Query getStringTermQuery(String field, String term) {
    return QueryBuilders.term().field(field).value(term).build()._toQuery();
  }

  private Query getIntTermQuery(String field, Integer term) {
    return QueryBuilders.term().field(field).value(term).build()._toQuery();
  }

  private Query getStringTermsQuery(String field, List<String> terms) {
    return TermsQuery.of(t -> t
        .field(field)
        .terms(tt -> tt
            .value(terms.stream().map(FieldValue::of).toList())))._toQuery();
  }

//  private Query getStringTermsQuery(SearchType searchType, String field, List<String> terms) {
//    TermsQuery.of(t -> t
//        .field(field)
//        .terms(tt -> tt
//            .value(terms.stream().map(FieldValue::of).toList())))._toQuery();
//  }

  private List<String> getValues(List<Long> ids, Function<Long, String> valueRetriever) {
    return ids.stream()
        .map(valueRetriever)
        .toList();
  }
}