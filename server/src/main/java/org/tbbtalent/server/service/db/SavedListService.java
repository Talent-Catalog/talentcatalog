/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.request.list.CreateSavedListRequest;
import org.tbbtalent.server.request.list.IHasSetOfCandidates;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
import org.tbbtalent.server.request.list.TargetListSelection;
import org.tbbtalent.server.request.list.UpdateSavedListInfoRequest;
import org.tbbtalent.server.request.search.UpdateSharingRequest;

/**
 * Saved List Service
 *
 * @author John Cameron
 */
public interface SavedListService {

    /**
     * Copies the given list to the list specified in the given request (which
     * may be a requested new list).
     * @param id ID of list to be copied
     * @param request Defines the target list and also whether copy is a 
     *                replace or an add.
     * @return The target list                
     * @throws EntityExistsException If a new list needs to be created but the
     * list name already exists.
     * @throws NoSuchObjectException if there is no saved list matching the id
     * or the target list id. 
     */
    SavedList copy(long id, TargetListSelection request)
            throws EntityExistsException, NoSuchObjectException;

    /**
     * Create a new SavedList 
     * @param request Create request
     * @return Created saved list
     * @throws EntityExistsException if a list with this name already exists.
     */
    SavedList createSavedList(CreateSavedListRequest request) 
            throws EntityExistsException;

    /**
     * Delete the SavedList with the given ID
     * @param savedListId ID of SavedList to delete
     * @return True if delete was successful
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    boolean deleteSavedList(long savedListId) throws InvalidRequestException;

    /**
     * Get the SavedList with the given id.
     * @param savedListId ID of SavedList to get 
     * @return Saved list
     * @throws NoSuchObjectException if there is no saved list with this id. 
     */
    SavedList get(long savedListId) throws NoSuchObjectException;

    /**
     * Return all SavedList's associated with the given candidate that match 
     * the given request, ordered by name.
     * <p/>
     * See also {@link #listSavedLists} which does the same except for
     * any candidate.
     * 
     * @param candidateId Candidate whose lists we are searching
     * @param request Defines which SavedList's to return 
     * @return Matching SavedList's
     */
    List<SavedList> search(long candidateId, SearchSavedListRequest request);

    /**
     * Return all SavedList's that match the given request, ordered by name.
     * <p/>
     * See also {@link #searchSavedLists} which does the same except
     * returns just one page of results.
     * @param request Defines which SavedList's to return
     * @return Matching SavedList's
     */
    List<SavedList> listSavedLists(SearchSavedListRequest request);

    /**
     * Merge the contents of the SavedList with the given id with the 
     * candidates indicated in the given request.
     * @param savedListId ID of saved list to be updated
     * @param request Request containing the contents to be merged into the list
     * @return False if no saved list with that id was found, otherwise true.
     */
    boolean mergeSavedList(long savedListId, IHasSetOfCandidates request);  

    /**
     * Remove the candidates indicated in the given request from the SavedList 
     * with the given id.
     * @param savedListId ID of saved list to be updated
     * @param request Request containing the new list contents
     * @return False if no saved list with that id was found, otherwise true.
     */
    boolean removeFromSavedList(long savedListId, IHasSetOfCandidates request); 

    /**
     * Replace the contents of the SavedList with the given id with the 
     * candidates indicated in the given request
     * @param savedListId ID of saved list to be updated
     * @param request Request containing the new list contents
     * @return False if no saved list with that id was found, otherwise true.
     */
    boolean replaceSavedList(long savedListId, IHasSetOfCandidates request); 

    /**
     * Return a page of SavedList's that match the given request, ordered by
     * name.
     * <p/>
     * See also {@link #listSavedLists} which does the same except it
     * returns all matching results.
     * @param request Defines which SavedList's to return
     * @return Matching SavedList's
     */
    Page<SavedList> searchSavedLists(SearchSavedListRequest request);

    /**
     * Update the info associated with the SavedList with the given id 
     * - for example changing its name. 
     * @param savedListId ID of saved list to be updated
     * @param request Request containing the new list info
     * @return Updated saved list
     * @throws NoSuchObjectException if there is no saved list with this id. 
     * @throws EntityExistsException if a list with the requested name already exists.
     */
    SavedList updateSavedList(long savedListId, UpdateSavedListInfoRequest request)
            throws NoSuchObjectException, EntityExistsException;


    /**
     * Adds a user who wants to share the given saved list (created by someone 
     * else).
     * @param id id of Saved List being shared
     * @param request Contains the id of the user who is sharing the list
     * @return The updated saved list with a modified collection of users
     * who are sharing it.
     * @throws NoSuchObjectException if there is no saved list with this id
     * or the user is not found.
     */
    SavedList addSharedUser(long id, UpdateSharingRequest request)
            throws NoSuchObjectException;

    /**
     * Removes a user who was sharing the given saved list (created by someone 
     * else).
     * @param id id of Saved List
     * @param request Contains the id of the user who is no longer interested
     *                in sharing the list
     * @return The updated saved list with a modified collection of users
     * who are sharing it.
     * @throws NoSuchObjectException if there is no saved list with this id
     * or the user is not found.
     */
    SavedList removeSharedUser(long id, UpdateSharingRequest request)
            throws NoSuchObjectException;
}
