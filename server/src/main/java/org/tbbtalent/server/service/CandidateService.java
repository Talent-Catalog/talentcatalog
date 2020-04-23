package org.tbbtalent.server.service;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.DataRow;
import org.tbbtalent.server.model.Gender;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.CandidateEmailSearchRequest;
import org.tbbtalent.server.request.candidate.CandidateNumberOrNameSearchRequest;
import org.tbbtalent.server.request.candidate.CandidatePhoneSearchRequest;
import org.tbbtalent.server.request.candidate.CreateCandidateRequest;
import org.tbbtalent.server.request.candidate.RegisterCandidateRequest;
import org.tbbtalent.server.request.candidate.SavedSearchRunRequest;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateAdditionalInfoRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateContactRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateLinksRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidatePersonalRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateStatusRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateSurveyRequest;
import org.tbbtalent.server.request.candidate.stat.CandidateStatDateRequest;

import java.io.PrintWriter;
import java.rmi.server.ExportException;
import java.util.List;

public interface CandidateService {

    Page<Candidate> searchCandidates(SearchCandidateRequest request);

    Page<Candidate> searchCandidates(SavedSearchRunRequest request);

    Page<Candidate> searchCandidates(CandidateEmailSearchRequest request);

    Page<Candidate> searchCandidates(CandidateNumberOrNameSearchRequest request);

    Page<Candidate> searchCandidates(CandidatePhoneSearchRequest request);

    Candidate getCandidate(long id);

    Candidate createCandidate(CreateCandidateRequest request) throws UsernameTakenException;

    Candidate updateCandidateAdditionalInfo(long id, UpdateCandidateAdditionalInfoRequest request);

    Candidate updateCandidateSurvey(long id, UpdateCandidateSurveyRequest request);

    Candidate updateCandidateStatus(long id, UpdateCandidateStatusRequest request);

    Candidate updateCandidateLinks(long id, UpdateCandidateLinksRequest request);

    Candidate updateCandidate(long id, UpdateCandidateRequest request);

    boolean deleteCandidate(long id);

    LoginRequest register(RegisterCandidateRequest request);

    Candidate updateContact(UpdateCandidateContactRequest request);

    Candidate updatePersonal(UpdateCandidatePersonalRequest request);

    Candidate updateEducation(UpdateCandidateEducationRequest request);

    Candidate updateAdditionalInfo(UpdateCandidateAdditionalInfoRequest request);

    Candidate updateCandidateSurvey(UpdateCandidateSurveyRequest request);

    Candidate getLoggedInCandidateLoadCandidateOccupations();

    Candidate getLoggedInCandidateLoadEducations();

    Candidate getLoggedInCandidateLoadJobExperiences();

    Candidate getLoggedInCandidateLoadCertifications();

    Candidate getLoggedInCandidateLoadCandidateLanguages();

    Candidate getLoggedInCandidate();

    Candidate getLoggedInCandidateLoadProfile();

    Candidate findByCandidateNumber(String candidateNumber);

    void exportToCsv(SavedSearchRunRequest request, PrintWriter writer) throws ExportException;

    void exportToCsv(SearchCandidateRequest request, PrintWriter writer) throws ExportException;

    List<DataRow> getGenderStats(CandidateStatDateRequest request);

    List<DataRow> getBirthYearStats(Gender gender, CandidateStatDateRequest request);

    List<DataRow> getRegistrationStats(CandidateStatDateRequest request);

    List<DataRow> getRegistrationOccupationStats(CandidateStatDateRequest request);

    List<DataRow> getLanguageStats(Gender gender, CandidateStatDateRequest request);

    List<DataRow> getOccupationStats(Gender gender, CandidateStatDateRequest request);

    List<DataRow> getMostCommonOccupationStats(Gender gender, CandidateStatDateRequest request);

    List<DataRow> getSpokenLanguageLevelStats(Gender gender, String language, CandidateStatDateRequest request);

    List<DataRow> getMaxEducationStats(Gender gender, CandidateStatDateRequest request);

    List<DataRow> getNationalityStats(Gender gender, String country, CandidateStatDateRequest request);

    List<DataRow> getSurveyStats(Gender gender, String country, CandidateStatDateRequest request);

    Resource generateCv(Candidate candidate);

    void notifyWatchers();

}
