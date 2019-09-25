package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.*;
import org.tbbtalent.server.response.JwtAuthenticationResponse;

import javax.security.auth.login.AccountLockedException;

public interface CandidateService {

    Page<Candidate> searchCandidates(SearchCandidateRequest request);

    Candidate getCandidate(long id);

    Candidate createCandidate(CreateCandidateRequest request);

    Candidate updateCandidate(long id, UpdateCandidateRequest request);

    boolean deleteCandidate(long id);

    JwtAuthenticationResponse login(LoginRequest request) throws AccountLockedException;

    JwtAuthenticationResponse register(RegisterCandidateRequest request) throws AccountLockedException;

    void logout();

    Candidate updateEmail(UpdateCandidateEmailRequest request);

    Candidate updateAlternateContacts(UpdateCandidateAlternateContactRequest request);

    Candidate updateAdditionalContacts(UpdateCandidateAdditionalContactRequest request);

    Candidate updatePersonal(UpdateCandidatePersonalRequest request);

    Candidate updateLocation(UpdateCandidateLocationRequest request);

    Candidate updateNationality(UpdateCandidateNationalityRequest request);

    Candidate updateEducationLevel(UpdateCandidateEducationLevelRequest request);

    Candidate updateAdditionalInfo(UpdateCandidateAdditionalInfoRequest request);

    Candidate getLoggedInCandidateLoadProfessions();

    Candidate getLoggedInCandidateLoadEducations();

    Candidate getLoggedInCandidateLoadWorkExperiences();

    Candidate getLoggedInCandidateLoadCertifications();

    Candidate getLoggedInCandidateLoadCandidateLanguages();

    Candidate getLoggedInCandidate();

}
