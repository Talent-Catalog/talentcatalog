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

import com.opencsv.CSVWriter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.exception.*;
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.model.es.CandidateEs;
import org.tbbtalent.server.model.sf.Contact;
import org.tbbtalent.server.repository.db.*;
import org.tbbtalent.server.repository.es.CandidateEsRepository;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.*;
import org.tbbtalent.server.request.note.CreateCandidateNoteRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.*;
import org.tbbtalent.server.service.db.email.EmailHelper;
import org.tbbtalent.server.service.db.util.PdfHelper;
import org.tbbtalent.server.util.filesystem.FileSystemFolder;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CandidateServiceImpl implements CandidateService {

    private static final Logger log = LoggerFactory.getLogger(CandidateServiceImpl.class);

    private final UserRepository userRepository;
    private final SavedListRepository savedListRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateEsRepository candidateEsRepository;
    private final CandidateSavedListService candidateSavedListService;
    private final ElasticsearchOperations elasticsearchOperations;
    private final GoogleFileSystemService fileSystemService;
    private final SalesforceService salesforceService;
    private final CountryRepository countryRepository;
    private final CountryService countryService;
    private final EducationLevelRepository educationLevelRepository;
    private final NationalityRepository nationalityRepository;
    private final NationalityService nationalityService;
    private final PasswordHelper passwordHelper;
    private final UserContext userContext;
    private final SavedSearchService savedSearchService;
    private final CandidateNoteService candidateNoteService;
    private final CandidateCitizenshipService candidateCitizenshipService;
    private final CandidateVisaService candidateVisaService;
    private final CandidateDependantService candidateDependantService;
    private final CandidateDestinationService candidateDestinationService;
    private final CandidateExamService candidateExamService;
    private final SurveyTypeRepository surveyTypeRepository;
    private final OccupationRepository occupationRepository;
    private final LanguageLevelRepository languageLevelRepository;
    private final EmailHelper emailHelper;
    private final PdfHelper pdfHelper;

    @Autowired
    public CandidateServiceImpl(UserRepository userRepository,
                                SavedListRepository savedListRepository,
                                SavedSearchRepository savedSearchRepository,
                                CandidateRepository candidateRepository,
                                CandidateEsRepository candidateEsRepository,
                                CandidateSavedListService candidateSavedListService,
                                ElasticsearchOperations elasticsearchOperations,
                                GoogleFileSystemService fileSystemService,
                                SalesforceService salesforceService,
                                CountryRepository countryRepository,
                                CountryService countryService,
                                EducationLevelRepository educationLevelRepository,
                                NationalityRepository nationalityRepository,
                                NationalityService nationalityService,
                                PasswordHelper passwordHelper,
                                UserContext userContext,
                                SavedSearchService savedSearchService,
                                CandidateNoteService candidateNoteService,
                                CandidateCitizenshipService candidateCitizenshipService, 
                                CandidateDependantService candidateDependantService,
                                CandidateDestinationService candidateDestinationService,
                                CandidateVisaService candidateVisaService,
                                CandidateExamService candidateExamService,
                                SurveyTypeRepository surveyTypeRepository,
                                OccupationRepository occupationRepository,
                                LanguageLevelRepository languageLevelRepository,
                                EmailHelper emailHelper, PdfHelper pdfHelper) {
        this.userRepository = userRepository;
        this.savedListRepository = savedListRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.candidateRepository = candidateRepository;
        this.candidateEsRepository = candidateEsRepository;
        this.candidateSavedListService = candidateSavedListService;
        this.elasticsearchOperations = elasticsearchOperations;
        this.countryRepository = countryRepository;
        this.countryService = countryService;
        this.educationLevelRepository = educationLevelRepository;
        this.nationalityRepository = nationalityRepository;
        this.nationalityService = nationalityService;
        this.passwordHelper = passwordHelper;
        this.userContext = userContext;
        this.savedSearchService = savedSearchService;
        this.candidateNoteService = candidateNoteService;
        this.candidateCitizenshipService = candidateCitizenshipService;
        this.candidateDependantService = candidateDependantService;
        this.candidateDestinationService = candidateDestinationService;
        this.candidateVisaService = candidateVisaService;
        this.candidateExamService = candidateExamService;
        this.surveyTypeRepository = surveyTypeRepository;
        this.occupationRepository = occupationRepository;
        this.languageLevelRepository = languageLevelRepository;
        this.emailHelper = emailHelper;
        this.pdfHelper = pdfHelper;
        this.fileSystemService = fileSystemService;
        this.salesforceService = salesforceService;
    }

    @Transactional
    @Override
    public int populateElasticCandidates(
            Pageable pageable, boolean logTotal, boolean createElastic) {
        Page<Candidate> candidates = candidateRepository.findCandidatesWhereStatusNotDeleted(pageable);
        if (logTotal) {
            log.info(candidates.getTotalElements() + " candidates to be processed.");
        }

        int count = 0;
        for (Candidate candidate : candidates) {
            try {
                if (createElastic) {
                    CandidateEs ces = new CandidateEs(candidate);
                    ces = candidateEsRepository.save(ces);

                    //Update textSearchId on candidate.
                    String textSearchId = ces.getId();
                    candidate.setTextSearchId(textSearchId);
                    save(candidate, false);
                } else {
                    //This also handles all the awkward cases - such as
                    //links to non existent proxies - creating them as needed.
                    updateElasticProxy(candidate);
                }

                count++;
            } catch (Exception ex) {
                log.warn("Could not load candidate " + candidate.getId(), ex);
            }
        }

        return count;
    }

    @Override
    public boolean clearCandidateSavedLists(long candidateId) {
        Candidate candidate = candidateRepository.findByIdLoadSavedLists(candidateId);

        boolean done = true;
        if (candidate == null) {
            done = false;
        } else {
            candidateSavedListService.clearCandidateSavedLists(candidate);
        }
        return done;
    }

    @Override
    public Page<Candidate> getSavedListCandidates(long savedListId, SavedListGetRequest request) {
        Page<Candidate> candidatesPage = candidateRepository.findAll(
                new GetSavedListCandidatesQuery(savedListId, request), request.getPageRequestWithoutSort());
        log.info("Found " + candidatesPage.getTotalElements() + " candidates in list");
        return candidatesPage;
    }

    @Override
    public boolean mergeCandidateSavedLists(long candidateId, IHasSetOfSavedLists request) {
        Candidate candidate = candidateRepository.findByIdLoadSavedLists(candidateId);
        
        boolean done = true;
        if (candidate == null) {
            done = false;
        } else {
            Set<SavedList> savedLists = fetchSavedLists(request);
            candidate.addSavedLists(savedLists);

            saveIt(candidate);
        }
        return done;
    }

    @Override
    public boolean removeFromCandidateSavedLists(long candidateId, IHasSetOfSavedLists request) {
        Candidate candidate = candidateRepository.findByIdLoadSavedLists(candidateId);

        boolean done = true;
        if (candidate == null) {
            done = false;
        } else {
            Set<SavedList> savedLists = fetchSavedLists(request);
            for (SavedList savedList : savedLists) {
                candidateSavedListService.removeFromSavedList(candidate, savedList);
            }
        }
        return done;
    }

    private @NotNull Set<SavedList> fetchSavedLists(IHasSetOfSavedLists request) 
            throws NoSuchObjectException {

        Set<SavedList> savedLists = new HashSet<>();

        Set<Long> savedListIds = request.getSavedListIds();
        if (savedListIds != null) {
            for (Long savedListId : savedListIds) {
                SavedList savedList = savedListRepository.findById(savedListId)
                        .orElse(null);
                if (savedList == null) {
                    throw new NoSuchObjectException(SavedList.class, savedListId);
                }
                savedLists.add(savedList);
            }
        }

        return savedLists;
    }

    /**
     * Update audit fields and use repository to save the Candidate
     * @param candidate Entity to save
     */
    private void saveIt(Candidate candidate) {
        candidate.setAuditFields(userContext.getLoggedInUser().orElse(null));
        save(candidate, true);
    }

    //todo this is horrible cloned code duplicated from SavedSearchServiceImpl - factor it out.
    private SearchCandidateRequest convertToSearchCandidateRequest(SavedSearch savedSearch) {
        SearchCandidateRequest searchCandidateRequest = new SearchCandidateRequest();
        searchCandidateRequest.setSavedSearchId(savedSearch.getId());
        searchCandidateRequest.setKeyword(savedSearch.getKeyword());
        searchCandidateRequest.setStatuses(getStatusListFromString(savedSearch.getStatuses()));
        searchCandidateRequest.setGender(savedSearch.getGender());
        searchCandidateRequest.setOccupationIds(getIdsFromString(savedSearch.getOccupationIds()));
        searchCandidateRequest.setMinYrs(savedSearch.getMinYrs());
        searchCandidateRequest.setMaxYrs(savedSearch.getMaxYrs());
        searchCandidateRequest.setVerifiedOccupationIds(getIdsFromString(savedSearch.getVerifiedOccupationIds()));
        searchCandidateRequest.setVerifiedOccupationSearchType(savedSearch.getVerifiedOccupationSearchType());
        searchCandidateRequest.setNationalityIds(getIdsFromString(savedSearch.getNationalityIds()));
        searchCandidateRequest.setNationalitySearchType(savedSearch.getNationalitySearchType());
        searchCandidateRequest.setCountryIds(getIdsFromString(savedSearch.getCountryIds()));
        searchCandidateRequest.setEnglishMinSpokenLevel(savedSearch.getEnglishMinSpokenLevel());
        searchCandidateRequest.setEnglishMinWrittenLevel(savedSearch.getEnglishMinWrittenLevel());
        searchCandidateRequest.setOtherLanguageId(savedSearch.getOtherLanguage() != null ? savedSearch.getOtherLanguage().getId() : null);
        searchCandidateRequest.setOtherMinSpokenLevel(savedSearch.getOtherMinSpokenLevel());
        searchCandidateRequest.setOtherMinWrittenLevel(savedSearch.getOtherMinWrittenLevel());
        searchCandidateRequest.setLastModifiedFrom(savedSearch.getLastModifiedFrom());
        searchCandidateRequest.setLastModifiedTo(savedSearch.getLastModifiedTo());
//        searchCandidateRequest.setRegisteredFrom(request.getCreatedFrom());
//        searchCandidateRequest.setRegisteredTo(request.getCreatedTo());
        searchCandidateRequest.setMinAge(savedSearch.getMinAge());
        searchCandidateRequest.setMaxAge(savedSearch.getMaxAge());
        searchCandidateRequest.setMinEducationLevel(savedSearch.getMinEducationLevel());
        searchCandidateRequest.setEducationMajorIds(getIdsFromString(savedSearch.getEducationMajorIds()));

        List<SearchJoinRequest> searchJoinRequests = new ArrayList<>();
        for (SearchJoin searchJoin : savedSearch.getSearchJoins()) {
            searchJoinRequests.add(new SearchJoinRequest(searchJoin.getChildSavedSearch().getId(), searchJoin.getChildSavedSearch().getName(), searchJoin.getSearchType()));
        }
        searchCandidateRequest.setSearchJoinRequests(searchJoinRequests);

        return searchCandidateRequest;

    }


    String getListAsString(List<Long> ids){
        return !org.springframework.util.CollectionUtils.isEmpty(ids) ? ids.stream().map(String::valueOf)
                .collect(Collectors.joining(",")) : null;
    }

    List<Long> getIdsFromString(String listIds){
        return listIds != null ? Stream.of(listIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList()) : null;
    }

    String getStatusListAsString(List<CandidateStatus> statuses){
        return !org.springframework.util.CollectionUtils.isEmpty(statuses) ? statuses.stream().map(String::valueOf)
                .collect(Collectors.joining(",")) : null;
    }

    List<CandidateStatus> getStatusListFromString(String statusList){
        return statusList != null ? Stream.of(statusList.split(","))
                .map(s -> CandidateStatus.valueOf(s))
                .collect(Collectors.toList()) : null;
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
            List<String> values) {
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

    private void markUserSelectedCandidates(@Nullable Long savedSearchId, Page<Candidate> candidates) {
        if (savedSearchId != null) {
            //Check for selection list to set the selected attribute on returned
            // candidates.
            SavedList selectionList = null;
            User user = userContext.getLoggedInUser().orElse(null);
            if (user != null) {
                selectionList = savedSearchService
                        .getSelectionList(savedSearchId, user.getId());
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

    private Specification<Candidate> computeQuery(SearchCandidateRequest request) {
        //There may be no logged in user if the search is called by the
        //overnight Watcher process.
        User user = userContext.getLoggedInUser().orElse(null);

        //This list is initialized with the main saved search id, but can be
        //added to by addQuery below when the search is built on other
        //searches. The idea is to avoid circular dependencies between searches.
        //For example, in the simplest case we don't want a saved search
        //to be based on itself.
        List<Long> searchIds = new ArrayList<>();
        if (request.getSavedSearchId() != null) {
            searchIds.add(request.getSavedSearchId());
        }

        Specification<Candidate> query = CandidateSpecification.buildSearchQuery(request, user);
        if (CollectionUtils.isNotEmpty(request.getSearchJoinRequests())) {
            for (SearchJoinRequest searchJoinRequest : request.getSearchJoinRequests()) {
                query = addQuery(query, searchJoinRequest, searchIds);
            }
        }
        return query;
    }

    private Page<Candidate> doSearchCandidates(SearchCandidateRequest request) {

        Page<Candidate> candidates;
        String simpleQueryString = request.getSimpleQueryString();
        if (simpleQueryString != null && simpleQueryString.length() > 0) {
            //This is an elastic search request
            BoolQueryBuilder boolQueryBuilder = computeElasticQuery(request,
                simpleQueryString);

            //Define sort from request 
            PageRequest req = CandidateEs.convertToElasticSortField(request);

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
            List<Candidate> unsorted = candidateRepository.findByIds(candidateIds);
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

            Specification<Candidate> query = computeQuery(request);

            candidates = candidateRepository.findAll(query, request.getPageRequestWithoutSort());
        }
        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    private BoolQueryBuilder computeElasticQuery(SearchCandidateRequest request,
        String simpleQueryString) {
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

        //Countries
        final List<Long> countryIds = request.getCountryIds();
        if (countryIds != null) {
            //Look up country names from ids.
            List<String> reqCountries = new ArrayList<>();
            for (Long countryId : countryIds) {
                final Country country = countryService.getCountry(countryId);
                reqCountries.add(country.getName());
            }
            boolQueryBuilder =
                    addElasticTermFilter(boolQueryBuilder,
                            null,"country.keyword", reqCountries);
        }

        //Nationalities
        final List<Long> nationalityIds = request.getNationalityIds();
        if (nationalityIds != null) {
            //Look up names from ids.
            List<String> reqNationalities = new ArrayList<>();
            for (Long id : nationalityIds) {
                final Nationality nationality = nationalityService.getNationality(id);
                reqNationalities.add(nationality.getName());
            }
            boolQueryBuilder = addElasticTermFilter(boolQueryBuilder,
                    request.getNationalitySearchType(),
                    "nationality.keyword", reqNationalities);
        }

        //Statuses
        List<CandidateStatus> statuses = request.getStatuses();
        if (request.getIncludeDraftAndDeleted() != null
                && request.getIncludeDraftAndDeleted()) {
            if (statuses == null) {
                statuses = new ArrayList<>();
            }
            statuses.add(CandidateStatus.draft);
            statuses.add(CandidateStatus.deleted);
        }
        if (statuses != null) {
            //Extract names from enums
            List<String> reqStatuses = new ArrayList<>();
            for (CandidateStatus status : statuses) {
                reqStatuses.add(status.name());
            }
            boolQueryBuilder =
                    addElasticTermFilter(boolQueryBuilder,
                            null,"status.keyword", reqStatuses);
        }

        //Gender
        Gender gender = request.getGender();
        if (gender != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.termQuery("gender", gender.name()));
        }
        return boolQueryBuilder;
    }

    @Override
    public Page<Candidate> searchCandidates(
            long savedSearchId, SavedSearchGetRequest request)
            throws NoSuchObjectException {

        SearchCandidateRequest searchRequest =
                this.savedSearchService.loadSavedSearch(savedSearchId);

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
    public Set<Long> searchCandidates(long savedSearchId)
            throws NoSuchObjectException {
        SearchCandidateRequest searchRequest =
                this.savedSearchService.loadSavedSearch(savedSearchId);

        Set<Long> candidateIds = new HashSet<>();
        String simpleQueryString = searchRequest.getSimpleQueryString();
        if (simpleQueryString != null && simpleQueryString.length() > 0) {
            //This is an elastic search request.
            
            BoolQueryBuilder boolQueryBuilder = computeElasticQuery(searchRequest,
                simpleQueryString);

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
            final Specification<Candidate> query = computeQuery(searchRequest);

            List<Candidate> candidates = candidateRepository.findAll(query);

            for (Candidate candidate : candidates) {
                candidateIds.add(candidate.getId());
            }
        }

        log.info("Found " + candidateIds.size() + " candidates in search");

        return candidateIds;
    }

    @Override
    public Page<Candidate> searchCandidates(SearchCandidateRequest request) {
        Page<Candidate> candidates;
        User user = userContext.getLoggedInUser().orElse(null);
        if (user == null) {
            candidates = doSearchCandidates(request);
        } else {
            //Update default search
            SavedSearch defaultSavedSearch =
                    savedSearchService.getDefaultSavedSearch();
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
            savedSearchService.updateSavedSearch(savedSearchId, updateRequest);

            //Do the search
            candidates = doSearchCandidates(request);

            //Add in any selections
            markUserSelectedCandidates(savedSearchId, candidates);
        }
        
        return candidates;
    }

    @Override
    public Page<Candidate> searchCandidates(CandidateEmailSearchRequest request) {
        String s = request.getCandidateEmail();
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        if (loggedInUser.getRole() == Role.admin || loggedInUser.getRole() == Role.sourcepartneradmin) {
            Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
            Page<Candidate> candidates;

            candidates = candidateRepository.searchCandidateEmail(
                    '%' + s +'%', sourceCountries, request.getPageRequestWithoutSort());

            log.info("Found " + candidates.getTotalElements() + " candidates in search");
            return candidates;
        } else {
            return null;
        }
    }

    @Override
    public Page<Candidate> searchCandidates(CandidateNumberOrNameSearchRequest request) {
        String s = request.getCandidateNumberOrName();
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        boolean searchForNumber = s.length() > 0 && Character.isDigit(s.charAt(0));
        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Page<Candidate> candidates;

        if (searchForNumber) {
            candidates = candidateRepository.searchCandidateNumber(
                        s +'%', sourceCountries,
                    request.getPageRequestWithoutSort());
        } else {
            if (loggedInUser.getRole() == Role.admin || loggedInUser.getRole() == Role.sourcepartneradmin) {
                candidates = candidateRepository.searchCandidateName(
                        '%' + s + '%', sourceCountries,
                        request.getPageRequestWithoutSort());
            } else {
                return null;
            }
        }

        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    @Override
    public Page<Candidate> searchCandidates(CandidatePhoneSearchRequest request) {
        String s = request.getCandidatePhone();
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        if (loggedInUser.getRole() == Role.admin || loggedInUser.getRole() == Role.sourcepartneradmin){
            Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
            Page<Candidate> candidates;

            candidates = candidateRepository.searchCandidatePhone(
                    '%' + s +'%', sourceCountries, request.getPageRequestWithoutSort());

            log.info("Found " + candidates.getTotalElements() + " candidates in search");
            return candidates;
        } else {
            return null;
        }
    }

    Specification<Candidate> addQuery(Specification<Candidate> query, SearchJoinRequest searchJoinRequest, List<Long> savedSearchIds) {
        if (savedSearchIds.contains(searchJoinRequest.getSavedSearchId())) {
            throw new CircularReferencedException(searchJoinRequest.getSavedSearchId());
        }
        User user = userContext.getLoggedInUser().orElse(null);
        //add id to list as do not want circular references
        savedSearchIds.add(searchJoinRequest.getSavedSearchId());
        //load saved search
        SearchCandidateRequest request = savedSearchService.loadSavedSearch(searchJoinRequest.getSavedSearchId());
        Specification<Candidate> joinQuery = CandidateSpecification.buildSearchQuery(request, user);
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

    @Override
    public Candidate addMissingDestinations(Candidate candidate) {
        //Check candidate's preferred destinations
        List<CandidateDestination> destinations = candidate.getCandidateDestinations();
        //Construct hashset of country ids for quick checking
        Set<Long> candidateDestinationCountryIds = new HashSet<>();
        for (CandidateDestination destination : destinations) {
            candidateDestinationCountryIds.add(destination.getCountry().getId());
        }

        //Check that all TBB destinations are present for candidate, adding
        //missing ones if necessary
        boolean addedDestinations = false;
        for (Country country : countryService.getTBBDestinations()) {
            //Does candidate have this destination preference?
            if (!candidateDestinationCountryIds.contains(country.getId())) {
                //If not, add in a new one
                CandidateDestination cd = new CandidateDestination();
                cd.setCountry(country);
                cd.setCandidate(candidate);
                destinations.add(cd);
                addedDestinations = true;
            }
        }
        
        if (addedDestinations) {
            candidate = save(candidate, false);
        }
        return candidate;
    }

    @Override
    public @NonNull Candidate getCandidate(long id) throws NoSuchObjectException {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
    }

    @Override
    @Transactional
    public Candidate createCandidate(CreateCandidateRequest request) throws UsernameTakenException {
        User user = new User(
                StringUtils.isNotBlank(request.getUsername()) ? request.getUsername() : request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                Role.user);

        // set Read Only to default false
        user.setReadOnly(false);

        User existing = userRepository.findByUsernameAndRole(user.getUsername(), Role.user);
        if (existing != null) {
            throw new UsernameTakenException("A user already exists with username: " + existing.getUsername());
        }

        user = this.userRepository.save(user);

        Candidate candidate = new Candidate(user, request.getPhone(), request.getWhatsapp(), user);
        candidate.setCandidateNumber("TEMP%04d" + RandomStringUtils.random(6));

        //set country and nationality to unknown on create as required for search
        candidate.setCountry(countryRepository.getOne(0L));
        candidate.setNationality(nationalityRepository.getOne(0L));

        //Save candidate to get id (but don't update Elasticsearch yet)
        candidate = save(candidate, false);

        //Use id to generate candidate number
        String candidateNumber = String.format("%04d", candidate.getId());
        candidate.setCandidateNumber(candidateNumber);
        
        //Now save again with candidateNumber, updating Elasticsearch
        candidate = save(candidate, true);

        return candidate;
    }

    @Override
    @Transactional
    public Candidate updateCandidateStatus(long id, UpdateCandidateStatusRequest request) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
        CandidateStatus originalStatus = candidate.getStatus();
        candidate.setStatus(request.getStatus());
        candidate.setCandidateMessage(request.getCandidateMessage());
        candidate = save(candidate, true);
        if (!request.getStatus().equals(originalStatus)){
            candidateNoteService.createCandidateNote(new CreateCandidateNoteRequest(id, "Status change from " + originalStatus + " to " + request.getStatus(), request.getComment()));
            if (originalStatus.equals(CandidateStatus.draft) && !request.getStatus().equals(CandidateStatus.deleted)) {
                emailHelper.sendRegistrationEmail(candidate.getUser());
                log.info("Registration email sent to " + candidate.getUser().getEmail());
            }
            if (request.getStatus().equals(CandidateStatus.incomplete)) {
                emailHelper.sendIncompleteApplication(candidate.getUser(), request.getCandidateMessage());
                log.info("Incomplete email sent to " + candidate.getUser().getEmail());
            }
        }
        if (candidate.getStatus().equals(CandidateStatus.deleted)){
            User user = candidate.getUser();
            user.setStatus(Status.deleted);
            userRepository.save(user);
        }
        return candidate;
    }

    @Override
    public Candidate updateCandidateLinks(long id, UpdateCandidateLinksRequest request) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
        candidate.setSflink(request.getSflink());
        candidate.setFolderlink(request.getFolderlink());
        candidate.setVideolink(request.getVideolink());
        candidate.setLinkedInLink(request.getLinkedInLink());
        candidate = save(candidate, true);
        return candidate;
    }

    @Override
    public Candidate updateCandidate(long id, UpdateCandidateRequest request) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
        // Check update request for a duplicate email or phone number
        request.setId(id);
        validateContactRequest(candidate.getUser(), request);

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        // Load the country from the database - throw an exception if not found
        Nationality nationality = nationalityRepository.findById(request.getNationalityId())
                .orElseThrow(() -> new NoSuchObjectException(Nationality.class, request.getNationalityId()));

        User user = candidate.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        userRepository.save(user);

        candidate.setUser(user);
        candidate.setDob(request.getDob());
        candidate.setGender(request.getGender());
        candidate.setPhone(request.getPhone());
        candidate.setWhatsapp(request.getWhatsapp());
        candidate.setAddress1(request.getAddress1());
        candidate.setCity(request.getCity());
        candidate.setCountry(country);
        candidate.setYearOfArrival(request.getYearOfArrival());
        candidate.setNationality(nationality);
        candidate.setUnhcrStatus(request.getUnhcrStatus());
        candidate.setUnhcrNumber(request.getUnhcrNumber());
        return save(candidate, true);
    }

    @Override
    public Candidate updateCandidateAdditionalInfo(long id, UpdateCandidateAdditionalInfoRequest request) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));

        candidate.setAdditionalInfo(request.getAdditionalInfo());
        return save(candidate, true);
    }

    @Override
    public Candidate updateCandidateSurvey(long id, UpdateCandidateSurveyRequest request) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));

        SurveyType surveyType = null;
        if (request.getSurveyTypeId() != null) {
            // Load the education level from the database - throw an exception if not found
            surveyType = surveyTypeRepository.findById(request.getSurveyTypeId())
                    .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, request.getSurveyTypeId()));
        }
        candidate.setSurveyType(surveyType);
        candidate.setSurveyComment(request.getSurveyComment());
        return save(candidate, true);
    }

    @Override
    @Transactional
    public boolean deleteCandidate(long id) {
        Candidate candidate = candidateRepository.findById(id).orElse(null);
        if (candidate != null) {
            String textSearchId = candidate.getTextSearchId();
            candidateRepository.delete(candidate);
            if (textSearchId != null) {
                candidateEsRepository.deleteById(textSearchId);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginRequest register(RegisterCandidateRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new PasswordMatchException();
        }

        // Check update request for a duplicate email or phone number
        validateContactRequest(null, request);

        /* Check for existing account with the username fields */
        if (StringUtils.isNotBlank(request.getUsername())) {
            User exists = userRepository.findByUsernameAndRole(request.getUsername(), Role.user);
            if (exists != null) {
                throw new UsernameTakenException("username");
            }
        }

        if (StringUtils.isBlank(request.getEmail())
                && StringUtils.isBlank(request.getPhone())
                && StringUtils.isBlank(request.getWhatsapp())) {
            throw new InvalidRequestException("Must specify at least one method of contact");
        }

        /* Validate the password before account creation */
        String passwordEncrypted = passwordHelper.validateAndEncodePassword(request.getPassword());

        /* Create the candidate */
        CreateCandidateRequest createCandidateRequest = new CreateCandidateRequest();
        createCandidateRequest.setUsername(request.getUsername());
        createCandidateRequest.setEmail(request.getEmail());
        createCandidateRequest.setPhone(request.getPhone());
        createCandidateRequest.setWhatsapp(request.getWhatsapp());
        Candidate candidate = createCandidate(createCandidateRequest);

        /* Update the password */
        User user = candidate.getUser();
        user.setPasswordEnc(passwordEncrypted);
        user = this.userRepository.save(user);

        /* Log the candidate in */
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(user.getUsername());
        loginRequest.setPassword(request.getPassword());
        return loginRequest;
    }

    @Override
    public Candidate updateContact(UpdateCandidateContactRequest request) {
        User user = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        // Check update request for a duplicate email or phone number
        validateContactRequest(user, request);

        user.setEmail(request.getEmail());
        user = userRepository.save(user);
        Candidate candidate = user.getCandidate();
        candidate.setPhone(request.getPhone());
        candidate.setWhatsapp(request.getWhatsapp());
        candidate.setAuditFields(user);
        candidate.setUser(user);
        candidate = save(candidate, true);
        return candidate;
    }

    @Override
    public Candidate updatePersonal(UpdateCandidatePersonalRequest request) {
        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        // Load the nationality from the database - throw an exception if not found
        Nationality nationality = nationalityRepository.findById(request.getNationality())
                .orElseThrow(() -> new NoSuchObjectException(Nationality.class, request.getNationality()));

        User user = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user = userRepository.save(user);
        Candidate candidate = candidateRepository.findByUserId(user.getId());
        if (candidate != null) {
            candidate.setGender(request.getGender());
            candidate.setDob(request.getDob());
            candidate.setCountry(country);
            candidate.setCity(request.getCity());
            candidate.setYearOfArrival(request.getYearOfArrival());
            candidate.setNationality(nationality);

            candidate.setAuditFields(user);
        }
        return save(candidate, true);
    }

    @Override
    public Candidate updateEducation(UpdateCandidateEducationRequest request) {
        Candidate candidate = getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        EducationLevel educationLevel = null;
        if (request.getMaxEducationLevelId() != null) {
            // Load the education level from the database - throw an exception if not found
            educationLevel = educationLevelRepository.findById(request.getMaxEducationLevelId())
                    .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, request.getMaxEducationLevelId()));
        }

        candidate.setMaxEducationLevel(educationLevel);
        candidate.setAuditFields(candidate.getUser());
        return save(candidate, true);
    }

    @Override
    public Candidate updateCandidateSurvey(UpdateCandidateSurveyRequest request) {
        Candidate candidate = getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        SurveyType surveyType = null;
        if (request.getSurveyTypeId() != null) {
            // Load the education level from the database - throw an exception if not found
            surveyType = surveyTypeRepository.findById(request.getSurveyTypeId())
                    .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, request.getSurveyTypeId()));
        }
        candidate.setSurveyType(surveyType);
        candidate.setSurveyComment(request.getSurveyComment());

        candidate.setAuditFields(candidate.getUser());
        return save(candidate, true);
    }

    @Override
    public Candidate updateAdditionalInfo(UpdateCandidateAdditionalInfoRequest request) {
        Candidate candidate = getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        candidate.setAdditionalInfo(request.getAdditionalInfo());
        candidate.setLinkedInLink(request.getLinkedInLink());
        candidate.setAuditFields(candidate.getUser());
        return save(candidate, true);
    }

    @Override
    public Candidate submitRegistration() {
        Candidate candidate = getLoggedInCandidate()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));
        // Don't update status to pending if status is already pending
        if (!candidate.getStatus().equals(CandidateStatus.pending)) {
            updateCandidateStatus(candidate.getId(), new UpdateCandidateStatusRequest(CandidateStatus.pending, "Candidate submitted"));
        }
        candidate.setAuditFields(candidate.getUser());
        return save(candidate, true);
    }

    @Override
    public Optional<Candidate> getLoggedInCandidateLoadCandidateOccupations() {
        Long candidateId = userContext.getLoggedInCandidateId();
        if (candidateId == null) {
            return Optional.empty();
        } else {
            Candidate candidate = candidateRepository
                    .findByIdLoadCandidateOccupations(candidateId);
            return candidate == null ? Optional.empty() : Optional.of(candidate);
        }
    }

    @Override
    public Optional<Candidate> getLoggedInCandidateLoadCertifications() {
        Long candidateId = userContext.getLoggedInCandidateId();
        if (candidateId == null) {
            return Optional.empty();
        } else {
            Candidate candidate = candidateRepository
                    .findByIdLoadCertifications(candidateId);
            return candidate == null ? Optional.empty() : Optional.of(candidate);
        }
    }

    @Override
    public Optional<Candidate> getLoggedInCandidateLoadCandidateLanguages() {
        Long candidateId = userContext.getLoggedInCandidateId();
        if (candidateId == null) {
            return Optional.empty();
        } else {
            Candidate candidate = candidateRepository
                    .findByIdLoadCandidateLanguages(candidateId);
            return candidate == null ? Optional.empty() : Optional.of(candidate);
        }
    }

    @Override
    public Optional<Candidate> getLoggedInCandidate() {
        User user = userContext.getLoggedInUser().orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        Candidate candidate = candidateRepository.findByUserId(user.getId()); 
        return candidate == null ? Optional.empty() : Optional.of(candidate);
    }

    @Override
    public Candidate findByCandidateNumber(String candidateNumber) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        return candidateRepository.findByCandidateNumberRestricted(candidateNumber, sourceCountries)
                .orElseThrow(() -> new CountryRestrictionException("You don't have access to this candidate."));
    }

    @Transactional(readOnly = true)
    void validateContactRequest(User user, BaseCandidateContactRequest request) {
        // Check email not already taken
        if (!StringUtils.isBlank(request.getEmail())) {
            try {
                User exists = userRepository.findByEmailIgnoreCase(request.getEmail());
                if (user == null && exists != null || exists != null && !exists.getId().equals(user.getId())) {
                    throw new UsernameTakenException("email");
                }
            } catch (IncorrectResultSizeDataAccessException e) {
                throw new UsernameTakenException("email");
            }
        }
    }

    private static String countryStr(String country) {
        return country == null ? "%" : country;
    }

    private static String genderStr(Gender gender) {
        return gender == null ? "%" : gender.toString();
    }

    private static List<DataRow> toRows(List<Object[]> objects) {
        List<DataRow> dataRows = new ArrayList<>(objects.size());
        for (Object[] row: objects) {
            String label = row[0] == null ? "undefined" : row[0].toString(); 
            DataRow dataRow = new DataRow(label, (BigInteger)row[1]);
            dataRows.add(dataRow);
        }
        return dataRows;
    }

    @Override
    public List<DataRow> computeBirthYearStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByBirthYearOrderByYear(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeBirthYearStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByBirthYearOrderByYear(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeGenderStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByGenderOrderByCount(
                sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeGenderStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByGenderOrderByCount(
                sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeLanguageStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByLanguageOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeLanguageStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByLanguageOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo, candidateIds));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeMaxEducationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByMaxEducationLevelOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeMaxEducationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByMaxEducationLevelOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeMostCommonOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByMostCommonOccupationOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeMostCommonOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByMostCommonOccupationOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo, candidateIds));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeNationalityStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByNationalityOrderByCount(
                        genderStr(gender), countryStr(country),
                        sourceCountryIds, dateFrom, dateTo));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeNationalityStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        List<DataRow> rows = toRows(candidateRepository.
                countByNationalityOrderByCount(
                        genderStr(gender), countryStr(country),
                        sourceCountryIds, dateFrom, dateTo, candidateIds));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByOccupationOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countByOccupationOrderByCount(
                        genderStr(gender), sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeRegistrationOccupationStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        final List<DataRow> rows = toRows(candidateRepository.countByOccupationOrderByCount(
                sourceCountryIds, dateFrom, dateTo));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeRegistrationOccupationStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        final List<DataRow> rows = toRows(candidateRepository.countByOccupationOrderByCount(
                sourceCountryIds, dateFrom, dateTo, candidateIds));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> computeRegistrationStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByCreatedDateOrderByCount(
                sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeRegistrationStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.countByCreatedDateOrderByCount(
                sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeSpokenLanguageLevelStats(Gender gender, String language, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countBySpokenLanguageLevelByCount(genderStr(gender), language,
                        sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeSpokenLanguageLevelStats(Gender gender, String language, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countBySpokenLanguageLevelByCount(genderStr(gender), language,
                        sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public List<DataRow> computeSurveyStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countBySurveyOrderByCount(
                        genderStr(gender), countryStr(country),
                        sourceCountryIds, dateFrom, dateTo));
    }

    @Override
    public List<DataRow> computeSurveyStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds) {
        return toRows(candidateRepository.
                countBySurveyOrderByCount(
                        genderStr(gender), countryStr(country),
                        sourceCountryIds, dateFrom, dateTo, candidateIds));
    }

    @Override
    public Resource generateCv(Candidate candidate) {
       return pdfHelper.generatePdf(candidate);
    }

    // List export
    @Override
    public void exportToCsv(
            long savedListId, SavedListGetRequest request, PrintWriter writer) 
            throws ExportFailedException {
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(getExportTitles());

            request.setPageNumber(0);
            request.setPageSize(500);
            boolean hasMore = true;
            while (hasMore) {
                Page<Candidate> result = getSavedListCandidates(savedListId, request);
                for (Candidate candidate : result.getContent()) {
                    candidate.setContextSavedListId(savedListId);
                    csvWriter.writeNext(getExportCandidateStrings(candidate));
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

    // Search export
    @Override
    public void exportToCsv(
            long savedSearchId, SavedSearchGetRequest request, PrintWriter writer) 
            throws ExportFailedException {
        SearchCandidateRequest searchRequest =
                this.savedSearchService.loadSavedSearch(savedSearchId);

        //Merge the SavedSearchGetRequest - notably the page request - in to
        //the standard saved search request. 
        searchRequest.merge(request);
        exportToCsv(searchRequest, writer);
    }

    @Override
    public void exportToCsv(SearchCandidateRequest request, PrintWriter writer) 
            throws ExportFailedException {
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(getExportTitles());

            request.setPageNumber(0);
            request.setPageSize(500);
            boolean hasMore = true;
            while (hasMore) {
                Page<Candidate> result = doSearchCandidates(request);
                setCandidateContext(request.getSavedSearchId(), result);
                for (Candidate candidate : result.getContent()) {
                    csvWriter.writeNext(getExportCandidateStrings(candidate));
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

    /**
     * Mark the Candidate objects with any context associated with the
     * selection list of the saved search.
     * This means that context fields (ie ContextNote) associated with the
     * saved search will be returned through the DtoBuilder if present.
     */
    @Override
    public void setCandidateContext(long savedSearchId, Iterable<Candidate> candidates) {
        User user = userContext.getLoggedInUser().orElse(null);
        SavedList selectionList = null;
        if (user != null) {
            selectionList = savedSearchService
                    .getSelectionList(savedSearchId, user.getId());
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

    private String[] getExportTitles() {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Role role = loggedInUser.getRole();
        if(role == Role.semilimited){
            return new String[]{
                    "Candidate Number", "Gender", "Country Residing", "Nationality",
                    "Dob", "Max Education Level", "Education Major", "English Spoken Level", "Occupation", "Context Note", "Link"
            };
        }else if(role == Role.limited){
            return new String[]{
                    "Candidate Number", "Gender", "Dob", "Max Education Level", "Education Major",
                    "English Spoken Level", "Occupation", "Context Note", "Link"
            };
        } else {
            return new String[]{
                    "Candidate Number", "Candidate First Name", "Candidate Last Name", "Gender", "Country Residing", "Nationality",
                    "Dob", "Email", "Max Education Level", "Education Major", "English Spoken Level", "Occupation", "Context Note", "Link"
            };
        }
    }

    private String[] getExportCandidateStrings(Candidate candidate) {
        User loggedInUser = userContext.getLoggedInUser()
                .orElseThrow(() -> new InvalidSessionException("Not logged in"));

        Role role = loggedInUser.getRole();
        if (role == Role.semilimited) {
            return new String[] {
                    candidate.getCandidateNumber(),
                    candidate.getGender() != null ? candidate.getGender().toString() : null,
                    candidate.getCountry() != null ? candidate.getCountry().getName() : candidate.getMigrationCountry(),
                    candidate.getNationality() != null ? candidate.getNationality().getName() : null,
                    candidate.getDob() != null ? candidate.getDob().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) : null,
                    candidate.getMaxEducationLevel() != null ? candidate.getMaxEducationLevel().getName() : null,
                    formatCandidateMajor(candidate.getCandidateEducations()),
                    getEnglishSpokenProficiency(candidate.getCandidateLanguages()),
                    formatCandidateOccupation(candidate.getCandidateOccupations()),
                    candidate.getContextNote() != null ? candidate.getContextNote() : null,
                    getCandidateExternalHref(candidate.getCandidateNumber())
            };
        } else if (role == Role.limited) {
            return new String[] {
                    candidate.getCandidateNumber(),
                    candidate.getGender() != null ? candidate.getGender().toString() : null,
                    candidate.getDob() != null ? candidate.getDob().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) : null,
                    candidate.getMaxEducationLevel() != null ? candidate.getMaxEducationLevel().getName() : null,
                    formatCandidateMajor(candidate.getCandidateEducations()),
                    getEnglishSpokenProficiency(candidate.getCandidateLanguages()),
                    formatCandidateOccupation(candidate.getCandidateOccupations()),
                    candidate.getContextNote() != null ? candidate.getContextNote() : null,
                    getCandidateExternalHref(candidate.getCandidateNumber())
            };
        } else {
            return new String[] {
                    candidate.getCandidateNumber(),
                    candidate.getUser().getFirstName(),
                    candidate.getUser().getLastName(),
                    candidate.getGender() != null ? candidate.getGender().toString() : null,
                    candidate.getCountry() != null ? candidate.getCountry().getName() : candidate.getMigrationCountry(),
                    candidate.getNationality() != null ? candidate.getNationality().getName() : null,
                    candidate.getDob() != null ? candidate.getDob().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) : null,
                    candidate.getUser().getEmail(),
                    candidate.getMaxEducationLevel() != null ? candidate.getMaxEducationLevel().getName() : null,
                    formatCandidateMajor(candidate.getCandidateEducations()),
                    getEnglishSpokenProficiency(candidate.getCandidateLanguages()),
                    formatCandidateOccupation(candidate.getCandidateOccupations()),
                    candidate.getContextNote() != null ? candidate.getContextNote() : null,
                    getCandidateExternalHref(candidate.getCandidateNumber())
            };
        }
    }

    private String getCandidateExternalHref(String candidateNumber) {
        return "https://www.tbbtalent.org/admin-portal/candidate/" + candidateNumber;
    }

    public String formatCandidateMajor(List<CandidateEducation> candidateEducations){
        StringBuilder buffer = new StringBuilder();
        if (!CollectionUtils.isEmpty(candidateEducations)){
            for (CandidateEducation candidateEducation : candidateEducations) {
                if (candidateEducation.getEducationMajor() != null){
                    buffer.append(candidateEducation.getEducationMajor().getName()).append("\n");
                }
            }
        }
        return buffer.toString();

    }

    public String formatCandidateOccupation(List<CandidateOccupation> candidateOccupations){
        StringBuilder buffer = new StringBuilder();
        if (!CollectionUtils.isEmpty(candidateOccupations)){
            for (CandidateOccupation candidateOccupation : candidateOccupations) {
                if (candidateOccupation.getOccupation() != null){
                    buffer.append(candidateOccupation.getOccupation().getName()).append("\n");
                }
            }
        }
        return buffer.toString();

    }

    public String getEnglishSpokenProficiency(List<CandidateLanguage> candidateLanguages){
        StringBuilder buffer = new StringBuilder();
        if (!CollectionUtils.isEmpty(candidateLanguages)){
            for (CandidateLanguage candidateLanguage : candidateLanguages) {
                if ((candidateLanguage.getLanguage() != null) && "english".equalsIgnoreCase(candidateLanguage.getLanguage().getName())){
                    if(candidateLanguage.getSpokenLevel() != null) {
                        buffer.append(candidateLanguage.getSpokenLevel().getName()).append("\n");
                    }
                }
            }
        }
        return buffer.toString();

    }

    private List<DataRow> limitRows(List<DataRow> rawData, int limit) {
        if (rawData.size() > limit) {
            List<DataRow> result = new ArrayList<>(rawData.subList(0, limit - 1));
            BigDecimal other = BigDecimal.ZERO;
            for (int i = limit-1; i < rawData.size(); i++) {
                other = other.add(rawData.get(i).getValue());
            }
            if (other.compareTo(BigDecimal.ZERO) != 0) {
                result.add(new DataRow("Other", other));
            }
            return result;
        } else {
            return rawData;
        }
    }

    //Midnight GMT
    @Scheduled(cron = "0 1 0 * * ?", zone = "GMT")
    public void notifySearchWatchers() {
        String currentSearch = "";
        try {
            Set<SavedSearch> searches = savedSearchRepository.findByWatcherIdsIsNotNullLoadSearchJoins();
            Map<Long, Set<SavedSearch>> userNotifications = new HashMap<>();

            log.info("Notify watchers: running " + searches.size() + " searches");
            
            int count = 0;
            
            for (SavedSearch savedSearch : searches) {

                count++;
                currentSearch = savedSearch.getName();
                log.info("Running search " + count + ": " + currentSearch);
                
                SearchCandidateRequest searchCandidateRequest =
                        convertToSearchCandidateRequest(savedSearch);

                LocalDate date = LocalDate.now().minusDays(1);
                searchCandidateRequest.setFromDate(date);
                Page<Candidate> candidates =
                        doSearchCandidates(searchCandidateRequest);

                if (candidates.getNumberOfElements() > 0) {
                    //Query has results. Need to let watchers know
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

    /**
     * Get a user’s source countries, defaulting to all countries if empty
     */
    public Set<Country> getDefaultSourceCountries(User user){
        Set<Country> countries;
        if(CollectionUtils.isEmpty(user.getSourceCountries())){
            countries = new HashSet<>(countryRepository.findAll());
        } else {
            countries = user.getSourceCountries();
        }
        return countries;
    }

    @Override
    public Candidate save(Candidate candidate, boolean updateCandidateEs) {
        candidate = candidateRepository.save(candidate);
        
        if (updateCandidateEs) {
            candidate = updateElasticProxy(candidate);
        }
        return candidate;
    }

    /**
     * Does whatever is needed to bring the Elastic proxy into sync with 
     * its parent candidate on the normal database.
     * <p>
     *     Handles the following cases:
     * </p>
     * <ul>
     *     <li>Normal case: updates proxy indicated by textSearchId of master</li>
     *     <li>Master has no linked proxy (no textSearchId) - create one </li>
     *     <li>Master has a textSearchId, but no such proxy is found, 
     *     log warning but create a proxy</li>
     * </ul>
     * @param candidate Candidate entity (the master) from thr normal database
     * @return Potentially modified candidate entity (with latest textSearchId)
     */
    private Candidate updateElasticProxy(Candidate candidate) {
        //Find/create Elasticsearch twin candidate
        CandidateEs twin;
        //Get textSearchId, if any
        String textSearchId = candidate.getTextSearchId();
        String originalTextSearchId = textSearchId;
        if (textSearchId == null) {
            //No twin - create one
            twin = new CandidateEs(candidate);
        } else {
            //Get twin
            twin = candidateEsRepository.findById(textSearchId)
                    .orElse(null);
            if (twin == null) {
                //Candidate is referring to non existent twin.
                //Create new twin
                twin = new CandidateEs(candidate);

                //Shouldn't really happen (except during a complete reload) 
                // so log warning
                log.warn("Candidate " + candidate.getId() +
                        " refers to non existent Elasticsearch id "
                        + textSearchId + ". Creating new twin.");
            } else {
                //Update twin from candidate
                twin.copy(candidate);
            }
        }
        twin = candidateEsRepository.save(twin);
        textSearchId = twin.getId();

        //Update textSearchId on candidate if necessary
        if (!textSearchId.equals(originalTextSearchId)) {
            candidate.setTextSearchId(textSearchId);
            candidate = candidateRepository.save(candidate);
        }
        return candidate;
    }
    
    @Override
    public Candidate createCandidateFolder(long id) 
            throws NoSuchObjectException, IOException {
        Candidate candidate = getCandidate(id);
        
        String candidateNumber = candidate.getCandidateNumber();
        
        FileSystemFolder folder = fileSystemService.findAFolder(candidateNumber);
        
        if (folder == null) {
            folder = fileSystemService.createFolder(candidateNumber);
        }
        candidate.setFolderlink(folder.getUrl());
        save(candidate, false);
        return candidate;
    }

    @Override
    public Candidate createUpdateSalesforce(long id) 
            throws NoSuchObjectException, GeneralSecurityException, 
            WebClientException {
        Candidate candidate = getCandidate(id);
        
        Contact candidateSf = salesforceService.createOrUpdateContact(candidate);
        candidate.setSflink(candidateSf.getUrl());

        save(candidate, false);
        return candidate;
    }

    @Override
    public void updateIntakeData(long id, CandidateIntakeDataUpdate data) 
            throws NoSuchObjectException {
        Candidate candidate = getCandidate(id);

        //If there is a non null citizen nationality, that means that this
        //is a citizenship update.
        final Long citizenNationalityId = data.getCitizenNationalityId();
        if (citizenNationalityId != null) {
            candidateCitizenshipService
                    .updateIntakeData(citizenNationalityId, candidate, data);
        }

        //If there is a non null dependent relation, that means that this
        //is a dependant update.
        final DependantRelations dependantRelation = data.getDependantRelation();
        if (dependantRelation != null) {
            candidateDependantService
                    .updateIntakeData(candidate, data);
        }

        //If there is a non null destination country, that means that this
        //is a destination update.
        final Long destinationCountryId = data.getDestinationCountryId();
        if (destinationCountryId != null) {
            candidateDestinationService
                    .updateIntakeData(destinationCountryId, candidate, data);
        }

        //If there is a non null visa country, that means that this
        //is a visa check update.
        final Long visaCountryId = data.getVisaCountryId();
        if (visaCountryId != null) {
            candidateVisaService
                    .updateIntakeData(visaCountryId, candidate, data);
        }

        //If there is a non null exam type, that means that this
        //is a exam update.
        final Exam exam = data.getExamType();
        if (exam != null) {
            candidateExamService
                    .updateIntakeData(candidate, data);
        }

        //Get the partner candidate object from the id in the data request and pass into the populateIntakeData method
        final Long partnerCandId = data.getPartnerCandId();
        Candidate partnerCandidate = null;
        if (partnerCandId != null) {
            partnerCandidate = candidateRepository.findById(partnerCandId).orElse(null);
        }

        //Get the partner education level object from the id in the data request and pass into the populateIntakeData method
        final Long partnerEduLevelId = data.getPartnerEduLevelId();
        EducationLevel partnerEducationLevel = null;
        if (partnerEduLevelId != null) {
            partnerEducationLevel = educationLevelRepository.findById(partnerEduLevelId).orElse(null);
        }

        final Long partnerOccupationId = data.getPartnerOccupationId();
        Occupation partnerOccupation = null;
        if (partnerOccupationId != null) {
            partnerOccupation = occupationRepository.findById(partnerOccupationId).orElse(null);
        }

        final Long partnerEnglishLevelId = data.getPartnerEnglishLevelId();
        LanguageLevel partnerEnglishLevel = null;
        if (partnerEnglishLevelId != null) {
            partnerEnglishLevel = languageLevelRepository.findById(partnerEnglishLevelId).orElse(null);
        }

        final Long partnerCitizenshipId = data.getPartnerCitizenshipId();
        Nationality partnerCitizenship = null;
        if (partnerCitizenshipId != null) {
            partnerCitizenship = nationalityRepository.findById(partnerCitizenshipId).orElse(null);
        }

        final Long drivingLicenseCountryId = data.getDrivingLicenseCountryId();
        Country drivingLicenseCountry = null;
        if (drivingLicenseCountryId != null) {
            drivingLicenseCountry = countryRepository.findById(drivingLicenseCountryId).orElse(null);
        }

        final Long birthCountryId = data.getBirthCountryId();
        Country birthCountry = null;
        if (birthCountryId != null) {
            birthCountry = countryRepository.findById(birthCountryId).orElse(null);
        }

        candidate.populateIntakeData(data, partnerCandidate, partnerEducationLevel,
                partnerOccupation, partnerEnglishLevel, partnerCitizenship, drivingLicenseCountry, birthCountry);

        save(candidate, true);

    }
}
