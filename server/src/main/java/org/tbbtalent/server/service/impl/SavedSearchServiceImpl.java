package org.tbbtalent.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.repository.*;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.SearchJoinRequest;
import org.tbbtalent.server.request.search.CreateSavedSearchRequest;
import org.tbbtalent.server.request.search.SearchSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.SavedSearchService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SavedSearchServiceImpl implements SavedSearchService {

    private static final Logger log = LoggerFactory.getLogger(SavedSearchServiceImpl.class);

    private final CandidateRepository candidateRepository;
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
    public SavedSearchServiceImpl(CandidateRepository candidateRepository, SavedSearchRepository savedSearchRepository,
                                  SearchJoinRepository searchJoinRepository, LanguageLevelRepository languageLevelRepository,
                                  LanguageRepository languageRepository, CountryRepository countryRepository, NationalityRepository nationalityRepository, OccupationRepository occupationRepository, EducationMajorRepository educationMajorRepository, EducationLevelRepository educationLevelRepository, UserContext userContext) {
        this.candidateRepository = candidateRepository;
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
        Page<SavedSearch> savedSearches = savedSearchRepository.findAll(
                SavedSearchSpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + savedSearches.getTotalElements() + " savedSearches in search");
        return savedSearches;
    }

    @Override
    public SearchCandidateRequest loadSavedSearch(long id) {
        SavedSearch savedSearch = this.savedSearchRepository.findByIdLoadSearchJoins(id)
                .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));
        return convertToSearchCandidateRequest(savedSearch);
    }

    @Override
    public SavedSearch getSavedSearch(long id) {
        SavedSearch savedSearch = this.savedSearchRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));

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
    public SavedSearch createSavedSearch(CreateSavedSearchRequest request) throws EntityExistsException {
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
        // if no search candidate request, only update the search name
        if(request.getSearchCandidateRequest() == null){
            SavedSearch savedSearch = savedSearchRepository.findById(id).orElse(null);
            savedSearch.setName(request.getName());
            return savedSearchRepository.save(savedSearch);
        }

        SavedSearch savedSearch = convertToSavedSearch(request);
        savedSearch.setId(id);
        //delete and recreate all joined searches
        searchJoinRepository.deleteBySearchId(id);

        savedSearch = addSearchJoins(request, savedSearch);
        savedSearch.setId(id);
        savedSearch.setAuditFields(userContext.getLoggedInUser());
        checkDuplicates(id, request.getName());
        return savedSearchRepository.save(savedSearch);
    }

    @Override
    @Transactional
    public boolean deleteSavedSearch(long id)  {
        SavedSearch savedSearch = savedSearchRepository.findById(id).orElse(null);

        if (savedSearch != null) {
            savedSearch.setStatus(Status.deleted);
            savedSearchRepository.save(savedSearch);
            return true;
        }
        return false;
    }

    private void checkDuplicates(Long id, String name) {
        SavedSearch existing = savedSearchRepository.findByNameIgnoreCase(name);
        if (existing != null && !existing.getId().equals(id) || (existing != null && id == null)){
            throw new EntityExistsException("savedSearch");
        }
    }

    private SavedSearch addSearchJoins(CreateSavedSearchRequest request, SavedSearch savedSearch) {
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
    private SavedSearch convertToSavedSearch(CreateSavedSearchRequest request) {


        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setName(request.getName());
        
        savedSearch.setType(request.getType());
        
        savedSearch.setKeyword(request.getSearchCandidateRequest().getKeyword());
        savedSearch.setStatuses(getStatusListAsString(request.getSearchCandidateRequest().getStatuses()));
        savedSearch.setGender(request.getSearchCandidateRequest().getGender());
        savedSearch.setOccupationIds(getListAsString(request.getSearchCandidateRequest().getOccupationIds()));
        savedSearch.setOrProfileKeyword(request.getSearchCandidateRequest().getOrProfileKeyword());
        savedSearch.setVerifiedOccupationIds(getListAsString(request.getSearchCandidateRequest().getVerifiedOccupationIds()));
        savedSearch.setVerifiedOccupationSearchType(request.getSearchCandidateRequest().getVerifiedOccupationSearchType());
        savedSearch.setNationalityIds(getListAsString(request.getSearchCandidateRequest().getNationalityIds()));
        savedSearch.setNationalitySearchType(request.getSearchCandidateRequest().getNationalitySearchType());
        savedSearch.setCountryIds(getListAsString(request.getSearchCandidateRequest().getCountryIds()));
        savedSearch.setEnglishMinSpokenLevel(request.getSearchCandidateRequest().getEnglishMinSpokenLevel());
        savedSearch.setEnglishMinWrittenLevel(request.getSearchCandidateRequest().getEnglishMinWrittenLevel());
        Optional<Language> language = request.getSearchCandidateRequest().getOtherLanguageId() != null ? languageRepository.findById(request.getSearchCandidateRequest().getOtherLanguageId()) : null;
        if (language != null && language.isPresent()){
            savedSearch.setOtherLanguage(language.get());
        }
        savedSearch.setOtherMinSpokenLevel(request.getSearchCandidateRequest().getOtherMinSpokenLevel());
        savedSearch.setOtherMinWrittenLevel(request.getSearchCandidateRequest().getOtherMinWrittenLevel());
        savedSearch.setUnRegistered(request.getSearchCandidateRequest().getUnRegistered());
        savedSearch.setLastModifiedFrom(request.getSearchCandidateRequest().getLastModifiedFrom());
        savedSearch.setLastModifiedTo(request.getSearchCandidateRequest().getLastModifiedTo());
//        savedSearch.setCreatedFrom(request.getSearchCandidateRequest().getRegisteredFrom());
//        savedSearch.setCreatedTo(request.getSearchCandidateRequest().getRegisteredTo());
        savedSearch.setMinAge(request.getSearchCandidateRequest().getMinAge());
        savedSearch.setMaxAge(request.getSearchCandidateRequest().getMaxAge());
        savedSearch.setMinEducationLevel(request.getSearchCandidateRequest().getMinEducationLevel());
        savedSearch.setEducationMajorIds(getListAsString(request.getSearchCandidateRequest().getEducationMajorIds()));

        return savedSearch;

    }

    private SearchCandidateRequest convertToSearchCandidateRequest(SavedSearch request) {
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
        searchCandidateRequest.setCountryIds(getIdsFromString(request.getCountryIds()));
        searchCandidateRequest.setEnglishMinSpokenLevel(request.getEnglishMinSpokenLevel());
        searchCandidateRequest.setEnglishMinWrittenLevel(request.getEnglishMinWrittenLevel());
        searchCandidateRequest.setOtherLanguageId(request.getOtherLanguage() != null ? request.getOtherLanguage().getId() : null);
        searchCandidateRequest.setOtherMinSpokenLevel(request.getOtherMinSpokenLevel());
        searchCandidateRequest.setOtherMinWrittenLevel(request.getOtherMinWrittenLevel());
        searchCandidateRequest.setUnRegistered(request.isUnRegistered());
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
