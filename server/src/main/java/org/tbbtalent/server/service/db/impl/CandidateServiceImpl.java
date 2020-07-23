package org.tbbtalent.server.service.db.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.CircularReferencedException;
import org.tbbtalent.server.exception.CountryRestrictionException;
import org.tbbtalent.server.exception.ExportFailedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.PasswordMatchException;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.model.db.CandidateLanguage;
import org.tbbtalent.server.model.db.CandidateOccupation;
import org.tbbtalent.server.model.db.CandidateStatus;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.DataRow;
import org.tbbtalent.server.model.db.EducationLevel;
import org.tbbtalent.server.model.db.Gender;
import org.tbbtalent.server.model.db.Nationality;
import org.tbbtalent.server.model.db.Role;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.model.db.SearchJoin;
import org.tbbtalent.server.model.db.SearchType;
import org.tbbtalent.server.model.db.Status;
import org.tbbtalent.server.model.db.SurveyType;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.model.es.CandidateEs;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.CandidateSpecification;
import org.tbbtalent.server.repository.db.CountryRepository;
import org.tbbtalent.server.repository.db.EducationLevelRepository;
import org.tbbtalent.server.repository.db.GetSavedListCandidatesQuery;
import org.tbbtalent.server.repository.db.NationalityRepository;
import org.tbbtalent.server.repository.db.SavedListRepository;
import org.tbbtalent.server.repository.db.SavedSearchRepository;
import org.tbbtalent.server.repository.db.SurveyTypeRepository;
import org.tbbtalent.server.repository.db.UserRepository;
import org.tbbtalent.server.repository.es.CandidateEsRepository;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.BaseCandidateContactRequest;
import org.tbbtalent.server.request.candidate.CandidateEmailSearchRequest;
import org.tbbtalent.server.request.candidate.CandidateNumberOrNameSearchRequest;
import org.tbbtalent.server.request.candidate.CandidatePhoneSearchRequest;
import org.tbbtalent.server.request.candidate.CreateCandidateRequest;
import org.tbbtalent.server.request.candidate.IHasSetOfSavedLists;
import org.tbbtalent.server.request.candidate.RegisterCandidateRequest;
import org.tbbtalent.server.request.candidate.SavedListGetRequest;
import org.tbbtalent.server.request.candidate.SavedSearchGetRequest;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.SearchJoinRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateAdditionalInfoRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateContactRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateLinksRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidatePersonalRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateStatusRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateSurveyRequest;
import org.tbbtalent.server.request.candidate.stat.CandidateStatDateRequest;
import org.tbbtalent.server.request.note.CreateCandidateNoteRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.db.CandidateNoteService;
import org.tbbtalent.server.service.db.CandidateService;
import org.tbbtalent.server.service.db.SavedSearchService;
import org.tbbtalent.server.service.db.email.EmailHelper;
import org.tbbtalent.server.service.db.util.PdfHelper;

import com.opencsv.CSVWriter;

@Service
public class CandidateServiceImpl implements CandidateService {

    private static final Logger log = LoggerFactory.getLogger(CandidateServiceImpl.class);

    private final UserRepository userRepository;
    private final SavedListRepository savedListRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateEsRepository candidateEsRepository;
    private final CountryRepository countryRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final NationalityRepository nationalityRepository;
    private final PasswordHelper passwordHelper;
    private final UserContext userContext;
    private final SavedSearchService savedSearchService;
    private final CandidateNoteService candidateNoteService;
    private final SurveyTypeRepository surveyTypeRepository;
    private final EmailHelper emailHelper;
    private final PdfHelper pdfHelper;

    @Autowired
    public CandidateServiceImpl(UserRepository userRepository,
                                SavedListRepository savedListRepository,
                                SavedSearchRepository savedSearchRepository,
                                CandidateRepository candidateRepository,
                                CandidateEsRepository candidateEsRepository,
                                CountryRepository countryRepository,
                                EducationLevelRepository educationLevelRepository,
                                NationalityRepository nationalityRepository,
                                PasswordHelper passwordHelper,
                                UserContext userContext,
                                SavedSearchService savedSearchService,
                                CandidateNoteService candidateNoteService,
                                SurveyTypeRepository surveyTypeRepository,
                                EmailHelper emailHelper, PdfHelper pdfHelper) {
        this.userRepository = userRepository;
        this.savedListRepository = savedListRepository;
        this.savedSearchRepository = savedSearchRepository;
        this.candidateRepository = candidateRepository;
        this.candidateEsRepository = candidateEsRepository;
        this.countryRepository = countryRepository;
        this.educationLevelRepository = educationLevelRepository;
        this.nationalityRepository = nationalityRepository;
        this.passwordHelper = passwordHelper;
        this.userContext = userContext;
        this.savedSearchService = savedSearchService;
        this.candidateNoteService = candidateNoteService;
        this.surveyTypeRepository = surveyTypeRepository;
        this.emailHelper = emailHelper;
        this.pdfHelper = pdfHelper;
    }

