/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.request.list.SearchSavedListRequest;
import org.tbbtalent.server.request.list.UpdateSavedListRequest;

/**
 * Saved List Service
 *
 * @author John Cameron
 */
public interface SavedListService {

    /**
     * Create a new SavedList 
     * @param request Create request
     * @return Created saved list
     */
    SavedList createSavedList(UpdateSavedListRequest request);

    /**
     * Merge the contents of the SavedList with the given id with the 
     * candidates indicated in the given request.
     * @param savedListId ID of saved list to be updated
     * @param request Request containing the contents to be merged into the list
     * @return Updated saved list
     */
    SavedList mergeSavedList(long savedListId, UpdateSavedListRequest request);

    /**
     * Replace the contents of the SavedList with the given id with the 
     * candidates indicated in the given request
     * @param savedListId ID of saved list to be updated
     * @param request Request containing the new list contents
     * @return Updated saved list
     */
    SavedList replaceSavedList(long savedListId, UpdateSavedListRequest request);

    /**
     * Delete the SavedList with the given ID
     * @param savedListId ID of SavedList to delete
     * @return True if delete was successful
     */
    boolean deleteSavedList(long savedListId);

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
     * Return a page of SavedList's that match the given request, ordered by
     * name.
     * <p/>
     * See also {@link #listSavedLists} which does the same except it
     * returns all matching results.
     * @param request Defines which SavedList's to return
     * @return Matching SavedList's
     */
    Page<SavedList> searchSavedLists(SearchSavedListRequest request);
}
