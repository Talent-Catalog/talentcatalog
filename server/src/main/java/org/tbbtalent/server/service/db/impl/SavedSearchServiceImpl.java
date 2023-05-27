/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.opencsv.CSVWriter;
import io.jsonwebtoken.lang.Collections;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.util.StringUtils;
import org.tbbtalent.server.exception.CircularReferencedException;
import org.tbbtalent.server.exception.CountryRestrictionException;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.ExportFailedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.InvalidSessionException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateStatus;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.EducationLevel;
import org.tbbtalent.server.model.db.Gender;
import org.tbbtalent.server.model.db.Language;
import org.tbbtalent.server.model.db.LanguageLevel;
import org.tbbtalent.server.model.db.PartnerImpl;
import org.tbbtalent.server.model.db.SalesforceJobOpp;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.SavedSearchType;
import org.tbbtalent.server.model.db.SearchJoin;
import org.tbbtalent.server.model.db.SearchType;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.db.partner.Partner;
import org.tbbtalent.server.model.db.partner.SourcePartner;
import org.tbbtalent.server.model.es.CandidateEs;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CandidateReviewStatusRepository;
import org.tbbtalent.server.repository.db.CandidateSpecification;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.repository.db.EducationLevelRepository;
import org.tbbtalent.server.repository.db.EducationMajorRepository;
import org.tbbtalent.server.repository.db.LanguageLevelRepository;
import org.tbbtalent.server.repository.db.LanguageRepository;
import org.tbbtalent.server.repository.db.OccupationRepository;
import org.tbbtalent.server.repository.db.PartnerRepository;
import org.tbbtalent.server.repository.db.SavedListRepository;
import org.tbbtalent.server.repository.db.SavedSearchRepository;
import org.tbbtalent.server.repository.db.SavedSearchSpecification;
import org.tbbtalent.server.repository.db.SearchJoinRepository;
import org.tbbtalent.server.repository.db.SurveyTypeRepository;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.request.candidate.SavedSearchGetRequest;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.SearchJoinRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tbbtalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tbbtalent.server.request.candidate.source.UpdateCandidateSourceDescriptionRequest;
import org.tbbtalent.server.request.search.CreateFromDefaultSavedSearchRequest;
import org.tbbtalent.server.request.search.SearchSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSharingRequest;
import org.tbbtalent.server.request.search.UpdateWatchingRequest;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.CountryService;
import org.tbbtalent.server.service.db.PartnerService;
import org.tbbtalent.server.service.db.SalesforceJobOppService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.SavedSearchService;
import org.tbbtalent.server.service.db.UserService;
import org.tbbtalent.server.service.db.email.EmailHelper;

