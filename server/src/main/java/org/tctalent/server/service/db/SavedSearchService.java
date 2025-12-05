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

import jakarta.validation.constraints.NotNull;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.ExportFailedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.InvalidSessionException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.request.IdsRequest;
import org.tctalent.server.request.candidate.SavedSearchGetRequest;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tctalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tctalent.server.request.candidate.source.UpdateCandidateSourceDescriptionRequest;
import org.tctalent.server.request.search.CreateFromDefaultSavedSearchRequest;
import org.tctalent.server.request.search.SearchSavedSearchRequest;
import org.tctalent.server.request.search.UpdateSavedSearchRequest;
import org.tctalent.server.request.search.UpdateSharingRequest;
import org.tctalent.server.request.search.UpdateWatchingRequest;


public interface SavedSearchService {
    /**
     * <p>
     * Extracts native database query SQL corresponding to the given search request.
     * </p>
     * <p>
     *     The SQL will always be a "SELECT FROM candidate" statement plus joins to other tables
     *     as needed and a WHERE clause.
     * </p>
     * <p>
     *     The request will return candidate data without duplicates.
     * </p>
     * @return String containing the SQL
     */
    String extractFetchSQL(SearchCandidateRequest request);

    /**
     * Return all SavedSearch's that match the given ids, ordered by name.
     * @param request Defines the ids of the SavedSearch's to return
     * @return Requested SavedSearch's
     */
    List<SavedSearch> search(IdsRequest request);

    /**
     * Searches for saved searches whose name and other attributes match the
     * given search request.
     * <p/>
     * See also {@link #searchPaged} which does the same except
     * returns just one page of results.
     * @param request Attributes to search on
     * @return Matching saved searches.
     */
    List<SavedSearch> search(SearchSavedSearchRequest request);

    /**
     * Searches for a page of saved searches whose name and other attributes match the
     * given search request.
     * @param request Attributes to search on - including paging details
     * @return Requested page of matching saved searches.
     */
    Page<SavedSearch> searchPaged(SearchSavedSearchRequest request);

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
     * @param request Request specifying which page of candidates to return
     * @return Page of candidates
     * @throws NoSuchObjectException is no saved search exists with given id.
     */
    Page<Candidate> searchCandidates(
        long savedSearchId, SavedSearchGetRequest request)
        throws NoSuchObjectException;

    /**
     * Returns a set of the ids of all candidates matching the given saved search.
     * <p/>
     * WARNING: This method clears the JPA persistence context by calling entityManager.clear().
     *
     * @param savedSearchId ID of saved search
     * @return Candidate ids (NOT candidateNumbers) of candidates matching search
     * @throws NoSuchObjectException if no saved search exists with given id.
     * @throws InvalidRequestException if we exceed the limit on the maximum number of candidates.
     */
    @NotNull
    Set<Long> searchCandidates(long savedSearchId)
        throws NoSuchObjectException, InvalidRequestException;

    void setCandidateContext(long savedSearchId, Iterable<Candidate> candidates);

    SearchCandidateRequest loadSavedSearch(long id);

    SavedSearch getSavedSearch(long id);

    /**
     * Clears the given user's selections for the given saved search.
     * @param id ID of saved search
     * @param userId Associated user id
     * @throws InvalidRequestException if not authorized.
     * @throws NoSuchObjectException if there is no such saved search
     */
    void clearSelection(long id, Long userId)
            throws InvalidRequestException, NoSuchObjectException;

    /**
     * Creates a new saved search from the data in the given request.
     * @param request Request containing details from which the record is created.
     * @return Created saved search
     * @throws EntityExistsException If a saved search with the requested name
     * already exists.
     */
    SavedSearch createSavedSearch(UpdateSavedSearchRequest request)
            throws EntityExistsException;

    /**
     * Creates a new saved search from the current user's default saved search,
     * named as specified in the request (either with the name of a specified
     * existing saved list, or with a specified name) and with the sfJoblink,
     * if any, in the request.
     * <p/>
     * The selection for the new saved search is the same as the selection
     * (including any context notes) for the default search.
     * <p/>
     * If a saved search with the given already exists, it is replaced.
     * @param request Request containing details from which the search is created.
     * @return Created search
     * @throws NoSuchObjectException If there is no logged in user or no list
     * with given id.
     */
    SavedSearch createFromDefaultSavedSearch(
            CreateFromDefaultSavedSearchRequest request)
            throws NoSuchObjectException;

    void setPublicIds(List<SavedSearch> savedSearches);

    SavedSearch updateSavedSearch(long id, UpdateSavedSearchRequest request) throws EntityExistsException;

    boolean deleteSavedSearch(long id);