    @Override
    public Page<Candidate> getSavedListCandidates(long id, SavedListGetRequest request) {
        Page<Candidate> candidatesPage = candidateRepository.findAll(
                new GetSavedListCandidatesQuery(id, request), request.getPageRequestWithoutSort());
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
            candidate.removeSavedLists(savedLists);

            saveIt(candidate);
        }
        return done;
    }

    @Override
    public boolean replaceCandidateSavedLists(long candidateId, IHasSetOfSavedLists request) {
        Candidate candidate = candidateRepository.findByIdLoadSavedLists(candidateId);

        boolean done = true;
        if (candidate == null) {
            done = false;
        } else {
            Set<SavedList> savedLists = fetchSavedLists(request);
            candidate.setSavedLists(savedLists);

            saveIt(candidate);
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
        candidate.setAuditFields(userContext.getLoggedInUser());
        candidateRepository.save(candidate);
    }

    //todo this is horrible cloned code duplicated from SavedSearchServiceImpl - factor it out.
    private SearchCandidateRequest convertToSearchCandidateRequest(SavedSearch savedSearch) {
        SearchCandidateRequest searchCandidateRequest = new SearchCandidateRequest();
        searchCandidateRequest.setSavedSearchId(savedSearch.getId());
        searchCandidateRequest.setKeyword(savedSearch.getKeyword());
        searchCandidateRequest.setStatuses(getStatusListFromString(savedSearch.getStatuses()));
        searchCandidateRequest.setGender(savedSearch.getGender());
        searchCandidateRequest.setOccupationIds(getIdsFromString(savedSearch.getOccupationIds()));
        searchCandidateRequest.setOrProfileKeyword(savedSearch.getOrProfileKeyword());
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
        searchCandidateRequest.setUnRegistered(savedSearch.getUnRegistered());
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

    private Page<Candidate> doSearchCandidates(SearchCandidateRequest request) {

        Page<Candidate> candidates;
        String simpleQueryString = request.getSimpleQueryString();
        if (simpleQueryString != null) {
            //This is an elastic search request
            
            //Modify the search to escape out any "
            final String replacement = "\\\\\\\\\"";
            simpleQueryString = 
            simpleQueryString.replaceAll("\"", replacement);
            
            //todo Need to support sorting 
            PageRequest req = CandidateEs.getAdjustedPagedSearchRequest(request);
            Page<CandidateEs> candidateProxies = candidateEsRepository
                    .simpleQueryString(simpleQueryString, req);
            //Get candidate ids from the returned results - maintaining the sort
            //Avoid duplicates, but maintaining order by using a LinkedHashSet
            LinkedHashSet<Long> candidateIds = new LinkedHashSet<>();
            for (CandidateEs candidateProxy : candidateProxies) {
                candidateIds.add(candidateProxy.getMasterId());
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
                    candidateProxies.getTotalElements());  
        } else {

            User user = userContext.getLoggedInUser();

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

            candidates = candidateRepository.findAll(query, request.getPageRequestWithoutSort());
        }
        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    private void addInSelections(@Nullable Long savedSearchId, Page<Candidate> candidates) {
        if (savedSearchId != null) {
            //Check for selection list to set the selected attribute on returned 
            // candidates.
            SavedList selectionList = null;
            User user = userContext.getLoggedInUser();
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
        addInSelections(savedSearchId, candidates);

        return candidates;
    }

    @Override
    public Page<Candidate> searchCandidates(SearchCandidateRequest request) {
        Page<Candidate> candidates;
        User user = userContext.getLoggedInUser();
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
            addInSelections(savedSearchId, candidates);
        }
        
        return candidates;
    }

    @Override
    public Page<Candidate> searchCandidates(CandidateEmailSearchRequest request) {
        String s = request.getCandidateEmail();
        User loggedInUser = userContext.getLoggedInUser();
        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Page<Candidate> candidates;

        candidates = candidateRepository.searchCandidateEmail(
                    '%' + s +'%', sourceCountries, request.getPageRequestWithoutSort());

        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    @Override
    public Page<Candidate> searchCandidates(CandidateNumberOrNameSearchRequest request) {
        String s = request.getCandidateNumberOrName();
        User loggedInUser = userContext.getLoggedInUser();
        boolean searchForNumber = s.length() > 0 && Character.isDigit(s.charAt(0));
        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Page<Candidate> candidates;

        if (searchForNumber) {
            candidates = candidateRepository.searchCandidateNumber(
                        s +'%', sourceCountries,
                    request.getPageRequestWithoutSort());
        } else {
            candidates = candidateRepository.searchCandidateName(
                        '%' + s +'%', sourceCountries,
                    request.getPageRequestWithoutSort());
        }

        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    @Override
    public Page<Candidate> searchCandidates(CandidatePhoneSearchRequest request) {
        String s = request.getCandidatePhone();
        User loggedInUser = userContext.getLoggedInUser();
        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Page<Candidate> candidates;

        candidates = candidateRepository.searchCandidatePhone(
                    '%' + s +'%', sourceCountries, request.getPageRequestWithoutSort());

        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    Specification<Candidate> addQuery(Specification<Candidate> query, SearchJoinRequest searchJoinRequest, List<Long> savedSearchIds) {
        if (savedSearchIds.contains(searchJoinRequest.getSavedSearchId())) {
            throw new CircularReferencedException(searchJoinRequest.getSavedSearchId());
        }
        User user = userContext.getLoggedInUser();
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
    public Candidate getCandidate(long id) throws NoSuchObjectException {
        return this.candidateRepository.findById(id)
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

        candidate = this.candidateRepository.save(candidate);

        //Create the corresponding CandidateEs.
        CandidateEs ces = new CandidateEs(candidate);
        ces = candidateEsRepository.save(ces);
        
        //Update textSearchId on candidate.
        String textSearchId = ces.getId();
        candidate.setTextSearchId(textSearchId);

        String candidateNumber = String.format("%04d", candidate.getId());
        candidate.setCandidateNumber(candidateNumber);
        candidate = this.candidateRepository.save(candidate);

        return candidate;
    }

    @Override
    @Transactional
    public Candidate updateCandidateStatus(long id, UpdateCandidateStatusRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
        CandidateStatus originalStatus = candidate.getStatus();
        candidate.setStatus(request.getStatus());
        candidate.setCandidateMessage(request.getCandidateMessage());
        candidate = candidateRepository.save(candidate);
        if (!request.getStatus().equals(originalStatus)){
            candidateNoteService.createCandidateNote(new CreateCandidateNoteRequest(id, "Status change from " + originalStatus + " to " + request.getStatus(), request.getComment()));
            if (request.getStatus().equals(CandidateStatus.incomplete)) {
                emailHelper.sendIncompleteApplication(candidate.getUser(), request.getCandidateMessage());
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
        User loggedInUser = userContext.getLoggedInUser();
        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
        candidate.setSflink(request.getSflink());
        candidate.setFolderlink(request.getFolderlink());
        candidate.setVideolink(request.getVideolink());
        candidate = candidateRepository.save(candidate);
        return candidate;
    }

    @Override
    public Candidate updateCandidate(long id, UpdateCandidateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
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
        candidate.setUnRegistered(request.getUnRegistered());
        candidate.setUnRegistrationNumber(request.getUnRegistrationNumber());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateCandidateAdditionalInfo(long id, UpdateCandidateAdditionalInfoRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id, sourceCountries)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));

        candidate.setAdditionalInfo(request.getAdditionalInfo());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateCandidateSurvey(long id, UpdateCandidateSurveyRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
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
        return candidateRepository.save(candidate);
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
        User user = userContext.getLoggedInUser();
        // Check update request for a duplicate email or phone number
        validateContactRequest(user, request);

        user.setEmail(request.getEmail());
        user = userRepository.save(user);
        Candidate candidate = user.getCandidate();
        candidate.setPhone(request.getPhone());
        candidate.setWhatsapp(request.getWhatsapp());
        candidate.setAuditFields(user);
        candidate = candidateRepository.save(candidate);
        candidate.setUser(user);
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

        User user = userContext.getLoggedInUser();
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
            candidate.setUnRegistered(request.getRegisteredWithUN());
            candidate.setUnRegistrationNumber(request.getRegistrationId());
            candidate.setAuditFields(user);
        }
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateEducation(UpdateCandidateEducationRequest request) {
        Candidate candidate = getLoggedInCandidate();

        EducationLevel educationLevel = null;
        if (request.getMaxEducationLevelId() != null) {
            // Load the education level from the database - throw an exception if not found
            educationLevel = educationLevelRepository.findById(request.getMaxEducationLevelId())
                    .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, request.getMaxEducationLevelId()));
        }

        candidate.setMaxEducationLevel(educationLevel);
        candidate.setAuditFields(candidate.getUser());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateCandidateSurvey(UpdateCandidateSurveyRequest request) {
        Candidate candidate = getLoggedInCandidate();

        SurveyType surveyType = null;
        if (request.getSurveyTypeId() != null) {
            // Load the education level from the database - throw an exception if not found
            surveyType = surveyTypeRepository.findById(request.getSurveyTypeId())
                    .orElseThrow(() -> new NoSuchObjectException(EducationLevel.class, request.getSurveyTypeId()));
        }
        candidate.setSurveyType(surveyType);
        candidate.setSurveyComment(request.getSurveyComment());

        candidate.setAuditFields(candidate.getUser());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateAdditionalInfo(UpdateCandidateAdditionalInfoRequest request) {
        Candidate candidate = getLoggedInCandidate();
        candidate.setAdditionalInfo(request.getAdditionalInfo());
        if (BooleanUtils.isTrue(request.getSubmit()) && !candidate.getStatus().equals(CandidateStatus.pending)) {
            updateCandidateStatus(candidate.getId(), new UpdateCandidateStatusRequest(CandidateStatus.pending, "Candidate submitted"));

            emailHelper.sendRegistrationEmail(candidate.getUser());
        }
        candidate.setAuditFields(candidate.getUser());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate getLoggedInCandidateLoadCandidateOccupations() {
        Candidate candidate = getLoggedInCandidate();
        candidate = candidateRepository.findByIdLoadCandidateOccupations(candidate.getId());
        return candidate;
    }

    @Override
    public Candidate getLoggedInCandidateLoadEducations() {
        Candidate candidate = getLoggedInCandidate();
        candidate = candidateRepository.findByIdLoadEducations(candidate.getId());
        return candidate;
    }

    @Override
    public Candidate getLoggedInCandidateLoadJobExperiences() {
        Candidate candidate = getLoggedInCandidate();
        candidate = candidateRepository.findByIdLoadJobExperiences(candidate.getId());
        return candidate;
    }

    @Override
    public Candidate getLoggedInCandidateLoadCertifications() {
        Candidate candidate = getLoggedInCandidate();
        candidate = candidateRepository.findByIdLoadCertifications(candidate.getId());
        return candidate;
    }

    @Override
    public Candidate getLoggedInCandidateLoadCandidateLanguages() {
        Candidate candidate = getLoggedInCandidate();
        candidate = candidateRepository.findByIdLoadCandidateLanguages(candidate.getId());
        return candidate;
    }

    @Override
    public Candidate getLoggedInCandidate() {
        User user = userContext.getLoggedInUser();
        return candidateRepository.findByUserId(user.getId());
    }

    @Override
    public Candidate getLoggedInCandidateLoadProfile() {
        User user = userContext.getLoggedInUser();
        return candidateRepository.findByUserIdLoadProfile(user.getId());
    }

    @Override
    public Candidate findByCandidateNumber(String candidateNumber) {
        User loggedInUser = userContext.getLoggedInUser();
        Set<Country> sourceCountries = getDefaultSourceCountries(loggedInUser);
        return candidateRepository.findByCandidateNumberRestricted(candidateNumber, sourceCountries)
                .orElseThrow(() -> new CountryRestrictionException("You don't have access to this candidate."));
    }

    @Transactional(readOnly = true)
    void validateContactRequest(User user, BaseCandidateContactRequest request) {
        Candidate candidate = null;
        if (user != null) {
            candidate = user.getCandidate();
        }

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
    public List<DataRow> getGenderStats(CandidateStatDateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        List<Long> sourceCountryIds = getDefaultSourceCountryIds(loggedInUser);
        CandidateStatDateRequest requestWithDefaults = convertDateRangeDefaults(request);
        return toRows(candidateRepository.countByGenderOrderByCount(
                sourceCountryIds,
                requestWithDefaults.getDateFrom(),
                requestWithDefaults.getDateTo()));
    }

    @Override
    public List<DataRow> getBirthYearStats(Gender gender, CandidateStatDateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        List<Long> sourceCountryIds = getDefaultSourceCountryIds(loggedInUser);
        CandidateStatDateRequest requestWithDefaults = convertDateRangeDefaults(request);
        return toRows(candidateRepository.
                countByBirthYearOrderByYear(
                        genderStr(gender),
                        sourceCountryIds,
                        requestWithDefaults.getDateFrom(),
                        requestWithDefaults.getDateTo()));
    }

    @Override
    public List<DataRow> getRegistrationStats(CandidateStatDateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        List<Long> sourceCountryIds = getDefaultSourceCountryIds(loggedInUser);
        CandidateStatDateRequest requestWithDefaults = convertDateRangeDefaults(request);
        return toRows(candidateRepository.countByCreatedDateOrderByCount(
                sourceCountryIds,
                requestWithDefaults.getDateFrom(),
                requestWithDefaults.getDateTo()));
    }

    @Override
    public List<DataRow> getRegistrationOccupationStats(CandidateStatDateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        List<Long> sourceCountryIds = getDefaultSourceCountryIds(loggedInUser);
        CandidateStatDateRequest requestWithDefaults = convertDateRangeDefaults(request);
        final List<DataRow> rows = toRows(candidateRepository.countByOccupationOrderByCount(
                sourceCountryIds,
                requestWithDefaults.getDateFrom(),
                requestWithDefaults.getDateTo()));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> getNationalityStats(Gender gender, String country, CandidateStatDateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        List<Long> sourceCountryIds = getDefaultSourceCountryIds(loggedInUser);
        CandidateStatDateRequest requestWithDefaults = convertDateRangeDefaults(request);
        List<DataRow> rows = toRows(candidateRepository.
                countByNationalityOrderByCount(
                        genderStr(gender),
                        countryStr(country),
                        sourceCountryIds,
                        requestWithDefaults.getDateFrom(),
                        requestWithDefaults.getDateTo()));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> getSurveyStats(Gender gender, String country, CandidateStatDateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        List<Long> sourceCountryIds = getDefaultSourceCountryIds(loggedInUser);
        CandidateStatDateRequest requestWithDefaults = convertDateRangeDefaults(request);
        return toRows(candidateRepository.
                countBySurveyOrderByCount(
                        genderStr(gender),
                        countryStr(country),
                        sourceCountryIds,
                        requestWithDefaults.getDateFrom(),
                        requestWithDefaults.getDateTo()));
    }

    @Override
    public List<DataRow> getMaxEducationStats(Gender gender, CandidateStatDateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        List<Long> sourceCountryIds = getDefaultSourceCountryIds(loggedInUser);
        CandidateStatDateRequest requestWithDefaults = convertDateRangeDefaults(request);
        return toRows(candidateRepository.
                countByMaxEducationLevelOrderByCount(
                        genderStr(gender),
                        sourceCountryIds,
                        requestWithDefaults.getDateFrom(),
                        requestWithDefaults.getDateTo()));
    }

    @Override
    public List<DataRow> getLanguageStats(Gender gender, CandidateStatDateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        List<Long> sourceCountryIds = getDefaultSourceCountryIds(loggedInUser);
        CandidateStatDateRequest requestWithDefaults = convertDateRangeDefaults(request);
        List<DataRow> rows = toRows(candidateRepository.
                countByLanguageOrderByCount(
                        genderStr(gender),
                        sourceCountryIds,
                        requestWithDefaults.getDateFrom(),
                        requestWithDefaults.getDateTo()));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> getOccupationStats(Gender gender, CandidateStatDateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        List<Long> sourceCountryIds = getDefaultSourceCountryIds(loggedInUser);
        CandidateStatDateRequest requestWithDefaults = convertDateRangeDefaults(request);
        return toRows(candidateRepository.
                countByOccupationOrderByCount(
                        genderStr(gender),
                        sourceCountryIds,
                        requestWithDefaults.getDateFrom(),
                        requestWithDefaults.getDateTo()));
    }

    @Override
    public List<DataRow> getMostCommonOccupationStats(Gender gender, CandidateStatDateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        List<Long> sourceCountryIds = getDefaultSourceCountryIds(loggedInUser);
        CandidateStatDateRequest requestWithDefaults = convertDateRangeDefaults(request);
        List<DataRow> rows = toRows(candidateRepository.
                countByMostCommonOccupationOrderByCount(
                        genderStr(gender),
                        sourceCountryIds,
                        requestWithDefaults.getDateFrom(),
                        requestWithDefaults.getDateTo()));
        return limitRows(rows, 15);
    }

    @Override
    public List<DataRow> getSpokenLanguageLevelStats(Gender gender, String language, CandidateStatDateRequest request) {
        User loggedInUser = userContext.getLoggedInUser();
        List<Long> sourceCountryIds = getDefaultSourceCountryIds(loggedInUser);
        CandidateStatDateRequest requestWithDefaults = convertDateRangeDefaults(request);
        return toRows(candidateRepository.
                countBySpokenLanguageLevelByCount(
                        genderStr(gender),
                        language,
                        sourceCountryIds,
                        requestWithDefaults.getDateFrom(),
                        requestWithDefaults.getDateTo()));
    }

    @Override
    public Resource generateCv(Candidate candidate) {
       return pdfHelper.generatePdf(candidate);
    }

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
                    csvWriter.writeNext(getExportCandidateStrings(candidate));
                }

                if (result.getNumber() * request.getPageSize() < result.getTotalElements()) {
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
                for (Candidate candidate : result.getContent()) {
                    csvWriter.writeNext(getExportCandidateStrings(candidate));
                }

                if (result.getNumber() * request.getPageSize() < result.getTotalElements()) {
                    request.setPageNumber(request.getPageNumber()+1);
                } else {
                    hasMore = false;
                }
            }
        } catch (IOException e) {
            throw new ExportFailedException( e);
        }
    }

    private String[] getExportTitles() {
        return new String[]{
                "Candidate Number", "Candidate First Name", "Candidate Last Name", "Gender", "Country Residing", "Nationality",
                "Dob", "Email", "Max Education Level", "Education Major", "English Spoken Level", "Occupation", "Link"
        };
    }

    private String[] getExportCandidateStrings(Candidate candidate) {
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
                getCandidateExternalHref(candidate.getCandidateNumber())
        };
    }

    private String getCandidateExternalHref(String candidateNumber) {
        return "https://www.tbbtalent.org/admin-portal/candidate/" + candidateNumber;
    }

    public String formatCandidateMajor(List<CandidateEducation> candidateEducations){
        StringBuffer buffer = new StringBuffer();
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
        StringBuffer buffer = new StringBuffer();
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
        StringBuffer buffer = new StringBuffer();
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
    @Override
    @Scheduled(cron = "0 0 0 * * ?", zone = "GMT")
    public void notifyWatchers() {
        try {
            Set<SavedSearch> searches = savedSearchRepository.findByWatcherIdsIsNotNullLoadSearchJoins();
            Map<Long, Set<SavedSearch>> userNotifications = new HashMap<>();
            for (SavedSearch savedSearch : searches) {
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
            String mess = "Watcher notification failure";
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

    /**
     * Get a user’s source country Ids, defaulting to all countries if empty
     */
    public List<Long> getDefaultSourceCountryIds(User user){
        List<Long> listOfCountryIds;

        if(CollectionUtils.isEmpty(user.getSourceCountries())){
            listOfCountryIds = countryRepository.findAll().stream()
                    .map(Country::getId)
                    .collect(Collectors.toList());
        } else {
            listOfCountryIds = user.getSourceCountries().stream()
                    .map(Country::getId)
                    .collect(Collectors.toList());
        }

        return listOfCountryIds;
    }

    /**
     * Convert null string to date default.
     */
    public CandidateStatDateRequest convertDateRangeDefaults(CandidateStatDateRequest request){
        if (request.getDateFrom() == null) {
            request.setDateFrom(LocalDate.parse("2000-01-01"));
        }

        if(request.getDateTo() == null) {
            request.setDateTo(LocalDate.now());
        }
        return request;
    }
    
}
