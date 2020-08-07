/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.io.PrintWriter;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.tbbtalent.server.exception.ExportFailedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.DataRow;
import org.tbbtalent.server.model.db.Gender;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.CandidateEmailSearchRequest;
import org.tbbtalent.server.request.candidate.CandidateNumberOrNameSearchRequest;
import org.tbbtalent.server.request.candidate.CandidatePhoneSearchRequest;
import org.tbbtalent.server.request.candidate.CreateCandidateRequest;
import org.tbbtalent.server.request.candidate.IHasSetOfSavedLists;
import org.tbbtalent.server.request.candidate.RegisterCandidateRequest;
import org.tbbtalent.server.request.candidate.SavedListGetRequest;
import org.tbbtalent.server.request.candidate.SavedSearchGetRequest;
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

public interface CandidateService {

    /**
     * Adds or updates the Elasticsearch records corresponding to candidates
     * on our standard database.
     * <p/>
     * This is intended to be a bulk update which updates the contents of
     * the elasticsearch server to for ALL non deleted candidates on our 
     * database.
     * <p/> 
     * For performance reasons (and to minimize memory use) this update is
     * done a page of records (eg 20) at a time - as defined by the "pageable" 
     * parameter passed to this method.
     * This method will normally be called repeatedly from a background Async 
     * task, triggered by an API call to SystemAdminApi, starting with page 0,
     * page 1, etc, until all candidates have been added/updated.
     * @param pageable The page request - basically the page number.
     * @param logTotal If true, the method is requested to log the total number
     *                 of candidates to be updated.
     * @param createElastic If true, it is assumed that the Elasticsearch has
     *                      started empty, so new records need to be created
     *                      (rather than updating existing records).
     * @return The number of candidates added or updated on this call. Normally
     * that will be page full (eg 20).
     */
    int populateElasticCandidates(
            Pageable pageable, boolean logTotal, boolean createElastic);
    
    Page<Candidate> searchCandidates(SearchCandidateRequest request);

    Page<Candidate> searchCandidates(
            long savedSearchId, SavedSearchGetRequest request) 
            throws NoSuchObjectException;

    Page<Candidate> searchCandidates(CandidateEmailSearchRequest request);

    Page<Candidate> searchCandidates(CandidateNumberOrNameSearchRequest request);

    Page<Candidate> searchCandidates(CandidatePhoneSearchRequest request);

    Page<Candidate> getSavedListCandidates(long id, SavedListGetRequest request);

    /**
     * Merge the saved lists indicated in the request into the given candidate's
     * existing lists.
     * @param candidateId ID of candidate to be updated
     * @param request Request containing the saved lists to be merged into the 
     *                candidate's existing lists
     * @return False if no candidate with that id was found, otherwise true.
     */
    boolean mergeCandidateSavedLists(long candidateId, IHasSetOfSavedLists request);

    /**
     * Merge the saved lists indicated in the request into the given candidate's
     * existing lists.
     * @param candidateId ID of candidate to be updated
     * @param request Request containing the new saved lists
     * @return False if no candidate with that id was found, otherwise true.
     */
    boolean removeFromCandidateSavedLists(long candidateId, IHasSetOfSavedLists request);

    /**
     * Replace given candidate's existing lists with the saved lists indicated 
     * in the request.
     * @param candidateId ID of candidate to be updated
     * @param request Request containing the new saved lists
     * @return False if no candidate with that id was found, otherwise true.
     */
    boolean replaceCandidateSavedLists(long candidateId, IHasSetOfSavedLists request);

    Candidate getCandidate(long id) throws NoSuchObjectException;

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

    void exportToCsv(long savedListId, SavedListGetRequest request, PrintWriter writer) 
            throws ExportFailedException;

    void exportToCsv(long savedSearchId, SavedSearchGetRequest request, PrintWriter writer) 
            throws ExportFailedException;

    void exportToCsv(SearchCandidateRequest request, PrintWriter writer) 
            throws ExportFailedException;

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

    /**
     * IMPORTANT: Use this instead of {@link CandidateRepository#save} 
     * Saves candidate to repository, but also optionally updates corresponding
     * Elasticsearch CandidateEs
     * @param candidate Candidate to be saved
     * @param updateCandidateEs If true, will also update Elasticsearch
     * @return Candidate object as returned by {@link CandidateRepository#save} 
     */
    Candidate save(Candidate candidate, boolean updateCandidateEs);

}
