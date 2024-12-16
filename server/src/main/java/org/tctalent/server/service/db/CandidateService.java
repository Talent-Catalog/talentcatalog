/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.exception.CountryRestrictionException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.ExportFailedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.CandidateSubfolderType;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.DataRow;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.partner.Partner;
import org.tctalent.server.model.db.task.QuestionTaskAssignment;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.LoginRequest;
import org.tctalent.server.request.candidate.CandidateEmailOrPhoneSearchRequest;
import org.tctalent.server.request.candidate.CandidateEmailSearchRequest;
import org.tctalent.server.request.candidate.CandidateExternalIdSearchRequest;
import org.tctalent.server.request.candidate.CandidateIntakeAuditRequest;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tctalent.server.request.candidate.CandidateNumberOrNameSearchRequest;
import org.tctalent.server.request.candidate.RegisterCandidateRequest;
import org.tctalent.server.request.candidate.ResolveTaskAssignmentsRequest;
import org.tctalent.server.request.candidate.SavedListGetRequest;
import org.tctalent.server.request.candidate.UpdateCandidateAdditionalInfoRequest;
import org.tctalent.server.request.candidate.UpdateCandidateContactRequest;
import org.tctalent.server.request.candidate.UpdateCandidateEducationRequest;
import org.tctalent.server.request.candidate.UpdateCandidateLinksRequest;
import org.tctalent.server.request.candidate.UpdateCandidateMediaRequest;
import org.tctalent.server.request.candidate.UpdateCandidatePersonalRequest;
import org.tctalent.server.request.candidate.UpdateCandidateRegistrationRequest;
import org.tctalent.server.request.candidate.UpdateCandidateRequest;
import org.tctalent.server.request.candidate.UpdateCandidateShareableNotesRequest;
import org.tctalent.server.request.candidate.UpdateCandidateStatusInfo;
import org.tctalent.server.request.candidate.UpdateCandidateStatusRequest;
import org.tctalent.server.request.candidate.UpdateCandidateSurveyRequest;
import org.tctalent.server.request.chat.FetchCandidatesWithChatRequest;
import org.tctalent.server.util.dto.DtoBuilder;

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
     * Updates the candidates database records corresponding to candidates on
     * elasticsearch.
     * <p/>
     * This is intended to be a bulk update which updates the contents of the
     * database server for ALL non deleted candidates on our database and
     * updates a specific field/fields in the database from ES.
     * This was made to handle if the two databases get out of sync
     * (e.g. due to a bad flyway) and we want the restore the data from the ES.
     * As long as the ES hasn't been reloaded (so retains the original data before flyway).
     * <p/>
     * For performance reasons (and to minimize memory use) this update is done
     * a page of records (eg 20) at a time - as defined by the "pageable"
     * parameter passed to this method. This method will normally be called
     * repeatedly from a background Async task, triggered by an API call to
     * SystemAdminApi, starting with page 0, page 1, etc, until all candidates
     * have been added/updated.
     *
     * @param pageable      The page request - basically the page number.
     * @return The number of candidates added or updated on this call. Normally
     * that will be page full (eg 20).
     */
    int populateCandidatesFromElastic(Pageable pageable);

    Page<Candidate> searchCandidates(CandidateEmailSearchRequest request);

    Page<Candidate> searchCandidates(CandidateEmailOrPhoneSearchRequest request);

    Page<Candidate> searchCandidates(CandidateNumberOrNameSearchRequest request);

    Page<Candidate> searchCandidates(CandidateExternalIdSearchRequest request);

    /**
     * Returns a set of the ids of all candidates resulting from the given SQL query.
     *
     * @param sql SQL query
     * @throws PersistenceException if the SQL is invalid
     * @return Candidate ids (NOT candidateNumbers) of candidates matching query
     */
    @NotNull
    Set<Long> searchCandidatesUsingSql(String sql) throws PersistenceException;

    Page<Candidate> getSavedListCandidates(SavedList savedList, SavedListGetRequest request);

    List<Candidate> getSavedListCandidatesUnpaged(SavedList savedList, SavedListGetRequest request);

    Candidate getCandidate(long id) throws NoSuchObjectException;

    Candidate updateCandidateAdditionalInfo(long id, UpdateCandidateAdditionalInfoRequest request);

    Candidate updateShareableNotes(long id, UpdateCandidateShareableNotesRequest request);

    Candidate updateCandidateSurvey(long id, UpdateCandidateSurveyRequest request);

    Candidate updateCandidateMedia(long id, UpdateCandidateMediaRequest request);

    Candidate updateCandidateRegistration(long id, UpdateCandidateRegistrationRequest request);

    Candidate updateCandidateSalesforceLink(Candidate candidate, String sfLink);

    Candidate updateCandidateStatus(Candidate candidate, UpdateCandidateStatusInfo info);

    void updateCandidateStatus(UpdateCandidateStatusRequest request);

    void updateCandidateStatus(SavedList savedList, UpdateCandidateStatusInfo info);

    Candidate updateCandidateLinks(long id, UpdateCandidateLinksRequest request);

    Candidate updateCandidate(long id, UpdateCandidateRequest request);

    boolean deleteCandidate(long id);

    /**
     * Registers a new candidate by creating a new candidate and user.
     * It returns a login request for the generated candidate so that they are processed as
     * a normal login.
     * <p/>
     * Logic for assigning a candidate to a partner.
     * <p/>
     * A source partner is assigned to a candidate when the candidate registers with the TC.
     * This is the process:
     * <ul>
     *     <li>
     * Candidate follows a url link which directs them to TC registration
     *     </li>
     *     <li>
     * If they have a p=, then the branding of the TC registration is set to that of the partner
     *     </li>
     *     <li>
     * Initially (step 0) they provide email and accept our conditions - then click Register
     *     </li>
     *     <li>
     * They then proceed to step 1 (contact )where the email can be changed and they also can
     * supply phone and whatsapp.
     * When they click Next, the user is actually created using p= branding, or defaulting to TBB
     * - in this method.
     *     </li>
     *     <li>
     * Arriving at step 2 (tell us about yourself - registration personal) they say where they are
     * located.
     * When they click Next, the user is updated with the supplied info
     * (in private method CandidateServiceImpl.checkForChangedPartner).
     * The partner can be changed here if the currently defined partner is the DefaultSourcePartner
     * and there is a partner set up as auto-assignable from the country where the candidate is
     * located.
     *     </li>
     * </ul>
     *
     * @param request Registration request
     * @param httpRequest HTTP request for registration
     * @return A login request generated for the newly created candidate.
     */
    LoginRequest register(RegisterCandidateRequest request, HttpServletRequest httpRequest);

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
     * candidate exams.
     * <p/>
     * See doc for {@link #getLoggedInCandidate()}
     * @return candidate entity preloaded with candidate exams.
     * Returned as Optional - can be empty if nobody is logged in.
     */
    Optional<Candidate> getLoggedInCandidateLoadCandidateExams();

    /**
     * Returns the currently logged in candidate entity preloaded with
     * candidate destinations.
     * <p/>
     * See doc for {@link #getLoggedInCandidate()}
     * @return candidate entity preloaded with candidate destinations.
     * Returned as Optional - can be empty if nobody is logged in.
     */
    Optional<Candidate> getLoggedInCandidateLoadDestinations();

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
     * (See <a href="https://www.baeldung.com/jpa-hibernate-persistence-context">...</a>)
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

    /**
     * Finds candidate with the given candidate number, or null if none found.
     *
     * @param candidateNumber Number of desired candidate
     * @return Candidate or null if none found
     */
    @Nullable
    Candidate findByCandidateNumber(String candidateNumber);

    /**
     * Return candidates by their ids
     * @param ids The ids to be looked up. If an id does not correspond to any candidate,
     *            it is ignored and no candidate is returned.
     * @return Candidates with ids matching the given ids.
     */
    List<Candidate> findByIds(@NonNull Collection<Long> ids);

    /**
     * Restricted access to the candidate with the given candidate number.
     * <p/>
     * Access is restricted to users who are logged in and also depending on any source country
     * restrictions they have associated with them.
     * @param candidateNumber Number of desired candidate
     * @return Candidate
     * @throws InvalidSessionException if user is not logged in
     * @throws CountryRestrictionException if the candidate is not found
     */
    Candidate findByCandidateNumberRestricted(String candidateNumber);

    Candidate getCandidateFromRequest(Long requestCandidateId);

    /**
     * Get the standard name of the candidate subfolder corresponding to the given candidate
     * subfolder type.
     * @param type Type of candidate subfolder
     * @return Subfolder name
     */
    @NonNull
    String getCandidateSubfolderName(CandidateSubfolderType type);

    /**
     * Stored url link to candidate subfolder on Google Drive which corresponds to the given
     * candidate subfolder type, if a link has been stored.
     * <p/>
     * Note that this ends up calling one of the standard {@link Candidate} methods like
     * getFolderlinkAddress etc.
     * @param candidate Candidate owning subfolder
     * @param type Type of candidate subfolder
     * @return Url link or null if no link is stored
     */
    @Nullable
    String getCandidateSubfolderlink(Candidate candidate, CandidateSubfolderType type);

    /**
     * Saves url link of candidate subfolder on Google Drive which corresponds to the given
     * candidate subfolder type.
     * <p/>
     * Note that this ends up calling one of the standard {@link Candidate} methods like
     * setFolderlinkAddress etc.
     * @param candidate Candidate owning subfolder
     * @param type Type of candidate subfolder
     * @param link Url - can be null if we want to clear the link
     */
    void setCandidateSubfolderlink(Candidate candidate, CandidateSubfolderType type,
        @Nullable String link);

    void exportToCsv(SavedList savedList, SavedListGetRequest request, PrintWriter writer)
            throws ExportFailedException;

    List<DataRow> computeGenderStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeGenderStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeUnhcrRegisteredStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeUnhcrRegisteredStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeUnhcrStatusStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeUnhcrStatusStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeLinkedInExistsStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeLinkedInExistsStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeLinkedInStats(LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeLinkedInStats(LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeBirthYearStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeBirthYearStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeReferrerStats(Gender gender, String country, LocalDate dateFrom,
        LocalDate dateTo, List<Long> sourceCountryIds);

    List<DataRow> computeReferrerStats(Gender gender, String country, LocalDate dateFrom,
        LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

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

    List<DataRow> computeSourceCountryStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeSourceCountryStats(Gender gender, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeSurveyStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeSurveyStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    List<DataRow> computeStatusStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, List<Long> sourceCountryIds);
    List<DataRow> computeStatusStats(Gender gender, String country, LocalDate dateFrom, LocalDate dateTo, Set<Long> candidateIds, List<Long> sourceCountryIds);

    Resource generateCv(Candidate candidate, Boolean showName, Boolean showContact);

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
     * Creates a folder for the given candidate on Google Drive, as well as standard subfolders.
     * <p/>
     * If a link to the folder or subfolders are already recorded for the candidate, does nothing.
     * Otherwise, checks whether folders already exist, creating them if necessary, and stores
     * the links with the candidate (and DB).
     *
     * @param id ID of candidate
     * @return Updated candidate object, containing link to folder (created or
     * existing) in {@link Candidate#getFolderlink()}
     * @throws NoSuchObjectException if no candidate is found with that id
     * @throws IOException           if there is a problem creating the folder.
     */
    Candidate createCandidateFolder(long id) throws NoSuchObjectException, IOException;

    /**
     * Applies {@link #createCandidateFolder(long)} for each of the given candidate ids.
     * @param candidateIds ids of candidates
     * @throws NoSuchObjectException if no candidate is found with an id
     * @throws IOException           if there is a problem creating the folders.
     */
    void createCandidateFolder(Collection<Long> candidateIds)
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
     * @throws SalesforceException If there are errors relating to keys
     * and digital signing.
     * @throws WebClientException if there is a problem connecting to Salesforce
     */
    Candidate createUpdateSalesforce(long id)
            throws NoSuchObjectException, SalesforceException, WebClientException;

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
     * Updates the intake data associated with the given candidate.
     * @param id ID of candidate
     * @param request Request object containing the audit data, only provided if the intake is an externally input intake.
     * @throws NoSuchObjectException if no candidate is found with that id
     */
    Candidate completeIntake(long id, CandidateIntakeAuditRequest request)
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

    /**
     * This candidate exam method is moved into the Candidate Service due a
     * circular dependency error when referencing the candidate service in
     * the Candidate Exam Service. The Candidate Service is needed in this method
     * to update the Candidate's record on Elasticsearch with the updated IeltsScore field.
     * Delete the candidate exam with the given id.
     * @param examId ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    boolean deleteCandidateExam(long examId)
            throws EntityReferencedException, InvalidRequestException;

    /**
     * Retrieve a dummy candidate who can be used for testing.
     * @return a dummy test candidate
     */
    Candidate getTestCandidate();

    // TODO: 12/2/22 Doc
    Candidate findByIdLoadSavedLists(long candidateId);

    //TODO JC Doc
    void saveIt(Candidate candidate);

    //todo doc
    Candidate findByIdLoadUser(long id, Set<Country> sourceCountries);

    //todo doc
    String[] getExportCandidateStrings(Candidate candidate);

    //todo doc
    String[] getExportTitles();

    /**
     * Stores the given answer supplied for the given question task assignment.
     * @param ta Question task assignment
     * @param answer Answer to question
     * @throws InvalidRequestException If the task associated with the given task assignment is not
     * a QuestionTask
     */
    void storeCandidateTaskAnswer(QuestionTaskAssignment ta, String answer);

    /**
     * Allows admins to resolve candidates tasks within a list, turning any outstanding required tasks to completed & abandoned.
     * Also turns abandoned tasks to completed. When a task has a completed & abandoned date we know it was resolved by TBB staff.
     * @param request This request contains the list of candidate ids selected from a list. These are the candidates that are to have their tasks resolved.
     */
    void resolveOutstandingTaskAssignments(ResolveTaskAssignmentsRequest request);

    /**
     * Upserts candidates to SF contacts and updates their TC profile SF links
     * @param orderedCandidates - candidates must be ordered for final step of updating SF links to work
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    void upsertCandidatesToSf(List<Candidate> orderedCandidates);

    /**
     * Processes a single page for the TC-SF candidate sync.
     * @param startPage page to process (zero-based index)
     * @param statuses types of {@link CandidateStatus} to filter for in search
     * @throws WebClientException if there is a problem connecting to Salesforce
     * @throws SalesforceException if Salesforce had a problem with the data
     */
    void processSfCandidateSyncPage(
        long startPage, List<CandidateStatus> statuses
    ) throws SalesforceException, WebClientException;

    /**
     * Returns IDs of Job Chats of type 'CandidateProspect' for candidates managed by the logged-in
     * user's partner organisation, if they contain posts unread by same user.
     * @return list of IDs of Job Chats matching the criteria
     */
    List<Long> findUnreadChatsInCandidates();

    /**
     * If unreadOnly boolean contained in request is true, returns paged search results of
     * candidates managed by the logged-in user's partner organisation, if they have a Job Chat of
     * type 'CandidateProspect' containing at least one post that is unread by the logged-in user.
     * If unreadOnly is false, the candidates' chat only has to contain one post, read or unread.
     * @param request {@link FetchCandidatesWithChatRequest}
     * @return paged search results of candidates matching the criteria
     */
    Page<Candidate> fetchCandidatesWithChat(FetchCandidatesWithChatRequest request);

    /**
     * Extracts to a list the candidates on given page, iterates over list, setting partnerId on
     * associated user object, saves to DB and updates the corresponding elasticsearch index entry.
     * @param candidatePage page of candidates
     * @param newPartner the new partner to which they will be assigned
     */
    void reassignCandidatesOnPage(Page<Candidate> candidatePage, Partner newPartner)
        throws IllegalArgumentException;

    List<Candidate> fetchPotentialDuplicatesOfCandidateWithGivenId(@NotNull Long candidateId);

    /**
     * Takes a page of potentially duplicated candidates and if not already true, sets their
     * potentialDuplicate property to true and saves them to DB. annotated @Transactional to
     * rollback incomplete confusing results.
     * <p>
     *   NB: currently not saving to ES - would need to be amended if we decide to index this property.
     * </p>
     * @param candidatePage page of potential duplicate candidates to be processed
     */
    void processPotentialDuplicatePage(Page<Candidate> candidatePage);

    /**
     * If admins have not deliberately refreshed results for a candidate whose duplicated versions
     * have been marked deleted, their potentialDuplicates value would remain true - this component
     * of the duplicate processing fixes that by comparing with previously flagged candidates.
     * Again, annotated @Transactional to rollback incomplete confusing results.
     */
    void cleanUpResolvedDuplicates();
}
