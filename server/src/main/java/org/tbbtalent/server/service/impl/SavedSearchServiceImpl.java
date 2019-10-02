package org.tbbtalent.server.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SavedSearchServiceImpl implements SavedSearchService {

    private static final Logger log = LoggerFactory.getLogger(SavedSearchServiceImpl.class);

    private final CandidateRepository candidateRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final SearchJoinRepository searchJoinRepository;
    private final LanguageLevelRepository languageLevelRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final LanguageRepository languageRepository;
    private final UserContext userContext;

    @Autowired
    public SavedSearchServiceImpl(CandidateRepository candidateRepository, SavedSearchRepository savedSearchRepository, SearchJoinRepository searchJoinRepository, LanguageLevelRepository languageLevelRepository, EducationLevelRepository educationLevelRepository, LanguageRepository languageRepository, UserContext userContext) {
        this.candidateRepository = candidateRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.searchJoinRepository = searchJoinRepository;
        this.languageLevelRepository = languageLevelRepository;
        this.educationLevelRepository = educationLevelRepository;
        this.languageRepository = languageRepository;
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
        return this.savedSearchRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, id));
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
        SavedSearch savedSearch = convertToSavedSearch(request);
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

        Map<Long, LanguageLevel> languageLevelMap = languageLevelRepository.findAll()
                .stream()
                .collect( Collectors.toMap(LanguageLevel::getId, Function.identity()) );

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setName(request.getName());
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
        savedSearch.setEnglishMinSpokenLevel(languageLevelMap.get(request.getSearchCandidateRequest().getEnglishMinSpokenLevelId()));
        savedSearch.setEnglishMinWrittenLevel(languageLevelMap.get(request.getSearchCandidateRequest().getEnglishMinWrittenLevelId()));
        Optional<Language> language = request.getSearchCandidateRequest().getOtherLanguageId() != null ? languageRepository.findById(request.getSearchCandidateRequest().getOtherLanguageId()) : null;
        if (language != null && language.isPresent()){
            savedSearch.setOtherLanguage(language.get());
        }
        savedSearch.setOtherMinSpokenLevel(languageLevelMap.get(request.getSearchCandidateRequest().getOtherMinSpokenLevelId()));
        savedSearch.setOtherMinWrittenLevel(languageLevelMap.get(request.getSearchCandidateRequest().getOtherMinWrittenLevelId()));
        savedSearch.setUnRegistered(request.getSearchCandidateRequest().getUnRegistered());
        savedSearch.setLastModifiedFrom(request.getSearchCandidateRequest().getLastModifiedFrom());
        savedSearch.setLastModifiedTo(request.getSearchCandidateRequest().getLastModifiedTo());
        savedSearch.setCreatedFrom(request.getSearchCandidateRequest().getCreatedFrom());
        savedSearch.setCreatedTo(request.getSearchCandidateRequest().getCreatedTo());
        savedSearch.setMinAge(request.getSearchCandidateRequest().getMinAge());
        savedSearch.setMaxAge(request.getSearchCandidateRequest().getMaxAge());
        Optional<EducationLevel> educationLevel = request.getSearchCandidateRequest().getMinEducationLevelId() != null ? educationLevelRepository.findById(request.getSearchCandidateRequest().getMinEducationLevelId()) : null;
        if (educationLevel != null && educationLevel.isPresent()){
            savedSearch.setMinEducationLevel(educationLevel.get());
        }
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
        searchCandidateRequest.setEnglishMinSpokenLevelId(request.getEnglishMinSpokenLevel() != null ? request.getEnglishMinSpokenLevel().getId() : null);
        searchCandidateRequest.setEnglishMinWrittenLevelId(request.getEnglishMinWrittenLevel() != null ? request.getEnglishMinWrittenLevel().getId() : null);
        searchCandidateRequest.setOtherLanguageId(request.getOtherLanguage() != null ? request.getOtherLanguage().getId() : null);
        searchCandidateRequest.setOtherMinSpokenLevelId(request.getOtherMinSpokenLevel() != null ? request.getOtherMinSpokenLevel().getId() : null);
        searchCandidateRequest.setOtherMinWrittenLevelId(request.getOtherMinWrittenLevel() != null ? request.getOtherMinWrittenLevel().getId() : null);
        searchCandidateRequest.setUnRegistered(request.isUnRegistered());
        searchCandidateRequest.setLastModifiedFrom(request.getLastModifiedFrom());
        searchCandidateRequest.setLastModifiedTo(request.getLastModifiedTo());
        searchCandidateRequest.setCreatedFrom(request.getCreatedFrom());
        searchCandidateRequest.setCreatedTo(request.getCreatedTo());
        searchCandidateRequest.setMinAge(request.getMinAge());
        searchCandidateRequest.setMaxAge(request.getMaxAge());
        searchCandidateRequest.setMinEducationLevelId(request.getMinEducationLevel() != null ? request.getMinEducationLevel().getId() : null);
        searchCandidateRequest.setEducationMajorIds(getIdsFromString(request.getEducationMajorIds()));

        List<SearchJoinRequest> searchJoinRequests = new ArrayList<>();
        for (SearchJoin searchJoin : request.getSearchJoins()) {
            searchJoinRequests.add(new SearchJoinRequest(searchJoin.getChildSavedSearch().getId(), searchJoin.getSearchType()));
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
