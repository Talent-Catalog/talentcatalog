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

import com.opencsv.CSVWriter;
import io.jsonwebtoken.lang.Collections;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.tctalent.server.exception.CircularReferencedException;
import org.tctalent.server.exception.CountryRestrictionException;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ExportFailedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
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
import org.tctalent.server.model.db.UnhcrStatus;
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
import org.tctalent.server.security.AuthService;
import org.tctalent.server.service.db.CandidateReviewStatusService;
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
import org.tctalent.server.service.db.email.EmailNotificationLink;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavedSearchServiceImpl implements SavedSearchService {
    @Value("${web.admin}")
    private String adminUrl;

    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final CandidateReviewStatusService candidateReviewStatusService;
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
    private final EntityManager entityManager;
    private final AuthService authService;

    /**
     * These are the default candidate statuses to included in searches when no statuses are
     * specified.
     * Basically all "inactive" statuses such as draft, deleted, employed and ineligible.
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
        if (request.getWatched() != null && request.getWatched() ) {
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
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("SearchSavedSearches")
            .message("Found " + savedSearches.size() + " savedSearches in search")
            .logInfo();

        for (SavedSearch savedSearch: savedSearches) {
            savedSearch.parseType();
        }

        return savedSearches;
    }

    @Override
    public Page<SavedSearch> searchPaged(SearchSavedSearchRequest request) {
        final User loggedInUser = userService.getLoggedInUser();

        Page<SavedSearch> savedSearches;
        //If requesting watches
        if (request.getWatched() != null && request.getWatched() ) {
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
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("SearchSavedSearches")
            .message("Found " + savedSearches.getTotalElements() + " savedSearches in search")
            .logInfo();

        for (SavedSearch savedSearch: savedSearches) {
            savedSearch.parseType();
        }

        return savedSearches;
    }

    @Override
    public Page<Candidate> searchCandidates(
        long savedSearchId, SavedSearchGetRequest request)
        throws NoSuchObjectException {

        SearchCandidateRequest searchRequest =
            loadSavedSearch(savedSearchId);

        //Merge the SavedSearchGetRequest - notably the page request - in to
        //the standard saved search request.
        searchRequest.merge(request);

        //If user filters on unverified statuses we bypass performing a full search
        //Simply return candidates that the user has already reviewed as verified and/or rejected
        if (request.getReviewStatusFilter() != null &&
            request.getReviewStatusFilter().contains(ReviewStatus.unverified)) {
            return reviewedCandidates(searchRequest);
        }

        //Do the search
        final Page<Candidate> candidates = doSearchCandidates(searchRequest);

        //Add in any selections
        markUserSelectedCandidates(savedSearchId, candidates);

        return candidates;
    }

    private Page<Candidate> reviewedCandidates(SearchCandidateRequest request) {
        Page<Candidate> candidates = candidateRepository.findReviewedCandidatesBySavedSearchId(
            request.getSavedSearchId(),
            request.getReviewStatusFilter(),
            request.getPageRequestWithoutSort());

        return candidates;
    }

    @Override
    public @NotNull Set<Long> searchCandidates(long savedSearchId)
        throws NoSuchObjectException, InvalidRequestException {
        SearchCandidateRequest searchRequest =
            loadSavedSearch(savedSearchId);

        Set<Long> candidateIds = new HashSet<>();

        searchRequest.setPageSize(10000);
        long count = 0;
        int pageNum = 0;

        Page<Candidate> pageOfCandidates;
        do {
            searchRequest.setPageNumber(pageNum++);
            pageOfCandidates = doSearchCandidates(searchRequest);

            int limit = 32000;
            if (pageOfCandidates.getTotalElements() > limit) {
                throw new InvalidRequestException(
                    "Sorry, but there is currently a limit on doing stats on searches returning more than "
                        + limit + " candidates. We are working to remove this limit.");
            }

            count += pageOfCandidates.getNumberOfElements();

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .searchId(savedSearchId)
                .action("SearchCandidates")
                .message("Processing page " + pageNum + ". "
                    + count + " candidates of "
                    + pageOfCandidates.getTotalElements())
                .logInfo();

            //Extract candidate ids
            List<Candidate> candidates = pageOfCandidates.getContent();
            for (Candidate candidate : candidates) {
                candidateIds.add(candidate.getId());
            }

            //Clear persistence context to free up memory
            entityManager.clear();
        } while (pageOfCandidates.hasNext());
        return candidateIds;
    }

    /**
     * Added @Transactional to this method as it is calling another method (updateSavedSearch) which requires
     * the @Transactional annotation.
     * Transaction needs to wrap the database modifying operation (searchJoinRepository.deleteBySearchId(id)) or
     * else an exception will be thrown. See: https://www.baeldung.com/jpa-transaction-required-exception
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
            SavedSearch defaultSavedSearch =
                getDefaultSavedSearch();
            Long savedSearchId = defaultSavedSearch.getId();
            UpdateSavedSearchRequest updateRequest = new UpdateSavedSearchRequest();
            updateRequest.setSearchCandidateRequest(request);
            //Set other fields - no changes there
            updateRequest.setName(defaultSavedSearch.getName());
            updateRequest.setDefaultSearch(defaultSavedSearch.getDefaultSearch());
            updateRequest.setFixed(defaultSavedSearch.getFixed());
            updateRequest.setReviewable(defaultSavedSearch.getReviewable());
            updateRequest.setSavedSearchType(defaultSavedSearch.getSavedSearchType());
            updateRequest.setSavedSearchSubtype(defaultSavedSearch.getSavedSearchSubtype());
            //todo Need special method which only updates search part. Then don't need the above "no changes there" stuff
            updateSavedSearch(savedSearchId, updateRequest);

            //Do the search
            candidates = doSearchCandidates(request);

            //Add in any selections
            markUserSelectedCandidates(savedSearchId, candidates);
        }

        return candidates;
    }

    /**
     * Mark the Candidate objects with any context associated with the
     * selection list of the saved search.
     * This means that context fields (ie ContextNote) associated with the
     * saved search will be returned through the DtoBuilder if present.
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

        Map<Integer, String> languageLevelMap = languageLevelRepository.findAllActive().stream().collect(
                Collectors.toMap(LanguageLevel::getLevel, LanguageLevel::getName, (l1, l2) ->  l1));
        Map<Integer, String> educationLevelMap = educationLevelRepository.findAllActive().stream().collect(
                Collectors.toMap(EducationLevel::getLevel, EducationLevel::getName, (l1, l2) ->  l1));

        if (!ObjectUtils.isEmpty(savedSearch.getCountryIds())){
            savedSearch.setCountryNames(countryRepository.getNamesForIds(getIdsFromString(savedSearch.getCountryIds())));
        }
        if (!ObjectUtils.isEmpty(savedSearch.getPartnerIds())){
            savedSearch.setPartnerNames(partnerRepository.getNamesForIds(getIdsFromString(savedSearch.getPartnerIds())));
        }
        if (!ObjectUtils.isEmpty(savedSearch.getNationalityIds())){
            savedSearch.setNationalityNames(countryRepository.getNamesForIds(getIdsFromString(savedSearch.getNationalityIds())));
        }
        if (!ObjectUtils.isEmpty(savedSearch.getOccupationIds())){
            savedSearch.setOccupationNames(occupationRepository.getNamesForIds(getIdsFromString(savedSearch.getOccupationIds())));
        }
        if (!ObjectUtils.isEmpty(savedSearch.getEducationMajorIds())){
            savedSearch.setEducationMajors(educationMajorRepository.getNamesForIds(getIdsFromString(savedSearch.getEducationMajorIds())));
        }
        if (!ObjectUtils.isEmpty(savedSearch.getSurveyTypeIds())){
            savedSearch.setSurveyTypeNames(surveyTypeRepository.getNamesForIds(getIdsFromString(savedSearch.getSurveyTypeIds())));
        }
        if (savedSearch.getEnglishMinWrittenLevel() != null){
            savedSearch.setEnglishWrittenLevel(languageLevelMap.get(savedSearch.getEnglishMinWrittenLevel()));
        }
        if (savedSearch.getEnglishMinSpokenLevel() != null){
            savedSearch.setEnglishSpokenLevel(languageLevelMap.get(savedSearch.getEnglishMinSpokenLevel()));
        }
        if (savedSearch.getOtherMinSpokenLevel() != null){
            savedSearch.setOtherSpokenLevel(languageLevelMap.get(savedSearch.getOtherMinSpokenLevel()));
        }
        if (savedSearch.getOtherMinWrittenLevel() != null){
            savedSearch.setOtherWrittenLevel(languageLevelMap.get(savedSearch.getOtherMinWrittenLevel()));
        }
        if (savedSearch.getMinEducationLevel() != null){
            savedSearch.setMinEducationLevelName(educationLevelMap.get(savedSearch.getMinEducationLevel()));
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
        createRequest.setJobId(request.getJobId());

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
        if(request.getSearchCandidateRequest() == null){
            // If a saved search isn't global and belongs to loggedInUser, allow changes
            if (!savedSearch.getFixed() || savedSearch.getCreatedBy().getId().equals(loggedInUser.getId())) {
                savedSearch.setName(request.getName());
                savedSearch.setFixed(request.getFixed());
                savedSearch.setReviewable(request.getReviewable());

                final Long jobId = request.getJobId();
                if (jobId != null) {
                    savedSearch.setSfJobOpp(salesforceJobOppService.getJobOpp(jobId));
                }

                savedSearch.setType(request.getSavedSearchType(), request.getSavedSearchSubtype());
                return savedSearchRepository.save(savedSearch);
            } else {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .searchId(savedSearch.getId())
                    .action("UpdateSavedSearch")
                    .message("Can't update saved search " + savedSearch.getId() + " - " + savedSearch.getName())
                    .logWarn();

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
    public boolean deleteSavedSearch(long id)  {
        SavedSearch savedSearch = savedSearchRepository.findByIdLoadAudit(id).orElse(null);
        final User loggedInUser = userService.getLoggedInUser();

        if (savedSearch != null && loggedInUser != null) {

            // Check if saved search was created by the user deleting.
            if(savedSearch.getCreatedBy().getId().equals(loggedInUser.getId())) {
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
                entityManager.clear();
                Page<Candidate> result = doSearchCandidates(request);
                setCandidateContext(request.getSavedSearchId(), result);
                for (Candidate candidate : result.getContent()) {
                    csvWriter.writeNext(candidateService.getExportCandidateStrings(candidate));
                }

                if ((long) result.getNumber() * request.getPageSize() < result.getTotalElements()) {
                    request.setPageNumber(request.getPageNumber()+1);
                } else {
                    hasMore = false;
                }
            }
        } catch (IOException e) {
            throw new ExportFailedException( e);
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
            String s = searches.stream().map(SavedSearch::getName).sorted().collect(Collectors.joining(","));
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

    public boolean includesElasticSearch(long savedSearchId) {

        SearchCandidateRequest searchRequest = loadSavedSearch(savedSearchId);
        if (!ObjectUtils.isEmpty(searchRequest.getSimpleQueryString())) {
            return true;
        }

        List<SearchJoinRequest> searchJoinRequests = searchRequest.getSearchJoinRequests();
        while (!ObjectUtils.isEmpty(searchJoinRequests)) {
            //Note that in practice there is now only ever one searchJoinRequest
            final Long id = searchJoinRequests.get(0).getSavedSearchId();
            searchRequest = loadSavedSearch(id);
            if (!ObjectUtils.isEmpty(searchRequest.getSimpleQueryString())) {
                return true;
            }
            searchJoinRequests = searchRequest.getSearchJoinRequests();
        }

        //Didn't find any Elastic search
        return false;
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
        throws  NoSuchObjectException {
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
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(request.getSearchJoinRequests())) {
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
        if (simpleQueryString != null && !simpleQueryString.isEmpty()) {
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

        // AGE
        // Note that the orders of filters is reversed, since we're using DOB — i.e., min age has
        // higher DOB-as-a-number than max age (e.g., 19690101 vs 19920101)
        Integer minAge = request.getMinAge();
        Integer maxAge = request.getMaxAge();
        if (minAge != null || maxAge != null) {
            boolQueryBuilder = addElasticRangeFilter(
                boolQueryBuilder,
                "dob",
                constructDobFilter(maxAge),
                constructDobFilter(minAge));
        }

        //English levels
        Integer minSpokenLevel = request.getEnglishMinSpokenLevel();
        if (minSpokenLevel != null) {
            boolQueryBuilder =
                addElasticRangeFilter(boolQueryBuilder,
                    "minEnglishSpokenLevel",
                    minSpokenLevel, null);
        }
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

                Integer minOtherSpokenLevel = request.getOtherMinSpokenLevel();
                if (minOtherSpokenLevel != null) {
                    nestedQueryBuilder =
                            addElasticRangeFilter(nestedQueryBuilder,
                                    "otherLanguages.minSpokenLevel",
                                    minOtherSpokenLevel, null);
                }

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
        if (excludedCandidates != null && excludedCandidates.size() > 0) {
            List<Object> candidateIds = excludedCandidates.stream()
                .map(Candidate::getId).collect(Collectors.toList());
            boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
                SearchType.not,"masterId", candidateIds);
        }

        //Occupations
        final List<Long> occupationIds = request.getOccupationIds();
        final Integer minYrs = request.getMinYrs();
        final Integer maxYrs = request.getMaxYrs();
        if (!Collections.isEmpty(occupationIds)) {
            //Look up names from ids.
            List<Object> reqOccupations = new ArrayList<>();
            for (Long id : occupationIds) {
                final Occupation occupation = occupationService.getOccupation(id);
                reqOccupations.add(occupation.getName());
            }
            if (reqOccupations.size() > 0) {
                BoolQueryBuilder nestedQueryBuilder = new BoolQueryBuilder();
                nestedQueryBuilder = addElasticTermFilter(
                    nestedQueryBuilder, SearchType.or, "occupations.name.keyword", reqOccupations);
                if (minYrs != null || maxYrs != null) {
                    nestedQueryBuilder = addElasticRangeFilter(
                        nestedQueryBuilder, "occupations.yearsExperience", minYrs, maxYrs);
                }
                boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.nestedQuery(
                        "occupations", nestedQueryBuilder, ScoreMode.Avg));
            }
        }

        //Countries - need to take account of source country restrictions
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
        } else if (user != null && !Collections.isEmpty(user.getSourceCountries())){
            for (Country country: user.getSourceCountries()) {
                reqCountries.add(country.getName());
            }
        }

        if (reqCountries.size() > 0) {
            boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
                request.getCountrySearchType(),
                "country.keyword", reqCountries);
        }

        //Partners
        final List<Long> partnerIds = request.getPartnerIds();
        if (partnerIds != null) {
            //Look up names from ids.
            List<Object> reqPartners = new ArrayList<>();
            for (Long id : partnerIds) {
                final Partner partner = partnerService.getPartner(id);
                reqPartners.add(partner.getAbbreviation());
            }
            boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
                null,"partner.keyword", reqPartners);
        }

        //Nationalities
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
        List<CandidateStatus> statuses = request.getStatuses();
        if (statuses != null) {
            //Extract names from enums
            List<Object> reqStatuses = new ArrayList<>();
            for (CandidateStatus status : statuses) {
                reqStatuses.add(status.name());
            }
            boolQueryBuilder =
                addElasticTermFilter(boolQueryBuilder,
                    null,"status.keyword", reqStatuses);
        }

        //UNHCR Statuses
        List<UnhcrStatus> unhcrStatuses = request.getUnhcrStatuses();
        if (unhcrStatuses != null) {
            //Extract names from enums
            List<Object> reqUnhcrStatuses = new ArrayList<>();
            for (UnhcrStatus unhcrStatus : unhcrStatuses) {
                reqUnhcrStatuses.add(unhcrStatus.name());
            }
            boolQueryBuilder =
                addElasticTermFilter(boolQueryBuilder,
                    null,"unhcrStatus.keyword", reqUnhcrStatuses);
        }

        //Referrer
        String referrer = request.getRegoReferrerParam();
        if (referrer != null && !referrer.isEmpty()) {
            boolQueryBuilder = boolQueryBuilder.filter(
                QueryBuilders.termQuery("regoReferrerParam.keyword", referrer));
        }

        //Gender
        Gender gender = request.getGender();
        if (gender != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                QueryBuilders.termQuery("gender", gender.name()));
        }

        //Education Level (minimum)
        Integer minEducationLevel = request.getMinEducationLevel();
        if (minEducationLevel != null) {
            boolQueryBuilder =
                    addElasticRangeFilter(boolQueryBuilder,
                            "maxEducationLevel",
                            minEducationLevel, null);
        }

        //Educations
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

        //Mini Intake
        final Boolean miniIntakeCompleted = request.getMiniIntakeCompleted();
        if (miniIntakeCompleted != null) {
            if (miniIntakeCompleted) {
                boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.existsQuery("miniIntakeCompletedDate")
                );
            } else {
                boolQueryBuilder = boolQueryBuilder.mustNot(
                    QueryBuilders.existsQuery("miniIntakeCompletedDate")
                );
            }
        }

        //Full Intake
        final Boolean fullIntakeCompleted = request.getFullIntakeCompleted();
        if (fullIntakeCompleted != null) {
            if (fullIntakeCompleted) {
                boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.existsQuery("fullIntakeCompletedDate")
                );
            } else {
                boolQueryBuilder = boolQueryBuilder.mustNot(
                    QueryBuilders.existsQuery("fullIntakeCompletedDate")
                );
            }

        }

        // Last Modified
        // updatedDate is converted for the ES field 'updated' to a long denoting no. of
        // milliseconds elapsed since 1970-01-01T00:00:00Z. This enables an ES range query by
        // converting the dates in the request in the same way, as below.
        if (request.getLastModifiedFrom() != null) {
            Long lastModifiedFrom = OffsetDateTime.of(
                request.getLastModifiedFrom(),
                LocalTime.MIN,
                ZoneOffset.UTC
            ).toInstant().toEpochMilli();

            Long lastModifiedTo = request.getLastModifiedTo() == null ?
                null : OffsetDateTime.of(
                    request.getLastModifiedTo(),
                    LocalTime.MAX,
                    ZoneOffset.UTC
                ).toInstant().toEpochMilli();

            boolQueryBuilder = addElasticRangeFilter(
                boolQueryBuilder,
                "updated",
                lastModifiedFrom,
                lastModifiedTo
            );
        }

        // Survey types
        final List<Long> surveyTypeIds = request.getSurveyTypeIds();
        if (surveyTypeIds != null) {
            List<Object> surveyTypeObjList = new ArrayList<>(surveyTypeIds);
            boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
                null,"surveyType", surveyTypeObjList);
        }

        return boolQueryBuilder;
    }

    /**
     * Takes a min or max age as specified in candidate search and returns a term for filtering on
     * the DOB field in elasticsearch.
     * @param age min or max age as an Integer
     * @return String term for adding to search query as min or max value for a range filter
     */
    private String constructDobFilter(Integer age) {
        return age == null ? null : LocalDate.now()
            .minusYears(age + 1)
            .toString()
            .replaceAll("-", "");
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
     * Checks whether the given user already has another saved search with this
     * name - throwing exception if it does
     * @param savedSearchId Existing saved search - or null if none
     * @param name Saved search name
     * @param userId User
     * @throws EntityExistsException if saved search with this name already
     * exists for this user
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

    private BoolQueryBuilder addElasticRangeFilter(
        BoolQueryBuilder builder, String field,
        @Nullable Object min, @Nullable Object max) {
        if (min != null || max != null) {
            RangeQueryBuilder rangeQueryBuilder =
                QueryBuilders.rangeQuery(field).from(min).to(max);
            builder = builder.filter(rangeQueryBuilder);
        }
        return builder;
    }

    private BoolQueryBuilder addElasticTermFilter(
        BoolQueryBuilder builder, @Nullable SearchType searchType, String field,
        List<Object> values) {
        final int nValues = values.size();
        if (nValues > 0) {
            QueryBuilder queryBuilder;
            if (nValues == 1) {
                queryBuilder = QueryBuilders.termQuery(field, values.get(0));
            } else {
                queryBuilder = QueryBuilders.termsQuery(field, values.toArray());
            }
            if (searchType == SearchType.not) {
                builder = builder.mustNot(queryBuilder);
            } else {
                builder = builder.filter(queryBuilder);
            }
        } return builder;
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

    private Specification<Candidate> addQuery(Specification<Candidate> query, SearchJoinRequest searchJoinRequest, List<Long> savedSearchIds) {
        if (savedSearchIds.contains(searchJoinRequest.getSavedSearchId())) {
            throw new CircularReferencedException(searchJoinRequest.getSavedSearchId());
        }
        User user = userService.getLoggedInUser();
        //add id to list as do not want circular references
        savedSearchIds.add(searchJoinRequest.getSavedSearchId());
        //load saved search
        SearchCandidateRequest request = loadSavedSearch(searchJoinRequest.getSavedSearchId());
        Specification<Candidate> joinQuery = CandidateSpecification.buildSearchQuery(request, user, null);
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
                for (SearchJoinRequest searchJoinRequest : request.getSearchCandidateRequest().getSearchJoinRequests()) {
                    SearchJoin searchJoin = new SearchJoin();
                    searchJoin.setSavedSearch(savedSearch);
                    searchJoin.setChildSavedSearch(savedSearchRepository.findById(searchJoinRequest.getSavedSearchId()).orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, searchJoinRequest.getSavedSearchId())));
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
        final Long jobId = request.getJobId();
        if (jobId != null) {
            final SalesforceJobOpp jobOpp =
                jobId < 0 ? null : salesforceJobOppService.getJobOpp(jobId);
            savedSearch.setSfJobOpp(jobOpp);
        }

        savedSearch.setType(request.getSavedSearchType(), request.getSavedSearchSubtype());

        final SearchCandidateRequest searchCandidateRequest = request.getSearchCandidateRequest();
        populateSearchAttributes(savedSearch, searchCandidateRequest);

        return savedSearch;
    }

    private void markUserSelectedCandidates(@Nullable Long savedSearchId, Page<Candidate> candidates) {
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
                if (selectedCandidates.size() > 0) {
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
     * Has to be annotated as Transactional in order to create the "persistence context" (what
     * the underlying Hibernate calls a Session).
     * This context is used to fetch lazily loaded attributes (by auto generating other
     * SQL calls on the database).
     * <p/>
     * When running searches from requests through the REST API, Spring automatically
     * creates this context - so you don't have to annotate all your REST API methods
     * as Transactional.
     * - JC
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

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("notifySearchWatchers")
                .message("Notify watchers: running " + searches.size() + " searches")
                .logInfo();

            int count = 0;

            OffsetDateTime yesterday = OffsetDateTime.now().minusDays(1);

            //Look through all watched searches looking for any that have candidates that were
            //created since yesterday.
            //Those are the searches that need to notify their watchers.
            for (SavedSearch savedSearch : searches) {

                count++;
                currentSearch = savedSearch.getName() + " (" + savedSearch.getId() + ")";

                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("notifySearchWatchers")
                    .message("Running search " + count + ": " + currentSearch)
                    .logInfo();

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

                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("notifySearchWatchers")
                    .message("Tell user " + userId + " about searches " + s)
                    .logInfo();

                User user = this.userRepository.findById(userId).orElse(null);
                if (user == null) {
                    final String mess = "Unknown user watcher id " + userId + " watching searches " + s;
                    LogBuilder.builder(log)
                        .user(authService.getLoggedInUser())
                        .action("notifySearchWatchers")
                        .message(mess)
                        .logWarn();

                    emailHelper.sendAlert(mess);
                } else {
                    //Compute email notification links from SavedSearches
                    List<EmailNotificationLink> links = new ArrayList<>();
                    for (SavedSearch savedSearch : savedSearches) {
                        links.add(new EmailNotificationLink(
                            savedSearch.getId(), computeSearchUrl(savedSearch), savedSearch.getName()));
                    }
                    emailHelper.sendWatcherEmail(user, links);
                }
            }
        } catch (Exception ex) {
            String mess = "Watcher notification failure (" + currentSearch + ")";
            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .action("notifySearchWatchers")
                .message(mess)
                .logError(ex);

            emailHelper.sendAlert(mess, ex);
        }
    }

    private URL computeSearchUrl(SavedSearch savedSearch) {
        URL url = null;
        if (savedSearch != null) {
            String urlStr = adminUrl + "/search/" + savedSearch.getId();
            try {
                url = new URI(urlStr).toURL();
            } catch (Exception e) {
                LogBuilder.builder(log)
                    .user(authService.getLoggedInUser())
                    .action("computeSearchUrl")
                    .message("Bad url created from search " +
                        savedSearch.getId() + ": '" + urlStr + "'")
                    .logError(e);
            }
        }
        return url;
    }

    private void populateSearchAttributes(
            SavedSearch savedSearch, SearchCandidateRequest request) {
        if (request != null) {
            savedSearch.setSimpleQueryString(request.getSimpleQueryString());
            savedSearch.setKeyword(request.getKeyword());
            savedSearch.setStatuses(getStatusListAsString(request.getStatuses()));
            savedSearch.setUnhcrStatuses(getUnhcrStatusListAsString(request.getUnhcrStatuses()));
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
            // Not currently in use as of Jun '24 - preserved for now in case of reinstatement.
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

    private SearchCandidateRequest convertToSearchCandidateRequest(SavedSearch search) throws CountryRestrictionException {
        User user = userService.getLoggedInUser();
        SearchCandidateRequest searchCandidateRequest = new SearchCandidateRequest();
        searchCandidateRequest.setSavedSearchId(search.getId());
        searchCandidateRequest.setSimpleQueryString(search.getSimpleQueryString());
        searchCandidateRequest.setKeyword(search.getKeyword());
        searchCandidateRequest.setStatuses(getStatusListFromString(search.getStatuses()));
        searchCandidateRequest.setUnhcrStatuses(getUnhcrStatusListFromString(search.getUnhcrStatuses()));
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
        if(user != null
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

        // Not currently in use as of Jun '24 - preserved for now in case of reinstatement.
        CandidateFilterByOpps candidateFilterByOpps = CandidateFilterByOpps.mapToEnum(
            search.getAnyOpps(), search.getClosedOpps(), search.getRelocatedOpps());
        searchCandidateRequest.setCandidateFilterByOpps(candidateFilterByOpps);

        List<SearchJoinRequest> searchJoinRequests = new ArrayList<>();
        for (SearchJoin searchJoin : search.getSearchJoins()) {
            searchJoinRequests.add(new SearchJoinRequest(searchJoin.getChildSavedSearch().getId(), searchJoin.getChildSavedSearch().getName(), searchJoin.getSearchType()));
        }
        searchCandidateRequest.setSearchJoinRequests(searchJoinRequests);

        return searchCandidateRequest;

    }



    String getListAsString(List<Long> ids){
        return !CollectionUtils.isEmpty(ids) ? ids.stream().map(String::valueOf)
                .collect(Collectors.joining(",")) : null;
    }

    List<Long> getIdsFromString(String listIds){
        return listIds != null ? Stream.of(listIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList()) : null;
    }

    String getStatusListAsString(List<CandidateStatus> statuses){
        return !CollectionUtils.isEmpty(statuses) ? statuses.stream().map(String::valueOf)
                .collect(Collectors.joining(",")) : null;
    }

    List<CandidateStatus> getStatusListFromString(String statusList){
        return statusList != null ? Stream.of(statusList.split(","))
                .map(CandidateStatus::valueOf)
                .collect(Collectors.toList()) : null;
    }

    String getUnhcrStatusListAsString(List<UnhcrStatus> unhcrStatuses){
        return !CollectionUtils.isEmpty(unhcrStatuses) ? unhcrStatuses.stream().map(
                Enum::name)
            .collect(Collectors.joining(",")) : null;
    }

    List<UnhcrStatus> getUnhcrStatusListFromString(String unhcrStatusList){
        return unhcrStatusList != null ? Stream.of(unhcrStatusList.split(","))
            .map(UnhcrStatus::valueOf)
            .collect(Collectors.toList()) : null;
    }

    private Page<Candidate> doSearchCandidates(SearchCandidateRequest searchRequest) {

        Page<Candidate> candidates;

        // Compute the candidates which should be excluded from search
        Set<Candidate> excludedCandidates =
            computeCandidatesExcludedFromSearchCandidateRequest(searchRequest);

        // Modify request, doing standard defaults
        addDefaultsToSearchCandidateRequest(searchRequest);

        //Processing can change if search is based on another search.
        final boolean hasBaseSearch = searchRequest.getSearchJoinRequests() != null &&
                !searchRequest.getSearchJoinRequests().isEmpty();

        String simpleQueryString = searchRequest.getSimpleQueryString();

        //TODO JC Reconsider this logic of forcing all searches based on other searches to be
        //elastic searches.
        if ((simpleQueryString != null && !simpleQueryString.isEmpty()) || hasBaseSearch) {
            // This is an elasticsearch request OR is built on one or more other searches.

            // Combine any joined searches (which will all be processed as elastic)
            BoolQueryBuilder boolQueryBuilder = processElasticRequest(searchRequest,
                simpleQueryString, excludedCandidates);

            //Define sort from request
            PageRequest req = CandidateEs.convertToElasticSortField(searchRequest);

            // Convert BoolQueryBuilder to a compact JSON string
            String boolQueryJson = convertToJson(boolQueryBuilder);

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .searchId(searchRequest.getSavedSearchId())
                .action("doSearchCandidates")
                .message("Elasticsearch query: " + boolQueryJson)
                .logInfo();

            LogBuilder.builder(log)
                .user(authService.getLoggedInUser())
                .searchId(searchRequest.getSavedSearchId())
                .action("doSearchCandidates")
                .message("Elasticsearch sort: " + req)
                .logInfo();

            NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(req)
                .build();

            SearchHits<CandidateEs> hits = elasticsearchOperations.search(
                query, CandidateEs.class, IndexCoordinates.of(CandidateEs.INDEX_NAME));

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
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .searchId(searchRequest.getSavedSearchId())
            .action("doSearchCandidates")
            .message("Found " + candidates.getTotalElements() + " candidates in search")
            .logInfo();

        return candidates;
    }

    @NonNull
    private Set<Candidate> computeCandidatesExcludedFromSearchCandidateRequest(SearchCandidateRequest request) {
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

    private String convertToJson(BoolQueryBuilder boolQueryBuilder) {
        try (XContentBuilder builder = XContentFactory.jsonBuilder()) {
            boolQueryBuilder.toXContent(builder, null);
            BytesReference bytes = BytesReference.bytes(builder);
            return XContentHelper.convertToJson(bytes, false, XContentType.JSON);
        } catch (IOException e) {
            logConversionFailure(e);
            return boolQueryBuilder.toString();
        }
    }

    private void logConversionFailure(Exception e) {
        LogBuilder.builder(log)
            .user(authService.getLoggedInUser())
            .action("convertToJson")
            .message("Failed to convert BoolQueryBuilder to compact JSON: " + e.getMessage())
            .logWarn();
    }
}