@Service
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
    private final SurveyTypeRepository surveyTypeRepository;
    private final EducationMajorRepository educationMajorRepository;
    private final EducationLevelRepository educationLevelRepository;

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

    @Autowired
    public SavedSearchServiceImpl(
        CandidateRepository candidateRepository,
        CandidateService candidateService,
        CandidateReviewStatusRepository candidateReviewStatusRepository,
        CandidateSavedListService candidateSavedListService,
        CountryService countryService,
        PartnerService partnerService,
        ElasticsearchOperations elasticsearchOperations,
        EmailHelper emailHelper,
        UserRepository userRepository,
        UserService userService,
        SalesforceJobOppService salesforceJobOppService, SavedListRepository savedListRepository,
        SavedListService savedListService,
        SavedSearchRepository savedSearchRepository,
        SearchJoinRepository searchJoinRepository,
        LanguageLevelRepository languageLevelRepository,
        LanguageRepository languageRepository,
        CountryRepository countryRepository,
        PartnerRepository partnerRepository,
        OccupationRepository occupationRepository,
        SurveyTypeRepository surveyTypeRepository,
        EducationMajorRepository educationMajorRepository,
        EducationLevelRepository educationLevelRepository) {
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.candidateReviewStatusRepository = candidateReviewStatusRepository;
        this.candidateSavedListService = candidateSavedListService;
        this.countryService = countryService;
        this.partnerService = partnerService;
        this.elasticsearchOperations = elasticsearchOperations;
        this.emailHelper = emailHelper;
        this.userRepository = userRepository;
        this.userService = userService;
        this.salesforceJobOppService = salesforceJobOppService;
        this.savedListRepository = savedListRepository;
        this.savedListService = savedListService;
        this.savedSearchRepository = savedSearchRepository;
        this.searchJoinRepository = searchJoinRepository;
        this.languageLevelRepository = languageLevelRepository;
        this.languageRepository = languageRepository;
        this.partnerRepository = partnerRepository;
        this.countryRepository = countryRepository;
        this.occupationRepository = occupationRepository;
        this.surveyTypeRepository = surveyTypeRepository;
        this.educationMajorRepository = educationMajorRepository;
        this.educationLevelRepository = educationLevelRepository;
    }

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
        log.info("Found " + savedSearches.size() + " savedSearches in search");

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
        log.info("Found " + savedSearches.getTotalElements() + " savedSearches in search");

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

        //Do the search
        final Page<Candidate> candidates = doSearchCandidates(searchRequest);

        //Add in any selections
        markUserSelectedCandidates(savedSearchId, candidates);

        return candidates;
    }

    @Override
    public @NotNull Set<Long> searchCandidates(long savedSearchId)
        throws NoSuchObjectException {
        SearchCandidateRequest searchRequest =
            loadSavedSearch(savedSearchId);

        //Compute the candidates which should be excluded from search
        Set<Candidate> excludedCandidates = computeCandidatesExcludedFromSearchCandidateRequest(searchRequest);

        //Modify request, doing standard defaults
        addDefaultsToSearchCandidateRequest(searchRequest);

        Set<Long> candidateIds = new HashSet<>();
        String simpleQueryString = searchRequest.getSimpleQueryString();
        if (simpleQueryString != null && simpleQueryString.length() > 0) {
            //This is an elastic search request.

            BoolQueryBuilder boolQueryBuilder = computeElasticQuery(searchRequest,
                simpleQueryString, excludedCandidates);

            NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .build();

            SearchHits<CandidateEs> hits = elasticsearchOperations.search(
                query, CandidateEs.class, IndexCoordinates.of("candidates"));

            //Get candidate ids from the returned results
            for (SearchHit<CandidateEs> hit : hits) {
                candidateIds.add(hit.getContent().getMasterId());
            }
        } else {
            //Compute the normal query
            final Specification<Candidate> query = computeQuery(searchRequest, excludedCandidates);

            List<Candidate> candidates = candidateRepository.findAll(query);

            for (Candidate candidate : candidates) {
                candidateIds.add(candidate.getId());
            }
        }

        log.info("Found " + candidateIds.size() + " candidates in search");

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

        if (!StringUtils.isEmpty(savedSearch.getCountryIds())){
            savedSearch.setCountryNames(countryRepository.getNamesForIds(getIdsFromString(savedSearch.getCountryIds())));
        }
        if (!StringUtils.isEmpty(savedSearch.getPartnerIds())){
            savedSearch.setPartnerNames(partnerRepository.getNamesForIds(getIdsFromString(savedSearch.getPartnerIds())));
        }
        if (!StringUtils.isEmpty(savedSearch.getNationalityIds())){
            savedSearch.setNationalityNames(countryRepository.getNamesForIds(getIdsFromString(savedSearch.getNationalityIds())));
        }
        if (!StringUtils.isEmpty(savedSearch.getOccupationIds())){
            savedSearch.setOccupationNames(occupationRepository.getNamesForIds(getIdsFromString(savedSearch.getOccupationIds())));
        }
        if (!StringUtils.isEmpty(savedSearch.getVerifiedOccupationIds())){
            savedSearch.setVettedOccupationNames(occupationRepository.getNamesForIds(getIdsFromString(savedSearch.getVerifiedOccupationIds())));
        }
        if (!StringUtils.isEmpty(savedSearch.getEducationMajorIds())){
            savedSearch.setEducationMajors(educationMajorRepository.getNamesForIds(getIdsFromString(savedSearch.getEducationMajorIds())));
        }
        if (!StringUtils.isEmpty(savedSearch.getSurveyTypeIds())){
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
        SavedSearch savedSearch = convertToSavedSearch(request);
        final User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser != null) {
            checkDuplicates(null, request.getName(), loggedInUser.getId());
            savedSearch.setAuditFields(loggedInUser);
        }

        savedSearch = savedSearchRepository.save(savedSearch);
        savedSearch = addSearchJoins(request, savedSearch);

        //Copy across the user's selections (including context notes)
        //of the default saved search.
        if (loggedInUser != null) {
            SavedSearch defaultSavedSearch = getDefaultSavedSearch();
            Long savedSearchId = defaultSavedSearch.getId();

            //Get the default selection list.
            SavedList defaultSelectionList =
                    getSelectionList(savedSearchId, loggedInUser.getId());

            //Get the selection list of the new saved search
            SavedList newSelectionList =
                    getSelectionList(savedSearch.getId(), loggedInUser.getId());

            //Copy default list to the selection list of the new saved search.
            candidateSavedListService.copyContents(
                    defaultSelectionList, newSelectionList, false);

            //Clear search attributes and selections of default saved search
            clearSavedSearch(defaultSavedSearch, loggedInUser);

        }
        return savedSearch;
    }

    @Override
    @Transactional
    public SavedSearch updateSavedSearch(long id, UpdateSavedSearchRequest request)
            throws EntityExistsException {
        final User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            throw new InvalidSessionException("Not logged in");
        }

        if(request.getSearchCandidateRequest() == null){
            SavedSearch savedSearch = savedSearchRepository.findById(id)
                    .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));
            // If a saved search isn't global and belongs to loggedInUser, allow changes
            if (!savedSearch.getFixed() || savedSearch.getCreatedBy().getId().equals(loggedInUser.getId())) {
                savedSearch.setName(request.getName());
                savedSearch.setFixed(request.getFixed());
                savedSearch.setReviewable(request.getReviewable());
                savedSearch.setSfJobOpp(
                    salesforceJobOppService.getOrCreateJobOppFromLink(request.getSfJoblink()));

                savedSearch.setType(request.getSavedSearchType(), request.getSavedSearchSubtype());
                return savedSearchRepository.save(savedSearch);
            } else {
                log.warn("Can't update saved search " + savedSearch.getId() + " - " + savedSearch.getName());
                return savedSearch;
            }
        }

        SavedSearch savedSearch = convertToSavedSearch(request);

        //delete and recreate all joined searches
        searchJoinRepository.deleteBySearchId(id);

        savedSearch.setId(id);
        savedSearch = addSearchJoins(request, savedSearch);

        savedSearch.setAuditFields(loggedInUser);
        checkDuplicates(id, request.getName(), loggedInUser.getId());
        return savedSearchRepository.save(savedSearch);
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
            UpdateSavedSearchRequest request = new UpdateSavedSearchRequest();
            request.setName(constructDefaultSearchName(loggedInUser));
            request.setSavedSearchType(SavedSearchType.other);
            request.setDefaultSearch(true);
            savedSearch = createSavedSearch(request);
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

    private BoolQueryBuilder computeElasticQuery(SearchCandidateRequest request,
        String simpleQueryString, @Nullable Collection <Candidate> excludedCandidates) {
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

        //Create a simple query string builder from the given string
        SimpleQueryStringBuilder simpleQueryStringBuilder =
            QueryBuilders.simpleQueryStringQuery(simpleQueryString);

        //The simple query will be part of a composite query containing
        //filters.
        BoolQueryBuilder boolQueryBuilder =
            QueryBuilders.boolQuery().must(simpleQueryStringBuilder);

        //Add filters - each filter must return true for a hit
        //(Note: Filters are different from "Must" entries only in that
        //they don't affect the Elasticsearch score)

        //Add a TermsQuery filter for each multiselect request - eg
        //countries and nationalities. A match against any one of the
        //multiselected values will result in the filter returning true.
        //There is also a TermQuery which takes only one value.

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

        //Exclude given candidates
        if (excludedCandidates != null && excludedCandidates.size() > 0) {
            List<Object> candidateIds = excludedCandidates.stream()
                .map(Candidate::getId).collect(Collectors.toList());
            boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
                SearchType.not,"masterId", candidateIds);
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

        //Referrer
        String referrer = request.getRegoReferrerParam();
        if (referrer != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                QueryBuilders.termQuery("regoReferrerParam", referrer));
        }

        //Gender
        Gender gender = request.getGender();
        if (gender != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                QueryBuilders.termQuery("gender", gender.name()));
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
    private SavedSearch convertToSavedSearch(UpdateSavedSearchRequest request) {


        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setName(request.getName());
        savedSearch.setFixed(request.getFixed());
        savedSearch.setDefaultSearch(request.getDefaultSearch());
        savedSearch.setReviewable(request.getReviewable());
        savedSearch.setSfJobOpp(
            salesforceJobOppService.getOrCreateJobOppFromLink(request.getSfJoblink()));

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

    //Midnight GMT
    @Scheduled(cron = "0 1 0 * * ?", zone = "GMT")
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
                currentSearch = savedSearch.getName();
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
            savedSearch.setVerifiedOccupationIds(
                    getListAsString(request.getVerifiedOccupationIds()));
            savedSearch.setVerifiedOccupationSearchType(
                    request.getVerifiedOccupationSearchType());
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
        }
    }

    private SearchCandidateRequest convertToSearchCandidateRequest(SavedSearch request) throws CountryRestrictionException{
        User user = userService.getLoggedInUser();
        SearchCandidateRequest searchCandidateRequest = new SearchCandidateRequest();
        searchCandidateRequest.setSavedSearchId(request.getId());
        searchCandidateRequest.setSimpleQueryString(request.getSimpleQueryString());
        searchCandidateRequest.setKeyword(request.getKeyword());
        searchCandidateRequest.setStatuses(getStatusListFromString(request.getStatuses()));
        searchCandidateRequest.setGender(request.getGender());
        searchCandidateRequest.setOccupationIds(getIdsFromString(request.getOccupationIds()));
        searchCandidateRequest.setMinYrs(request.getMinYrs());
        searchCandidateRequest.setMaxYrs(request.getMaxYrs());
        searchCandidateRequest.setRegoReferrerParam(request.getRegoReferrerParam());
        searchCandidateRequest.setVerifiedOccupationIds(getIdsFromString(request.getVerifiedOccupationIds()));
        searchCandidateRequest.setVerifiedOccupationSearchType(request.getVerifiedOccupationSearchType());
        searchCandidateRequest.setPartnerIds(getIdsFromString(request.getPartnerIds()));
        searchCandidateRequest.setNationalityIds(getIdsFromString(request.getNationalityIds()));
        searchCandidateRequest.setSurveyTypeIds(getIdsFromString(request.getSurveyTypeIds()));
        searchCandidateRequest.setNationalitySearchType(request.getNationalitySearchType());
        searchCandidateRequest.setCountrySearchType(request.getCountrySearchType());

        // Check if the saved search countries match the source countries of the user
        List<Long> requestCountries = getIdsFromString(request.getCountryIds());

        // if a user has source country restrictions AND IF the request has countries selected
        if(user != null
                && user.getSourceCountries().size() > 0
                && request.getCountryIds() != null) {
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

        searchCandidateRequest.setEnglishMinSpokenLevel(request.getEnglishMinSpokenLevel());
        searchCandidateRequest.setEnglishMinWrittenLevel(request.getEnglishMinWrittenLevel());
        searchCandidateRequest.setExclusionListId(
            request.getExclusionList() != null ? request.getExclusionList().getId() : null);
        searchCandidateRequest.setOtherLanguageId(
            request.getOtherLanguage() != null ? request.getOtherLanguage().getId() : null);
        searchCandidateRequest.setOtherMinSpokenLevel(request.getOtherMinSpokenLevel());
        searchCandidateRequest.setOtherMinWrittenLevel(request.getOtherMinWrittenLevel());
        searchCandidateRequest.setLastModifiedFrom(request.getLastModifiedFrom());
        searchCandidateRequest.setLastModifiedTo(request.getLastModifiedTo());
//        searchCandidateRequest.setRegisteredFrom(request.getCreatedFrom());
//        searchCandidateRequest.setRegisteredTo(request.getCreatedTo());
        searchCandidateRequest.setMinAge(request.getMinAge());
        searchCandidateRequest.setMaxAge(request.getMaxAge());
        searchCandidateRequest.setMinEducationLevel(request.getMinEducationLevel());
        searchCandidateRequest.setEducationMajorIds(getIdsFromString(request.getEducationMajorIds()));
        List<SearchJoinRequest> searchJoinRequests = new ArrayList<>();
        for (SearchJoin searchJoin : request.getSearchJoins()) {
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
                .map(s -> CandidateStatus.valueOf(s))
                .collect(Collectors.toList()) : null;
    }


    private Page<Candidate> doSearchCandidates(SearchCandidateRequest request) {

        Page<Candidate> candidates;

        //Compute the candidates which should be excluded from search
        Set<Candidate> excludedCandidates = computeCandidatesExcludedFromSearchCandidateRequest(request);

        //Modify request, doing standard defaults
        addDefaultsToSearchCandidateRequest(request);

        String simpleQueryString = request.getSimpleQueryString();
        if (simpleQueryString != null && simpleQueryString.length() > 0) {
            User user = userService.getLoggedInUser();

            //This is an elastic search request
            BoolQueryBuilder boolQueryBuilder = computeElasticQuery(request,
                simpleQueryString, excludedCandidates);

            //Define sort from request
            PageRequest req = CandidateEs.convertToElasticSortField(request);

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
            //Now construct a candidate list sorted according to the original
            //list of ids.
            List<Candidate> candidateList = new ArrayList<>();
            for (Long candidateId : candidateIds) {
                candidateList.add(mapById.get(candidateId));
            }
            candidates = new PageImpl<>(candidateList, request.getPageRequest(),
                hits.getTotalHits());
        } else {
            Specification<Candidate> query = computeQuery(request, excludedCandidates);
            candidates = candidateRepository.findAll(query, request.getPageRequestWithoutSort());
        }
        log.info("Found " + candidates.getTotalElements() + " candidates in search");
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
                //Some partners default to seeing candidates from all source partners.
                final boolean isDefaultSourcePartner = partner instanceof SourcePartner
                    && ((SourcePartner) partner).isDefaultSourcePartner();
                //Different default for simple (non operating partners)
                //and default source partner
                if ("Partner".equals(partner.getPartnerType())
                    || "RecruiterPartner".equals(partner.getPartnerType())
                    || isDefaultSourcePartner) {
                   List<PartnerImpl> sourcePartners = partnerService.listSourcePartners();
                   List<Long> partnerIds =
                       sourcePartners.stream().map(PartnerImpl::getId).collect(Collectors.toList());
                    request.setPartnerIds(partnerIds);
                } else {
                   request.setPartnerIds(List.of(partner.getId()));
                }
            }
        }
    }

}
