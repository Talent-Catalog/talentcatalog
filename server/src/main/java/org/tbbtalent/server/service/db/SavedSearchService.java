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

import javax.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.model.db.SavedSearch;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tbbtalent.server.request.candidate.UpdateDisplayedFieldPathsRequest;
import org.tbbtalent.server.request.search.CreateFromDefaultSavedSearchRequest;
import org.tbbtalent.server.request.search.SearchSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSavedSearchRequest;
import org.tbbtalent.server.request.search.UpdateSharingRequest;
import org.tbbtalent.server.request.search.UpdateWatchingRequest;


public interface SavedSearchService {

    /**
     * Searches for saved searches whose name and other attributes match the
     * given search request.
     * @param request Attributes to search on
     * @return Matching saved searches.
     */
    Page<SavedSearch> searchPaged(SearchSavedSearchRequest request);

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

    SavedSearch updateSavedSearch(long id, UpdateSavedSearchRequest request) throws EntityExistsException;

    boolean deleteSavedSearch(long id);

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
     * Updates the fields that are displayed for each candidate in the results 
     * of the given saved search.
     * @param id Id of saved search
     * @param request Request containing the field paths to be displayed.
     * @throws NoSuchObjectException  if there is no saved search with this id
     */
    void updateDisplayedFieldPaths(
            long id, UpdateDisplayedFieldPathsRequest request)
            throws NoSuchObjectException;

}
