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

package org.tbbtalent.server.service.db;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tbbtalent.server.exception.ExportFailedException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.DataRow;
import org.tbbtalent.server.model.db.Gender;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.request.LoginRequest;
import org.tbbtalent.server.request.candidate.*;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CandidateService {

    /**
     * Adds or updates the Elasticsearch records corresponding to candidates on
     * our standard database.
     * <p/>
     * This is intended to be a bulk update which updates the contents of the
     * elasticsearch server to for ALL non deleted candidates on our database.
     * <p/>
     * For performance reasons (and to minimize memory use) this update is done
     * a page of records (eg 20) at a time - as defined by the "pageable"
     * parameter passed to this method. This method will normally be called
     * repeatedly from a background Async task, triggered by an API call to
     * SystemAdminApi, starting with page 0, page 1, etc, until all candidates
     * have been added/updated.
     *
     * @param pageable      The page request - basically the page number.
     * @param logTotal      If true, the method is requested to log the total
     *                      number of candidates to be updated.
     * @param createElastic If true, it is assumed that the Elasticsearch has
     *                      started empty, so new records need to be created
     *                      (rather than updating existing records).
     * @return The number of candidates added or updated on this call. Normally
     * that will be page full (eg 20).
     */
    int populateElasticCandidates(
            Pageable pageable, boolean logTotal, boolean createElastic);

    /**
     * Returns the requested page of candidates which match the attributes in
     * the request.
     * @param request Request specifying which candidates to return
     * @return Page of candidates
     */
    Page<Candidate> searchCandidates(SearchCandidateRequest request);

    /**
     * Returns the requested page of candidates of the given saved search.
     * @param savedSearchId ID of saved search
     * @param request Request specifying which candidates to return
     * @return Page of candidates
     * @throws NoSuchObjectException is no saved search exists with given id.
     */
    Page<Candidate> searchCandidates(
            long savedSearchId, SavedSearchGetRequest request)
            throws NoSuchObjectException;

    /**
     * Returns a set of the ids of all candidates matching the given saved search.
     * @param savedSearchId ID of saved search
     * @return Candidate ids (NOT candidateNumbers) of candidates matching search
     * @throws NoSuchObjectException is no saved search exists with given id.
     */
    Set<Long> searchCandidates(long savedSearchId)
            throws NoSuchObjectException;

    Page<Candidate> searchCandidates(CandidateEmailSearchRequest request);

    Page<Candidate> searchCandidates(CandidateNumberOrNameSearchRequest request);

    Page<Candidate> searchCandidates(CandidatePhoneSearchRequest request);

    Page<Candidate> getSavedListCandidates(long id, SavedListGetRequest request);

    /**
     * Remove the given candidate from all its lists
     * @param candidateId ID of candidate
     * @return False if no candidate with that id was found, otherwise true.
     */
    boolean clearCandidateSavedLists(long candidateId);

    /**
     * Merge the saved lists indicated in the request into the given candidate's
     * existing lists.
     *
     * @param candidateId ID of candidate to be updated
     * @param request     Request containing the saved lists to be merged into
     *                    the candidate's existing lists
     * @return False if no candidate with that id was found, otherwise true.
     */
    boolean mergeCandidateSavedLists(long candidateId, IHasSetOfSavedLists request);

    /**
     * Merge the saved lists indicated in the request into the given candidate's
     * existing lists.
     *
     * @param candidateId ID of candidate to be updated
     * @param request     Request containing the new saved lists
     * @return False if no candidate with that id was found, otherwise true.
     */
    boolean removeFromCandidateSavedLists(long candidateId, IHasSetOfSavedLists request);

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

    /**
     * Returns a candidate once they have completed their registration
     * <p/>
     */
    Candidate submitRegistration();

    /**
     * Returns the currently logged in candidate entity preloaded with
     * candidate occupations.
     * <p/>
     * See doc for {@link #getLoggedInCandidate()}
     * @return candidate entity preloaded with candidate occupations. 
     * Returned as Optional - can be empty if nobody is logged in.
     */
    Optional<Candidate> getLoggedInCandidateLoadCandidateOccupations();

    /**
     * Returns the currently logged in candidate entity preloaded with
     * candidate certifications.
     * <p/>
     * See doc for {@link #getLoggedInCandidate()}
     * @return candidate entity preloaded with candidate certifications. 
     * Returned as Optional - can be empty if nobody is logged in.
     */
    Optional<Candidate> getLoggedInCandidateLoadCertifications();

    /**
     * Returns the currently logged in candidate entity preloaded with
     * candidate languages.
     * <p/>
     * See doc for {@link #getLoggedInCandidate()}
     * @return candidate entity preloaded with candidate languages. 
     * Returned as Optional - can be empty if nobody is logged in.
     */
    Optional<Candidate> getLoggedInCandidateLoadCandidateLanguages();

    /**
     * Returns the currently logged in candidate entity.
     * <p/>
     * Note that the Candidate entity only lazily loads associated attributes.
     * So, for example, attributes like <code>candidateOccupations</code>
     * will not be populated. They will only be populated as needed, eg when 
     * accessed through a method like {@link Candidate#getCandidateOccupations()}.
     * <p/>
     * In that case, assuming that the JPA "persistence context" is still active
     * (which it normally will be in your controllers processing HTTP requests),
     * JPA will perform another database access to populate the candidate
     * occupations.
     * (See https://www.baeldung.com/jpa-hibernate-persistence-context)
     * <p/>
     * Note that our DTO builder class {@link DtoBuilder} will also trigger 
     * loading of the requested attributes from the database.
     * <p/>
     * Note: In order to avoid unnecessary database accesses, there are some 
     * special methods such as {@link #getLoggedInCandidateLoadCandidateOccupations()} 
     * which load specific attributes at the same time as the Candidate entity
     * is fetched. This is achieved by using "join fetch" in the repository 
     * query.
     * See, for example, {@link CandidateRepository#findByIdLoadCandidateOccupations}. 
     * @return Lazily loaded entity corresponding to currently logged in 
     * candidate. Returned as Optional - can be empty if nobody is logged in.
     */
    Optional<Candidate> getLoggedInCandidate();

    Candidate findByCandidateNumber(String candidateNumber);

    void exportToCsv(long savedListId, SavedListGetRequest request, PrintWriter writer)
            throws ExportFailedException;

    void exportToCsv(long savedSearchId, SavedSearchGetRequest request, PrintWriter writer)
            throws ExportFailedException;

    void exportToCsv(SearchCandidateRequest request, PrintWriter writer)
            throws ExportFailedException;

    void setCandidateContext(long savedSearchId, Iterable<Candidate> candidates);

    List<DataRow> computeGenderStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeGenderStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeBirthYearStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeBirthYearStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeRegistrationStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeRegistrationStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeRegistrationOccupationStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeRegistrationOccupationStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeLanguageStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeLanguageStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeMostCommonOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeMostCommonOccupationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeSpokenLanguageLevelStats(Gender gender, String language, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeSpokenLanguageLevelStats(Gender gender, String language, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeMaxEducationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeMaxEducationStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeNationalityStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeNationalityStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeSurveyStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeSurveyStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    Resource generateCv(Candidate candidate);

    /**
     * IMPORTANT: Use this instead of {@link CandidateRepository#save} Saves
     * candidate to repository, but also optionally updates corresponding
     * Elasticsearch CandidateEs
     *
     * @param candidate         Candidate to be saved
     * @param updateCandidateEs If true, will also update Elasticsearch
     * @return Candidate object as returned by {@link CandidateRepository#save}
     */
    Candidate save(Candidate candidate, boolean updateCandidateEs);

    /**
     * Creates a folder for the given candidate on Google Drive.
     * <p/>
     * If a folder already exists for the candidate, does nothing.
     *
     * @param id ID of candidate
     * @return Updated candidate object, containing link to folder (created or
     * existing) in {@link Candidate#getFolderlink()}
     * @throws NoSuchObjectException if no candidate is found with that id
     * @throws IOException           if there is a problem creating the folder.
     */
    Candidate createCandidateFolder(long id)
            throws NoSuchObjectException, IOException;

    /**
     * Creates/updates a Contact record on Salesforce for the given candidate.
     * <p/>
     * If no Contact record exists, one is created.
     * If a record exists, it is updated to match the candidate details.
     * <p/>
     * The link to Salesforce record (sflink) is established and stored.
     *
     * @param id ID of candidate
     * @return Updated candidate object, containing link to corresponding 
     * Salesforce Contact record (created or
     * existing) in {@link Candidate#getSflink()}
     * @throws NoSuchObjectException if no candidate is found with that id
     * @throws GeneralSecurityException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    Candidate createUpdateSalesforce(long id)
            throws NoSuchObjectException, GeneralSecurityException,
            WebClientException;

    /**
     * Updates the intake data associated with the given candidate.
     * @param id ID of candidate
     * @param data Partially populated CandidateIntakeData record. Null data
     *             fields are ignored. Only non null fields are updated.
     * @throws NoSuchObjectException if no candidate is found with that id
     */
    void updateIntakeData(long id, CandidateIntakeDataUpdate data)
        throws NoSuchObjectException;

    /**
     * Checks all candidate data related to TBB destinations and checks that
     * the data matches the currently configured TBB destinations 
     * (eg Australia, Canada etc). It adds any missing destination records
     * if necessary and returns the updated Candidate record.
     * <p/>
     * Typically this only needs to be called when updating a candidate's
     * intake data so that extra data can be added for any new destinations.
     * <p/>
     * This allows candidate data to be modified for new TBB destinations only
     * as needed - rather than having to do a mass database update of all
     * candidate data each time a new TBB destination is added.
     * <p/>
     * Note that TBB destinations are configured in application.yml
     * (tbb.destinations). 
     * @param candidate Candidate to be checked
     * @return Updated candidate record.
     */
    Candidate addMissingDestinations(Candidate candidate);
}
