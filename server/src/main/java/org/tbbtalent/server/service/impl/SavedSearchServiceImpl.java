package org.tbbtalent.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.tbbtalent.server.exception.CountryRestrictionException;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.CandidateStatus;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.model.EducationLevel;
import org.tbbtalent.server.model.Language;
import org.tbbtalent.server.model.LanguageLevel;
import org.tbbtalent.server.model.SavedSearch;
import org.tbbtalent.server.model.SearchJoin;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.repository.CountryRepository;
import org.tbbtalent.server.repository.EducationLevelRepository;
import org.tbbtalent.server.repository.EducationMajorRepository;
import org.tbbtalent.server.repository.LanguageLevelRepository;
import org.tbbtalent.server.repository.LanguageRepository;
import org.tbbtalent.server.repository.NationalityRepository;
import org.tbbtalent.server.repository.OccupationRepository;
import org.tbbtalent.server.repository.SavedSearchRepository;
import org.tbbtalent.server.repository.SavedSearchSpecification;
import org.tbbtalent.server.repository.SearchJoinRepository;
import org.tbbtalent.server.repository.UserRepository;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.SearchJoinRequest;
import org.tbbtalent.server.request.search.SearchSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSharingRequest;
import org.tbbtalent.server.request.search.UpdateWatchingRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.SavedSearchService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SavedSearchServiceImpl implements SavedSearchService {

    private static final Logger log = LoggerFactory.getLogger(SavedSearchServiceImpl.class);

    private final UserRepository userRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final SearchJoinRepository searchJoinRepository;
    private final LanguageLevelRepository languageLevelRepository;
    private final LanguageRepository languageRepository;
    private final CountryRepository countryRepository;
    private final NationalityRepository nationalityRepository;
    private final OccupationRepository occupationRepository;
    private final EducationMajorRepository educationMajorRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final UserContext userContext;

    @Autowired
    public SavedSearchServiceImpl(
            UserRepository userRepository,
            SavedSearchRepository savedSearchRepository,
            SearchJoinRepository searchJoinRepository,
            LanguageLevelRepository languageLevelRepository,
            LanguageRepository languageRepository,
            CountryRepository countryRepository,
            NationalityRepository nationalityRepository,
            OccupationRepository occupationRepository,
            EducationMajorRepository educationMajorRepository,
            EducationLevelRepository educationLevelRepository,
            UserContext userContext) {
        this.userRepository = userRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.searchJoinRepository = searchJoinRepository;
        this.languageLevelRepository = languageLevelRepository;
        this.languageRepository = languageRepository;
        this.countryRepository = countryRepository;
        this.nationalityRepository = nationalityRepository;
        this.occupationRepository = occupationRepository;
        this.educationMajorRepository = educationMajorRepository;
        this.educationLevelRepository = educationLevelRepository;
        this.userContext = userContext;
    }

    @Override
    public Page<SavedSearch> searchSavedSearches(SearchSavedSearchRequest request) {
        User userWithSharedSearches =
                userRepository.findByIdLoadSharedSearches(
                        userContext.getLoggedInUser().getId());

        Page<SavedSearch> savedSearches = savedSearchRepository.findAll(
                SavedSearchSpecification.buildSearchQuery(
                        request, userWithSharedSearches), request.getPageRequest());
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
        SavedSearch savedSearch = this.savedSearchRepository.findByIdLoadUsers(id)
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
            savedSearch.setNationalityNames(nationalityRepository.getNamesForIds(getIdsFromString(savedSearch.getNationalityIds())));
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
    @Transactional
    public SavedSearch createSavedSearch(UpdateSavedSearchRequest request) throws EntityExistsException {
        SavedSearch savedSearch = convertToSavedSearch(request);
        checkDuplicates(null, request.getName());
        savedSearch.setAuditFields(userContext.getLoggedInUser());
        savedSearch = this.savedSearchRepository.save(savedSearch);
        savedSearch = addSearchJoins(request, savedSearch);
        return savedSearch;
    }

    @Override
    @Transactional
    public SavedSearch updateSavedSearch(long id, UpdateSavedSearchRequest request) throws EntityExistsException {
        // if no search candidate request, only update the search name and type
        if(request.getSearchCandidateRequest() == null){
            SavedSearch savedSearch = savedSearchRepository.findById(id).orElse(null);
            savedSearch.setName(request.getName());
            savedSearch.setFixed(request.getFixed());
            savedSearch.setReviewable(request.getReviewable());
            savedSearch.setType(request.getSavedSearchType(), request.getSavedSearchSubtype());
            return savedSearchRepository.save(savedSearch);
        }

        SavedSearch savedSearch = convertToSavedSearch(request);
        
        //delete and recreate all joined searches
        searchJoinRepository.deleteBySearchId(id);

        savedSearch.setId(id);
        savedSearch = addSearchJoins(request, savedSearch);
        savedSearch.setAuditFields(userContext.getLoggedInUser());
        checkDuplicates(id, request.getName());
        return savedSearchRepository.save(savedSearch);
    }

    @Override
    @Transactional
    public boolean deleteSavedSearch(long id)  {
        SavedSearch savedSearch = savedSearchRepository.findByIdLoadAudit(id).orElse(null);

        if (savedSearch != null) {

            // Check if saved search was created by the user deleting.
            if(savedSearch.getCreatedBy().getId() == userContext.getLoggedInUser().getId()) {
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

        List<SavedSearch> searches = savedSearchRepository.findUserWatchedSearches(request.getUserId());
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

    private void checkDuplicates(Long id, String name) {
        SavedSearch existing = savedSearchRepository.findByNameIgnoreCase(name);
        if (existing != null && existing.getStatus() != Status.deleted) {
            if (!existing.getId().equals(id)) {
                throw new EntityExistsException("savedSearch");
            }
        }
    }

    private SavedSearch addSearchJoins(UpdateSavedSearchRequest request, SavedSearch savedSearch) {
        Set<SearchJoin> searchJoins = new HashSet<>();
        if (!CollectionUtils.isEmpty(request.getSearchCandidateRequest().getSearchJoinRequests())){
            for (SearchJoinRequest searchJoinRequest : request.getSearchCandidateRequest().getSearchJoinRequests()) {
                SearchJoin searchJoin = new SearchJoin();
                searchJoin.setSavedSearch(savedSearch);
                searchJoin.setChildSavedSearch(savedSearchRepository.findById(searchJoinRequest.getSavedSearchId()).orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, searchJoinRequest.getSavedSearchId())));
                searchJoin.setSearchType(searchJoinRequest.getSearchType());
                this.searchJoinRepository.save(searchJoin);
            }
            savedSearch.setSearchJoins(searchJoins);
        }

        return savedSearch;
    }


    //---------------------------------------------------------------------------------------------------
    private SavedSearch convertToSavedSearch(UpdateSavedSearchRequest request) {


        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setName(request.getName());
        savedSearch.setFixed(request.getFixed());
        savedSearch.setReviewable(request.getReviewable());
        savedSearch.setType(request.getSavedSearchType(), request.getSavedSearchSubtype());

        final SearchCandidateRequest searchCandidateRequest = request.getSearchCandidateRequest();
        if (searchCandidateRequest != null) {
            savedSearch.setKeyword(searchCandidateRequest.getKeyword());
            savedSearch.setStatuses(getStatusListAsString(searchCandidateRequest.getStatuses()));
            savedSearch.setGender(searchCandidateRequest.getGender());
            savedSearch.setOccupationIds(getListAsString(searchCandidateRequest.getOccupationIds()));
            savedSearch.setOrProfileKeyword(searchCandidateRequest.getOrProfileKeyword());
            savedSearch.setVerifiedOccupationIds(getListAsString(searchCandidateRequest.getVerifiedOccupationIds()));
            savedSearch.setVerifiedOccupationSearchType(searchCandidateRequest.getVerifiedOccupationSearchType());
            savedSearch.setNationalityIds(getListAsString(searchCandidateRequest.getNationalityIds()));
            savedSearch.setNationalitySearchType(searchCandidateRequest.getNationalitySearchType());
            savedSearch.setCountryIds(getListAsString(searchCandidateRequest.getCountryIds()));
            savedSearch.setEnglishMinSpokenLevel(searchCandidateRequest.getEnglishMinSpokenLevel());
            savedSearch.setEnglishMinWrittenLevel(searchCandidateRequest.getEnglishMinWrittenLevel());
            Optional<Language> language = searchCandidateRequest.getOtherLanguageId() != null ? languageRepository.findById(searchCandidateRequest.getOtherLanguageId()) : null;
            if (language != null && language.isPresent()) {
                savedSearch.setOtherLanguage(language.get());
            }
            savedSearch.setOtherMinSpokenLevel(searchCandidateRequest.getOtherMinSpokenLevel());
            savedSearch.setOtherMinWrittenLevel(searchCandidateRequest.getOtherMinWrittenLevel());
            savedSearch.setUnRegistered(searchCandidateRequest.getUnRegistered());
            savedSearch.setLastModifiedFrom(searchCandidateRequest.getLastModifiedFrom());
            savedSearch.setLastModifiedTo(searchCandidateRequest.getLastModifiedTo());
//        savedSearch.setCreatedFrom(request.getSearchCandidateRequest().getRegisteredFrom());
//        savedSearch.setCreatedTo(request.getSearchCandidateRequest().getRegisteredTo());
            savedSearch.setMinAge(searchCandidateRequest.getMinAge());
            savedSearch.setMaxAge(searchCandidateRequest.getMaxAge());
            savedSearch.setMinEducationLevel(searchCandidateRequest.getMinEducationLevel());
            savedSearch.setEducationMajorIds(getListAsString(searchCandidateRequest.getEducationMajorIds()));
            savedSearch.setIncludeDraftAndDeleted(searchCandidateRequest.getIncludeDraftAndDeleted());
        }

        return savedSearch;

    }

    private SearchCandidateRequest convertToSearchCandidateRequest(SavedSearch request) {
        User user = userContext.getLoggedInUser();
        SearchCandidateRequest searchCandidateRequest = new SearchCandidateRequest();
        searchCandidateRequest.setSavedSearchId(request.getId());
        searchCandidateRequest.setKeyword(request.getKeyword());
        searchCandidateRequest.setStatuses(getStatusListFromString(request.getStatuses()));
        searchCandidateRequest.setGender(request.getGender());
        searchCandidateRequest.setOccupationIds(getIdsFromString(request.getOccupationIds()));
        searchCandidateRequest.setOrProfileKeyword(request.getOrProfileKeyword());
        searchCandidateRequest.setVerifiedOccupationIds(getIdsFromString(request.getVerifiedOccupationIds()));
        searchCandidateRequest.setVerifiedOccupationSearchType(request.getVerifiedOccupationSearchType());
        searchCandidateRequest.setNationalityIds(getIdsFromString(request.getNationalityIds()));
        searchCandidateRequest.setNationalitySearchType(request.getNationalitySearchType());

        // Check if the saved search countries match the source countries of the user
        List<Long> requestCountries = getIdsFromString(request.getCountryIds());

        // if a user has source country restrictions AND IF the request has countries selected
        if(user.getSourceCountries().size() > 0 && request.getCountryIds() != null) {
            List<Long> sourceCountries = user.getSourceCountries().stream()
                    .map(Country::getId)
                    .collect(Collectors.toList());
            //find the users source countries in the saved search countries
            requestCountries.retainAll(sourceCountries);
            if(requestCountries.size() == 0){
                //if no source countries in the saved search countries throw an error
                throw new CountryRestrictionException("You don't have access to any of the countries in the Saved Search: " + request.getName());
            }
        }
        searchCandidateRequest.setCountryIds(requestCountries);

        searchCandidateRequest.setEnglishMinSpokenLevel(request.getEnglishMinSpokenLevel());
        searchCandidateRequest.setEnglishMinWrittenLevel(request.getEnglishMinWrittenLevel());
        searchCandidateRequest.setOtherLanguageId(request.getOtherLanguage() != null ? request.getOtherLanguage().getId() : null);
        searchCandidateRequest.setOtherMinSpokenLevel(request.getOtherMinSpokenLevel());
        searchCandidateRequest.setOtherMinWrittenLevel(request.getOtherMinWrittenLevel());
        searchCandidateRequest.setUnRegistered(request.getUnRegistered());
        searchCandidateRequest.setLastModifiedFrom(request.getLastModifiedFrom());
        searchCandidateRequest.setLastModifiedTo(request.getLastModifiedTo());
//        searchCandidateRequest.setRegisteredFrom(request.getCreatedFrom());
//        searchCandidateRequest.setRegisteredTo(request.getCreatedTo());
        searchCandidateRequest.setMinAge(request.getMinAge());
        searchCandidateRequest.setMaxAge(request.getMaxAge());
        searchCandidateRequest.setMinEducationLevel(request.getMinEducationLevel());
        searchCandidateRequest.setEducationMajorIds(getIdsFromString(request.getEducationMajorIds()));
        searchCandidateRequest.setIncludeDraftAndDeleted(request.getIncludeDraftAndDeleted());
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
