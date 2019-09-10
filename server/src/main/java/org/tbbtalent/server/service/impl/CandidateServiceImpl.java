package org.tbbtalent.server.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tbbtalent.server.exception.*;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.CandidateRepository;
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

    private final CandidateRepository candidateRepository;
    private final PasswordHelper passwordHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserContext userContext;

    @Autowired
    public CandidateServiceImpl(CandidateRepository candidateRepository,
                                PasswordHelper passwordHelper,
                                AuthenticationManager authenticationManager,
                                JwtTokenProvider tokenProvider,
                                UserContext userContext) {
        this.candidateRepository = candidateRepository;
        this.passwordHelper = passwordHelper;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userContext = userContext;
    }

    @Override
    public Page<Candidate> searchCandidates(SearchCandidateRequest request) {
        return this.candidateRepository.findAll(
                PageRequest.of(request.getPageNumber(), request.getPageSize()));
    }

    @Override
    public Candidate getCandidate(long id) {
        return this.candidateRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Candidate.class, id));
    }

    @Override
    @Transactional
    public Candidate createCandidate(CreateCandidateRequest request) {
        Candidate candidate = new Candidate(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                request.getWhatsapp());
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
        candidate.setFirstName(request.getFirstName());
        candidate.setLastName(request.getLastName());
        candidate.setEmail(request.getEmail());
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
            Candidate candidate = this.candidateRepository.findByAnyUserIdentityIgnoreCase(request.getUsername());

            if (candidate.getStatus().equals(Status.inactive)) {
                throw new InvalidCredentialsException("Sorry, it looks like that account is no longer active.");
            }

            SecurityContextHolder.getContext().setAuthentication(auth);
            String jwt = tokenProvider.generateToken(auth);
            return new JwtAuthenticationResponse(jwt, candidate);

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
        Candidate exists = null;
        if (StringUtils.isNotBlank(request.getEmail())) {
            exists = candidateRepository.findByEmailIgnoreCase(request.getEmail());
            if (exists != null) {
                throw new UsernameTakenException("email");
            }
        } else if (StringUtils.isNotBlank(request.getPhone())) {
            exists = candidateRepository.findByPhoneIgnoreCase(request.getPhone());
            if (exists != null) {
                throw new UsernameTakenException("phone");
            }
        } else if (StringUtils.isNotBlank(request.getWhatsapp())) {
            exists = candidateRepository.findByWhatsappIgnoreCase(request.getWhatsapp());
            if (exists != null) {
                throw new UsernameTakenException("whatsapp");
            }
        } else {
            throw new InvalidRequestException("Must specify at least one method of contact");
        }

        /* Validate the password before account creation */
        String passwordEncrypted = passwordHelper.validateAndEncodePassword(request.getPassword());

        /* Create the candidate */
        CreateCandidateRequest createCandidateRequest = new CreateCandidateRequest();
        createCandidateRequest.setEmail(request.getEmail());
        createCandidateRequest.setPhone(request.getPhone());
        createCandidateRequest.setWhatsapp(request.getWhatsapp());
        Candidate candidate = createCandidate(createCandidateRequest);

        /* Update the password */
        candidate.setPasswordEnc(passwordEncrypted);
        candidate = this.candidateRepository.save(candidate);

        /* Log the candidate in */
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(candidate.getUsername());
        loginRequest.setPassword(request.getPassword());
        return login(loginRequest);
    }

    @Override
    public void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Override
    public Candidate getLoggedInCandidate() {
        Candidate candidate = userContext.getLoggedInCandidate();
        if (candidate == null) {
            throw new InvalidSessionException("Can not find an active session for a candidate with this token");
        }
        return candidate;
    }

    @Override
    public Candidate updateEmail(UpdateCandidateEmailRequest request) {
        Candidate candidate = getLoggedInCandidate();
        candidate.setEmail(request.getEmail());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateAlternateContacts(UpdateCandidateAlternateContactRequest request) {
        Candidate candidate = getLoggedInCandidate();
        if (StringUtils.isBlank(request.getEmail()) && StringUtils.isBlank(request.getPhone())
                && StringUtils.isBlank(request.getWhatsapp())) {
            throw new InvalidRequestException("You must specify at least one method of contact");
        }
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setWhatsapp(request.getWhatsapp());
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate updateAdditionalContacts(UpdateCandidateAdditionalContactRequest request) {
        Candidate candidate = getLoggedInCandidate();
        candidate.setPhone(request.getPhone());
        candidate.setWhatsapp(request.getWhatsapp());
        return candidateRepository.save(candidate);
    }
}
