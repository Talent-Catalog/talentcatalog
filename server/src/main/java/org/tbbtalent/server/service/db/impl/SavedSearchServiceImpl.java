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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.tbbtalent.server.exception.*;
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.repository.db.*;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.SearchJoinRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tbbtalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tbbtalent.server.request.search.*;
import org.tbbtalent.server.security.AuthService;
import org.tbbtalent.server.service.db.CandidateSavedListService;
import org.tbbtalent.server.service.db.SavedListService;
import org.tbbtalent.server.service.db.SavedSearchService;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SavedSearchServiceImpl implements SavedSearchService {

    private static final Logger log = LoggerFactory.getLogger(SavedSearchServiceImpl.class);

    private final CandidateSavedListService candidateSavedListService;
    private final UserRepository userRepository;
    private final SavedListRepository savedListRepository;
    private final SavedListService savedListService;
    private final SavedSearchRepository savedSearchRepository;
    private final SearchJoinRepository searchJoinRepository;
    private final LanguageLevelRepository languageLevelRepository;
    private final LanguageRepository languageRepository;
    private final CountryRepository countryRepository;
    private final OccupationRepository occupationRepository;
    private final EducationMajorRepository educationMajorRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final AuthService authService;

    @Autowired
    public SavedSearchServiceImpl(
            CandidateSavedListService candidateSavedListService,
            UserRepository userRepository,
            SavedListRepository savedListRepository,
            SavedListService savedListService,
            SavedSearchRepository savedSearchRepository,
            SearchJoinRepository searchJoinRepository,
            LanguageLevelRepository languageLevelRepository,
            LanguageRepository languageRepository,
            CountryRepository countryRepository,
            OccupationRepository occupationRepository,
            EducationMajorRepository educationMajorRepository,
            EducationLevelRepository educationLevelRepository,
            AuthService authService) {
        this.candidateSavedListService = candidateSavedListService;
        this.userRepository = userRepository;
        this.savedListRepository = savedListRepository;
        this.savedListService = savedListService;
        this.savedSearchRepository = savedSearchRepository;
        this.searchJoinRepository = searchJoinRepository;
        this.languageLevelRepository = languageLevelRepository;
        this.languageRepository = languageRepository;
        this.countryRepository = countryRepository;
        this.occupationRepository = occupationRepository;
        this.educationMajorRepository = educationMajorRepository;
        this.educationLevelRepository = educationLevelRepository;
        this.authService = authService;
    }

    @Override
    public List<SavedSearch> search(SearchSavedSearchRequest request) {
        final User loggedInUser = authService.getLoggedInUser().orElse(null);

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
            User userWithSharedSearches = loggedInUser == null ? null :
                userRepository.findByIdLoadSharedSearches(
                    loggedInUser.getId());
            savedSearches = savedSearchRepository.findAll(
                SavedSearchSpecification.buildSearchQuery(request, userWithSharedSearches));
        }
        log.info("Found " + savedSearches.size() + " savedSearches in search");

        for (SavedSearch savedSearch: savedSearches) {
            savedSearch.parseType();
        }

        return savedSearches;
    }

    @Override
    public Page<SavedSearch> searchPaged(SearchSavedSearchRequest request) {
        final User loggedInUser = authService.getLoggedInUser().orElse(null);

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
            User userWithSharedSearches = loggedInUser == null ? null :
                    userRepository.findByIdLoadSharedSearches(
                            loggedInUser.getId());
            savedSearches = savedSearchRepository.findAll(
                    SavedSearchSpecification.buildSearchQuery(
                            request, userWithSharedSearches), request.getPageRequest());
        }
        log.info("Found " + savedSearches.getTotalElements() + " savedSearches in search");

        for (SavedSearch savedSearch: savedSearches) {
            savedSearch.parseType();
        }
        
        return savedSearches;
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
        savedListService.clearSavedList(selectionList.getId());
    }

    @Override
    public SavedSearch createFromDefaultSavedSearch(
            CreateFromDefaultSavedSearchRequest request)  
            throws NoSuchObjectException {
        
        final User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

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
        final User loggedInUser = authService.getLoggedInUser().orElse(null);
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
            savedListService.copyContents(
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
        final User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        if(request.getSearchCandidateRequest() == null){
            SavedSearch savedSearch = savedSearchRepository.findById(id)
                    .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));
            // If a saved search isn't global and belongs to loggedInUser, allow changes
            if (!savedSearch.getFixed() || savedSearch.getCreatedBy().getId().equals(loggedInUser.getId())) {
                savedSearch.setName(request.getName());
                savedSearch.setFixed(request.getFixed());
                savedSearch.setReviewable(request.getReviewable());
                savedSearch.setSfJoblink(request.getSfJoblink());
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
        final User loggedInUser = authService.getLoggedInUser().orElse(null);

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
        User loggedInUser = authService.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

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
            savedList.setSfJoblink(savedSearch.getSfJoblink());
            savedList = savedListRepository.save(savedList);
        } else {
            //Keep SavedSearch sfJobLink in sync with its selection list.
            if (savedSearch.getSfJoblink() == null) {
                //If not both null
                if (savedList.getSfJoblink() != null) {
                    savedList.setSfJoblink(null);
                    savedList = savedListRepository.save(savedList);
                }
            } else {
                //If different
                if (!savedSearch.getSfJoblink().equals(savedList.getSfJoblink())) {
                    savedList.setSfJoblink(savedSearch.getSfJoblink());
                    savedList = savedListRepository.save(savedList);
                }
            }
        }
        
        return savedList;
    }

    @Override
    public @NotNull SavedList getSelectionListForLoggedInUser(long id) 
        throws InvalidSessionException {

        final User loggedInUser = authService.getLoggedInUser()
            .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        return getSelectionList(id, loggedInUser.getId());
    }

    @Override
    public void updateCandidateContextNote(long id, UpdateCandidateContextNoteRequest request) {
        final User loggedInUser = authService.getLoggedInUser().orElse(null);
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
        savedSearch.setSfJoblink(request.getSfJoblink());
        savedSearch.setType(request.getSavedSearchType(), request.getSavedSearchSubtype());

        final SearchCandidateRequest searchCandidateRequest = request.getSearchCandidateRequest();
        populateSearchAttributes(savedSearch, searchCandidateRequest);

        return savedSearch;
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
            savedSearch.setVerifiedOccupationIds(
                    getListAsString(request.getVerifiedOccupationIds()));
            savedSearch.setVerifiedOccupationSearchType(
                    request.getVerifiedOccupationSearchType());
            savedSearch.setNationalityIds(
                    getListAsString(request.getNationalityIds()));
            savedSearch.setNationalitySearchType(request.getNationalitySearchType());
            savedSearch.setCountryIds(getListAsString(request.getCountryIds()));
            savedSearch.setEnglishMinSpokenLevel(request.getEnglishMinSpokenLevel());
            savedSearch.setEnglishMinWrittenLevel(request.getEnglishMinWrittenLevel());
            Optional<Language> language = 
                    request.getOtherLanguageId() != null ? 
                            languageRepository.findById(
                                    request.getOtherLanguageId()) : Optional.empty();
            if (language.isPresent()) {
                savedSearch.setOtherLanguage(language.get());
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
        User user = authService.getLoggedInUser().orElse(null);
        SearchCandidateRequest searchCandidateRequest = new SearchCandidateRequest();
        searchCandidateRequest.setSavedSearchId(request.getId());
        searchCandidateRequest.setSimpleQueryString(request.getSimpleQueryString());
        searchCandidateRequest.setKeyword(request.getKeyword());
        searchCandidateRequest.setStatuses(getStatusListFromString(request.getStatuses()));
        searchCandidateRequest.setGender(request.getGender());
        searchCandidateRequest.setOccupationIds(getIdsFromString(request.getOccupationIds()));
        searchCandidateRequest.setMinYrs(request.getMinYrs());
        searchCandidateRequest.setMaxYrs(request.getMaxYrs());
        searchCandidateRequest.setVerifiedOccupationIds(getIdsFromString(request.getVerifiedOccupationIds()));
        searchCandidateRequest.setVerifiedOccupationSearchType(request.getVerifiedOccupationSearchType());
        searchCandidateRequest.setNationalityIds(getIdsFromString(request.getNationalityIds()));
        searchCandidateRequest.setNationalitySearchType(request.getNationalitySearchType());

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
        searchCandidateRequest.setOtherLanguageId(request.getOtherLanguage() != null ? request.getOtherLanguage().getId() : null);
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
}
