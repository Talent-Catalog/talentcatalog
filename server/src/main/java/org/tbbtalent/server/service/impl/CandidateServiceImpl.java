package org.tbbtalent.server.service.impl;

import com.opencsv.CSVWriter;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.CircularReferencedException;
import org.tbbtalent.server.exception.ExportFailedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.PasswordMatchException;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.model.CandidateOccupation;
import org.tbbtalent.server.model.CandidateStatus;
import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.model.DataRow;
import org.tbbtalent.server.model.EducationLevel;
import org.tbbtalent.server.model.Gender;
import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.SavedSearch;
import org.tbbtalent.server.model.SearchJoin;
import org.tbbtalent.server.model.SearchType;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.model.SurveyType;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.repository.CandidateRepository;
import org.tbbtalent.server.repository.CandidateSpecification;
import org.tbbtalent.server.repository.CountryRepository;
import org.tbbtalent.server.repository.EducationLevelRepository;
import org.tbbtalent.server.repository.NationalityRepository;
import org.tbbtalent.server.repository.SavedSearchRepository;
import org.tbbtalent.server.repository.SurveyTypeRepository;
import org.tbbtalent.server.repository.UserRepository;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.BaseCandidateContactRequest;
import org.tbbtalent.server.request.candidate.CandidateEmailSearchRequest;
import org.tbbtalent.server.request.candidate.CandidateNumberOrNameSearchRequest;
import org.tbbtalent.server.request.candidate.CandidatePhoneSearchRequest;
import org.tbbtalent.server.request.candidate.CreateCandidateRequest;
import org.tbbtalent.server.request.candidate.RegisterCandidateRequest;
import org.tbbtalent.server.request.candidate.SavedSearchRunRequest;
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
import org.tbbtalent.server.request.note.CreateCandidateNoteRequest;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateNoteService;
import org.tbbtalent.server.service.CandidateService;
import org.tbbtalent.server.service.SavedSearchService;
import org.tbbtalent.server.service.email.EmailHelper;
import org.tbbtalent.server.service.pdf.PdfHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.rmi.server.ExportException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CandidateServiceImpl implements CandidateService {

    private static final Logger log = LoggerFactory.getLogger(CandidateServiceImpl.class);

    private final UserRepository userRepository;
    private final SavedSearchRepository savedSearchRepository;
    private final CandidateRepository candidateRepository;
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
                                SavedSearchRepository savedSearchRepository,
                                CandidateRepository candidateRepository,
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
        this.savedSearchRepository = savedSearchRepository;
        this.candidateRepository = candidateRepository;
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
    public Page<Candidate> searchCandidates(SavedSearchRunRequest request) {
        
        Long savedSearchId = request.getSavedSearchId(); 
        SavedSearch savedSearch = this.savedSearchRepository.findByIdLoadSearchJoins(savedSearchId)
                .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, savedSearchId));
        SearchCandidateRequest searchCandidateRequest = convertToSearchCandidateRequest(savedSearch);
        
        searchCandidateRequest.setPageNumber(request.getPageNumber());
        searchCandidateRequest.setPageSize(request.getPageSize());
        searchCandidateRequest.setSortDirection(request.getSortDirection());
        searchCandidateRequest.setSortFields(request.getSortFields());
        searchCandidateRequest.setShortlistStatus(request.getShortlistStatus());
        
        return searchCandidates(searchCandidateRequest);
    }

    //todo this is horrible cloned code duplicated from SavedSearchServiceImpl - factor it out.
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
        searchCandidateRequest.setUnRegistered(request.getUnRegistered());
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

    @Override
    public Page<Candidate> searchCandidates(SearchCandidateRequest request) {
        List<Long> searchIds = new ArrayList<>();
        if (request.getSavedSearchId() != null) {
            searchIds.add(request.getSavedSearchId());
        }

        Specification<Candidate> query = CandidateSpecification.buildSearchQuery(request);
        if (CollectionUtils.isNotEmpty(request.getSearchJoinRequests())) {
            for (SearchJoinRequest searchJoinRequest : request.getSearchJoinRequests()) {
                query = addQuery(query, searchJoinRequest, searchIds);
            }
        }

        Page<Candidate> candidates = candidateRepository.findAll(query, request.getPageRequestWithoutSort());
        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    @Override
    public Page<Candidate> searchCandidates(CandidateEmailSearchRequest request) {
        String s = request.getCandidateEmail();

        Page<Candidate> candidates;
        candidates = candidateRepository.searchCandidateEmail(
                '%' + s +'%', request.getPageRequestWithoutSort());
        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    @Override
    public Page<Candidate> searchCandidates(CandidateNumberOrNameSearchRequest request) {
        String s = request.getCandidateNumberOrName();
        boolean searchForNumber = s.length() > 0 && Character.isDigit(s.charAt(0));

        Page<Candidate> candidates;
        if (searchForNumber) {
            candidates = candidateRepository.searchCandidateNumber(
                    s +'%', request.getPageRequestWithoutSort());
        } else {
            candidates = candidateRepository.searchCandidateName(
                    '%' + s +'%', request.getPageRequestWithoutSort());
            
        }
        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    @Override
    public Page<Candidate> searchCandidates(CandidatePhoneSearchRequest request) {
        String s = request.getCandidatePhone();

        Page<Candidate> candidates;
            candidates = candidateRepository.searchCandidatePhone(
                    '%' + s +'%', request.getPageRequestWithoutSort());
        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    Specification<Candidate> addQuery(Specification<Candidate> query, SearchJoinRequest searchJoinRequest, List<Long> savedSearchIds) {
        if (savedSearchIds.contains(searchJoinRequest.getSavedSearchId())) {
            throw new CircularReferencedException(searchJoinRequest.getSavedSearchId());
        }
        //add id to list as do not want circular references
        savedSearchIds.add(searchJoinRequest.getSavedSearchId());
        //load saved search
        SearchCandidateRequest request = savedSearchService.loadSavedSearch(searchJoinRequest.getSavedSearchId());
        Specification<Candidate> joinQuery = CandidateSpecification.buildSearchQuery(request);
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
    public Candidate getCandidate(long id) {
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



        String candidateNumber = String.format("%04d", candidate.getId());
        candidate.setCandidateNumber(candidateNumber);
        candidate = this.candidateRepository.save(candidate);

        return candidate;
    }

    @Override
    @Transactional
    public Candidate updateCandidateStatus(long id, UpdateCandidateStatusRequest request) {
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id)
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
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
        candidate.setSflink(request.getSflink());
        candidate.setFolderlink(request.getFolderlink());
        candidate.setVideolink(request.getVideolink());
        candidate = candidateRepository.save(candidate);
        return candidate;
    }

    @Override
    public Candidate updateCandidate(long id, UpdateCandidateRequest request) {
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id)
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
    @Transactional
    public boolean deleteCandidate(long id) {
        Candidate candidate = candidateRepository.findById(id).orElse(null);
        if (candidate != null) {
            candidateRepository.delete(candidate);
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
        return candidateRepository.findByCandidateNumber(candidateNumber);
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
    public List<DataRow> getGenderStats() {
        return toRows(candidateRepository.countByGenderOrderByCount()); 
    }

    @Override
    public List<DataRow> getBirthYearStats(Gender gender) {
        return toRows(candidateRepository.
                countByBirthYearOrderByYear(genderStr(gender))); 
    }

    @Override
    public List<DataRow> getNationalityStats(Gender gender) {
        List<DataRow> rows = toRows(candidateRepository.
                countByNationalityOrderByCount(genderStr(gender))); 
        return limitRows(rows, 15);
    }
    
    @Override
    public List<DataRow> getMaxEducationStats(Gender gender) {
        return toRows(candidateRepository.
                countByMaxEducationLevelOrderByCount(genderStr(gender)));
    }
    
    @Override
    public List<DataRow> getLanguageStats(Gender gender) {
        List<DataRow> rows = toRows(candidateRepository.
                countByLanguageOrderByCount(genderStr(gender)));
        return limitRows(rows, 15);
    }
    
    @Override
    public List<DataRow> getOccupationStats(Gender gender) {
        return toRows(candidateRepository.
                countByOccupationOrderByCount(genderStr(gender)));
    }
    
    @Override
    public List<DataRow> getMainOccupationStats(Gender gender) {
        List<DataRow> rows = toRows(candidateRepository.
                countByMainOccupationOrderByCount(genderStr(gender)));
        return limitRows(rows, 15);
    }
    
    @Override
    public List<DataRow> getSpokenLanguageLevelStats(Gender gender, String language) {
        return toRows(candidateRepository.
                countBySpokenLanguageLevelByCount(genderStr(gender), language));
    }

    @Override
    public Resource generateCv(Candidate candidate) {
       return pdfHelper.generatePdf(candidate);
    }

    @Override
    public void exportToCsv(SavedSearchRunRequest request, PrintWriter writer) throws ExportException {
        Long savedSearchId = request.getSavedSearchId();
        SavedSearch savedSearch = this.savedSearchRepository.findByIdLoadSearchJoins(savedSearchId)
                .orElseThrow(() -> new NoSuchObjectException(SavedSearch.class, savedSearchId));
        SearchCandidateRequest searchCandidateRequest = convertToSearchCandidateRequest(savedSearch);

        searchCandidateRequest.setPageNumber(request.getPageNumber());
        searchCandidateRequest.setPageSize(request.getPageSize());
        searchCandidateRequest.setSortDirection(request.getSortDirection());
        searchCandidateRequest.setSortFields(request.getSortFields());
        searchCandidateRequest.setShortlistStatus(request.getShortlistStatus());
        exportToCsv(searchCandidateRequest, writer);
    }

    @Override
    public void exportToCsv(SearchCandidateRequest request, PrintWriter writer) {
        try (CSVWriter csvWriter = new CSVWriter(writer)) {

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-mm-yyyy");
            csvWriter.writeNext(new String[] {
                    "Candidate Number", "Candidate First Name", "Candidate Last Name","Gender", "Country Residing", "Nationality",
                    "Dob","Max Education Level", "Education Major", "English Spoken Level", "Occupation"
            });

            request.setPageNumber(0);
            request.setPageSize(500);
            boolean hasMore = true;
            while (hasMore) {
                Page<Candidate> result = this.searchCandidates(request);

                for (Candidate candidate : result.getContent()) {
                    csvWriter.writeNext(new String[] {
                            candidate.getCandidateNumber(),
                            candidate.getUser().getFirstName(),
                            candidate.getUser().getLastName(),
                            candidate.getGender() != null ? candidate.getGender().toString() : null,
                            candidate.getCountry() != null ? candidate.getCountry().getName() : candidate.getMigrationCountry(),
                            candidate.getNationality() != null ? candidate.getNationality().getName() : null,
                            candidate.getDob() != null ? candidate.getDob().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) : null,
                            candidate.getMaxEducationLevel() != null ? candidate.getMaxEducationLevel().getName() : null,
                            formatCandidateMajor(candidate.getCandidateEducations()),
                            getEnglishSpokenProficiency(candidate.getCandidateLanguages()),
                            formatCandidateOccupation(candidate.getCandidateOccupations())

                    });
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
                if ("english".equalsIgnoreCase(candidateLanguage.getLanguage().getName())){
                    buffer.append(candidateLanguage.getSpokenLevel().getName()).append("\n");
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



}