    void exportToCsv(long savedSearchId, SavedSearchGetRequest request, PrintWriter writer)
        throws ExportFailedException;

    void exportToCsv(SearchCandidateRequest request, PrintWriter writer)
        throws ExportFailedException;

    /**
     * Adds a user who wants to share the given saved search (created by someone
     * else).
     * @param id id of Saved Search being shared
     * @param request Contains the id of the user who is sharing the search
     * @return The updated saved search with a modified collection of users
     * who are sharing it.
     * @throws NoSuchObjectException if there is no saved search with this id
     * or the user is not found.
     */
    SavedSearch addSharedUser(long id, UpdateSharingRequest request)
            throws NoSuchObjectException;

    /**
     * Removes a user who was sharing the given saved search (created by someone
     * else).
     * @param id id of Saved Search
     * @param request Contains the id of the user who is no longer interested
     *                in sharing the search
     * @return The updated saved search with a modified collection of users
     * who are sharing it.
     * @throws NoSuchObjectException if there is no saved search with this id
     * or the user is not found.
     */
    SavedSearch removeSharedUser(long id, UpdateSharingRequest request)
            throws NoSuchObjectException;

    SavedSearch addWatcher(long id, UpdateWatchingRequest request);

    SavedSearch removeWatcher(long id, UpdateWatchingRequest request);

    /**
     * Returns the scratch saved search for the logged in user,
     * creating one if necessary (there should only ever be one)
     * @return A saved search to be used by the given user for defining
     * searches which have not been saved.
     * @throws NoSuchObjectException is there is no logged in user
     */
    @NotNull SavedSearch getDefaultSavedSearch() throws NoSuchObjectException;

    /**
     * Returns a selection saved list for the given saved search and user,
     * creating one if necessary (there should only ever be one)
     * @param id Saved search ID
     * @param userId User id
     * @return A saved list to be used by the given user for selecting candidates
     * associated with the given saved list.
     * @throws NoSuchObjectException if there is no such saved search or user.
     */
    @NotNull SavedList getSelectionList(long id, Long userId)
            throws NoSuchObjectException;

    /**
     * Returns a selection saved list for the given saved search and the currently logged in user,
     * creating one if necessary (there should only ever be one)
     * @param id Saved search ID
     * @return A saved list to be used by the given user for selecting candidates
     * associated with the given saved list.
     * @throws NoSuchObjectException if there is no such saved search.
     * @throws InvalidSessionException if there is no logged in user.
     */
    @NotNull SavedList getSelectionListForLoggedInUser(long id)
            throws NoSuchObjectException, InvalidSessionException;

    /**
     * Returns true if the given search involves an Elastic search - either itself or in its
     * base class.
     * @param id ID of search
     * @return True if an Elastic search is part of this search.
     * @throws NoSuchObjectException if there is no such saved search.
     */
    boolean includesElasticSearch(long id) throws NoSuchObjectException;

    /**
     * Returns true if there are no candidates matching search
     * @param id ID of search
     * @return True if no candidates
     * @throws NoSuchObjectException if there is no such saved search.
     */
    boolean isEmpty(long id) throws NoSuchObjectException;

    /**
     * Updates a candidate context note associated with the given saved search.
     * (Actually associated with the selection list of the given saved search).
     * If the candidate (specified in the request) is not currently selected
     * into the saved search, does nothing.
     * @param id Id of saved search
     * @param request Request containing the candidate id and the context note
     *                text
     */
    void updateCandidateContextNote(
            long id, UpdateCandidateContextNoteRequest request);

    /**
     * Updates the description of the given saved search.
     * @param id Id of saved search
     * @param request Request containing the updated description
     * @throws NoSuchObjectException  if there is no saved search with this id
     */
    void updateDescription(long id, UpdateCandidateSourceDescriptionRequest request)
        throws  NoSuchObjectException;

    /**
     * Updates the fields that are displayed for each candidate in the results
     * of the given saved search.
     * @param id Id of saved search
     * @param request Request containing the field paths to be displayed.
     * @throws NoSuchObjectException  if there is no saved search with this id
     */
    void updateDisplayedFieldPaths(
            long id, UpdateDisplayedFieldPathsRequest request)
            throws NoSuchObjectException;

    /**
     * Updates the names of suggested searches for the given Job, to reflect its new name.
     * Job renaming happens first - if for any reason that failed, this method has the virtue of
     * reproducing the old name.
     * @param job Job whose suggested searches are to be renamed
     * @param oldJobName the previous name of the Job, used for character replacement
     */
    void updateSuggestedSearchesNames(SalesforceJobOpp job, String oldJobName);
}
