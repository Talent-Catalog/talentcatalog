package org.tbbtalent.server.service.impl;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.*;
import org.tbbtalent.server.model.*;
import org.tbbtalent.server.repository.*;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.*;
import org.tbbtalent.server.response.JwtAuthenticationResponse;
import org.tbbtalent.server.security.JwtTokenProvider;
import org.tbbtalent.server.security.PasswordHelper;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateService;

import javax.security.auth.login.AccountLockedException;

@Service
public class CandidateServiceImpl implements CandidateService {

    private static final Logger log = LoggerFactory.getLogger(CandidateServiceImpl.class);

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final CountryRepository countryRepository;
    private final NationalityRepository nationalityRepository;
    private final PasswordHelper passwordHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserContext userContext;

    @Autowired
    public CandidateServiceImpl(UserRepository userRepository, CandidateRepository candidateRepository,
                                CountryRepository countryRepository,
                                NationalityRepository nationalityRepository,
                                PasswordHelper passwordHelper,
                                AuthenticationManager authenticationManager,
                                JwtTokenProvider tokenProvider,
                                UserContext userContext) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
        this.nationalityRepository = nationalityRepository;
        this.passwordHelper = passwordHelper;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userContext = userContext;
    }

    @Override
    public Page<Candidate> searchCandidates(SearchCandidateRequest request) {
        Page<Candidate> candidates = candidateRepository.findAll(
                CandidateSpecification.buildSearchQuery(request), request.getPageRequest());
        log.info("Found " + candidates.getTotalElements() + " candidates in search");
        return candidates;
    }

    @Override
    public Candidate getCandidate(long id) {
        return this.candidateRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
    }

    @Override
    @Transactional
    public Candidate createCandidate(CreateCandidateRequest request) {
        User user = new User(
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                Role.user);

        user = this.userRepository.save(user);

        Candidate candidate = new Candidate(user, request.getPhone(), request.getWhatsapp(), user);
        candidate.setCandidateNumber("TEMP%04d"+RandomStringUtils.random(6));
        candidate = this.candidateRepository.save(candidate);

        String candidateNumber = String.format("CN%04d", candidate.getId());
        candidate.setCandidateNumber(candidateNumber);
        candidate = this.candidateRepository.save(candidate);

        return candidate;
    }

    @Override
    @Transactional
    public Candidate updateCandidate(long id, UpdateCandidateRequest request) {
        Candidate candidate = this.candidateRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
        candidate.setCandidateNumber(request.getCandidateNumber());
        candidate.getUser().setFirstName(request.getFirstName());
        candidate.getUser().setLastName(request.getLastName());
        candidate.getUser().setEmail(request.getEmail());
        candidate = this.candidateRepository.save(candidate);
        return candidate;
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
    public JwtAuthenticationResponse login(LoginRequest request) throws AccountLockedException {
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsername(), request.getPassword()
            ));
            User user = this.userRepository.findByUsernameIgnoreCase(request.getUsername());

            if (user.getStatus().equals(Status.inactive)) {
                throw new InvalidCredentialsException("Sorry, it looks like that account is no longer active.");
            }

            SecurityContextHolder.getContext().setAuthentication(auth);
            String jwt = tokenProvider.generateToken(auth);
            return new JwtAuthenticationResponse(jwt, user);

        } catch (BadCredentialsException e) {
            // map spring exception to a service exception for better handling
            throw new InvalidCredentialsException("Invalid credentials for candidate");
        } catch (LockedException e) {
            throw new AccountLockedException("Account locked");
        } catch (DisabledException e) {
            throw new CandidateDeactivatedException();
        } catch (CredentialsExpiredException e) {
            throw new PasswordExpiredException();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JwtAuthenticationResponse register(RegisterCandidateRequest request) throws AccountLockedException {
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new PasswordMatchException();
        }

        /* Check for existing account with the username fields */
        if (StringUtils.isNotBlank(request.getUsername())) {
            User exists = userRepository.findByUsernameIgnoreCase(request.getUsername());
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
        return login(loginRequest);
    }

    @Override
    public void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public User getLoggedInUser() {
        User user = userContext.getLoggedInUser();
        if (user == null) {
            throw new InvalidSessionException("Can not find an active session for a candidate with this token");
        }
        return user;
    }

    @Override
    public Candidate updateEmail(UpdateCandidateEmailRequest request) {
        User user = userContext.getLoggedInUser();
        user.setEmail(request.getEmail());
        user = userRepository.save(user);
        return user.getCandidate();
    }

    @Override
    public Candidate updateAlternateContacts(UpdateCandidateAlternateContactRequest request) {
        User user = userContext.getLoggedInUser();
        user.setEmail(request.getEmail());
        user = userRepository.save(user);
        Candidate candidate = candidateRepository.findByUserId(user.getId());
        if (candidate != null) {
            if (StringUtils.isBlank(request.getEmail()) && StringUtils.isBlank(request.getPhone())
                    && StringUtils.isBlank(request.getWhatsapp())) {
                throw new InvalidRequestException("You must specify at least one method of contact");
            }
            candidate.setPhone(request.getPhone());
            candidate.setWhatsapp(request.getWhatsapp());
        }
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateAdditionalContacts(UpdateCandidateAdditionalContactRequest request) {
        Candidate candidate = getLoggedInCandidate();
        candidate.setPhone(request.getPhone());
        candidate.setWhatsapp(request.getWhatsapp());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updatePersonal(UpdateCandidatePersonalRequest request) {
        User user = userContext.getLoggedInUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user = userRepository.save(user);
        Candidate candidate = candidateRepository.findByUserId(user.getId());
        if (candidate != null) {
            candidate.setGender(request.getGender());
            candidate.setDob(request.getDob());
        }
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateLocation(UpdateCandidateLocationRequest request) {

        // Load the country from the database - throw an exception if not found
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new NoSuchObjectException(Country.class, request.getCountryId()));

        Candidate candidate = getLoggedInCandidate();
        candidate.setCountry(country);
        candidate.setCity(request.getCity());
        candidate.setYearOfArrival(request.getYearOfArrival());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateNationality(UpdateCandidateNationalityRequest request) {

        // Load the nationality from the database - throw an exception if not found
        Nationality nationality = nationalityRepository.findById(request.getNationality())
                .orElseThrow(() -> new NoSuchObjectException(Nationality.class, request.getNationality()));

        Candidate candidate = getLoggedInCandidate();
        candidate.setNationality(nationality);
        candidate.setUnRegistered(request.getRegisteredWithUN());
        candidate.setUnRegistrationNumber(request.getRegistrationId());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateEducationLevel(UpdateCandidateEducationLevelRequest request) {
        Candidate candidate = getLoggedInCandidate();
        //candidate.setMaxEducationLevel(request.getEducationLevel()); todo
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateAdditionalInfo(UpdateCandidateAdditionalInfoRequest request) {
        Candidate candidate = getLoggedInCandidate();
        candidate.setAdditionalInfo(request.getAdditionalInfo());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate getLoggedInCandidateLoadProfessions() {
        Candidate candidate = getLoggedInCandidate();
        candidate = candidateRepository.findByIdLoadProfessions(candidate.getId());
        return candidate;
    }

    @Override
    public Candidate getLoggedInCandidateLoadEducations() {
        Candidate candidate = getLoggedInCandidate();
        candidate = candidateRepository.findByIdLoadEducations(candidate.getId());
        return candidate;
    }

    @Override
    public Candidate getLoggedInCandidateLoadWorkExperiences() {
        Candidate candidate = getLoggedInCandidate();
        candidate = candidateRepository.findByIdLoadWorkExperiences(candidate.getId());
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

}
