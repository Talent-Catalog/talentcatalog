package org.tbbtalent.server.service.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.*;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.repository.*;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.*;
import org.tbbtalent.server.request.note.CreateCandidateNoteRequest;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateNoteService;
import org.tbbtalent.server.service.CandidateService;
import org.tbbtalent.server.service.SavedSearchService;
import org.tbbtalent.server.service.email.EmailHelper;

import javax.security.auth.login.AccountLockedException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CandidateServiceImpl implements CandidateService {

    private static final Logger log = LoggerFactory.getLogger(CandidateServiceImpl.class);

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final CountryRepository countryRepository;
    private final EducationLevelRepository educationLevelRepository;
    private final NationalityRepository nationalityRepository;
    private final PasswordHelper passwordHelper;
    private final UserContext userContext;
    private final SavedSearchService savedSearchService;
    private final CandidateNoteService candidateNoteService;
    private final EmailHelper emailHelper; 

    @Autowired
    public CandidateServiceImpl(UserRepository userRepository,
                                CandidateRepository candidateRepository,
                                CountryRepository countryRepository,
                                EducationLevelRepository educationLevelRepository,
                                NationalityRepository nationalityRepository,
                                PasswordHelper passwordHelper,
                                UserContext userContext,
                                SavedSearchService savedSearchService,
                                CandidateNoteService candidateNoteService,
                                EmailHelper emailHelper) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
        this.educationLevelRepository = educationLevelRepository;
        this.nationalityRepository = nationalityRepository;
        this.passwordHelper = passwordHelper;
        this.userContext = userContext;
        this.savedSearchService = savedSearchService;
        this.candidateNoteService = candidateNoteService;
        this.emailHelper = emailHelper;
    }

    @Override
    public Page<Candidate> searchCandidates(SearchCandidateRequest request) {
        List<Long> searchIds = new ArrayList<>();
        if (request.getSavedSearchId() != null) {
            searchIds.add(request.getSavedSearchId());
        }

        Specification<Candidate> query = CandidateSpecification.buildSearchQuery(request);
        if (!request.getSearchJoinRequests().isEmpty()) {
            for (SearchJoinRequest searchJoinRequest : request.getSearchJoinRequests()) {
                query = addQuery(query, searchJoinRequest, searchIds);
            }
        }

        Page<Candidate> candidates = candidateRepository.findAll(query, request.getPageRequest());
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
        candidate = this.candidateRepository.save(candidate);

        String candidateNumber = String.format("CN%04d", candidate.getId());
        candidate.setCandidateNumber(candidateNumber);
        candidate = this.candidateRepository.save(candidate);

        return candidate;
    }

    @Override
    @Transactional
    public Candidate updateCandidateStatus(long id, UpdateCandidateStatusRequest request) {
        Candidate candidate = this.candidateRepository.findByIdLoadUser(id)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
        if (!request.getStatus().equals(candidate.getStatus())){
            candidateNoteService.createCandidateNote(new CreateCandidateNoteRequest(id, "Status change from "+candidate.getStatus()+" to "+request.getStatus(), request.getComment()));
            candidate.setStatus(request.getStatus());
            candidate.setCandidateMessage(request.getCandidateMessage());
            candidate =  candidateRepository.save(candidate);
            if (request.getStatus().equals(CandidateStatus.incomplete)){
                emailHelper.sendIncompleteApplication(candidate.getUser(), request.getCandidateMessage());
            }
        }
        return candidate;
    }

    @Override
    public Candidate updateCandidate(long id, UpdateCandidateRequest request) {

        Candidate candidate = this.candidateRepository.findByIdLoadUser(id)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        // Load the country from the database - throw an exception if not found
        Nationality nationality = nationalityRepository.findById(request.getNationalityId())
                .orElseThrow(() -> new NoSuchObjectException(Nationality.class, request.getNationalityId()));

        User user = candidate.getUser();
        //check email not already taken
        if (!StringUtils.isBlank(request.getPhone())) {
            User exists = userRepository.findByEmailIgnoreCase(request.getEmail());
            if (exists != null && !exists.getId().equals(user.getId())) {
                throw new UsernameTakenException("email");
            }
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            userRepository.save(user);
        }

        //check phone not already taken
        if (!StringUtils.isBlank(request.getPhone())) {
            Candidate exists = candidateRepository.findByPhoneIgnoreCase(request.getEmail());
            if (exists != null && !exists.getId().equals(id)) {
                throw new UsernameTakenException("phone");
            }
        }

        //check whatsapp not already taken
        if (!StringUtils.isBlank(request.getWhatsapp())) {
            Candidate exists = candidateRepository.findByWhatsappIgnoreCase(request.getWhatsapp());
            if (exists != null && !exists.getId().equals(id)) {
                throw new UsernameTakenException("whatsapp");
            }
        }
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
    public LoginRequest register(RegisterCandidateRequest request) throws AccountLockedException {
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new PasswordMatchException();
        }

        /* Check for existing account with the username fields */
        if (StringUtils.isNotBlank(request.getUsername())) {
            User exists = userRepository.findByUsernameAndRole(request.getUsername(), Role.user);
            if (exists != null) {
                throw new UsernameTakenException("username");
            }
        }
        if (StringUtils.isNotBlank(request.getEmail())) {
            User exists = userRepository.findByEmailIgnoreCase(request.getEmail());
            if (exists != null) {
                throw new UsernameTakenException("email");
            }
        }
        if (StringUtils.isNotBlank(request.getPhone())) {
            Candidate exists = candidateRepository.findByPhoneIgnoreCase(request.getPhone());
            if (exists != null) {
                throw new UsernameTakenException("phone");
            }
        }
        if (StringUtils.isNotBlank(request.getWhatsapp())) {
            Candidate exists = candidateRepository.findByWhatsappIgnoreCase(request.getWhatsapp());
            if (exists != null) {
                throw new UsernameTakenException("whatsapp");
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

}
