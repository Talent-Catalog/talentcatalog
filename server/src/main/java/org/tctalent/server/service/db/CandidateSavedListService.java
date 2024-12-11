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

import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.exception.UnauthorisedActionException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.request.candidate.IHasSetOfSavedLists;
import org.tctalent.server.request.candidate.UpdateCandidateContextNoteRequest;
import org.tctalent.server.request.candidate.UpdateCandidateShareableDocsRequest;
import org.tctalent.server.request.candidate.source.CopySourceContentsRequest;
import org.tctalent.server.request.list.UpdateExplicitSavedListContentsRequest;

/**
 * Handle anything to do with deletion of candidate savedList relationships.
 * <p/>
 * Relying on Cascading from SavedList or Candidate candidateSavedList
 * collections doesn't work.
 * See doc on SavedList and Candidate where candidateSavedList is declared
 */
public interface CandidateSavedListService {

    /**
     * Removes the given candidate from all its lists
     * @param candidate Candidate whose lists are being cleared
     */
    void clearCandidateSavedLists(Candidate candidate);

    /**
     * Remove the given candidate from all its lists
     * @param candidateId ID of candidate
     * @return False if no candidate with that id was found, otherwise true.
     */
    boolean clearCandidateSavedLists(long candidateId);

    /**
     * Removes all candidates from the given list
     * @param savedList List whose contents are being cleared
     */
    void clearSavedListCandidates(SavedList savedList);

    /**
     * Clear the contents of the SavedList with the given ID
     * @param savedListId ID of SavedList to clear
     * @return False if no saved list with that id was found, otherwise true.
     */
    boolean clearSavedList(long savedListId) throws InvalidRequestException;

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
    SavedList copy(long id, CopySourceContentsRequest request)
        throws EntityExistsException, NoSuchObjectException;

    /**
     * Copies the given list to the list specified in the given request (which
     * may be a requested new list).
     * @param sourceList List to be copied
     * @param request Defines the target list and also whether copy is a
     *                replace or an add.
     * @return The target list
     * @throws EntityExistsException If a new list needs to be created but the
     * list name already exists.
     * @throws NoSuchObjectException if there is no saved list matching the target list id.
     */
    SavedList copy(SavedList sourceList, CopySourceContentsRequest request)
        throws EntityExistsException, NoSuchObjectException;

    /**
     * Copies the contents (candidates plus any context notes) from the
     * source list to the destination.
     * Note that other list info (eg name, sfJoblink and other attributes are
     * not copied).
     * @param source List to copy from
     * @param destination List to copy to
     */
    void copyContents(SavedList source, SavedList destination, boolean replace);

    /**
     * Copies the contents (candidates plus any context notes) from the
     * candidates specified in the request to the destination.
     * @param request Contains the candidates to be copied including where those
     *                candidates came from. Also may contain updateStatusInfo.
     * @param destination List to copy to
     */
    void copyContents(UpdateExplicitSavedListContentsRequest request, SavedList destination);

    /**
     * Delete the SavedList with the given ID
     * @param savedListId ID of SavedList to delete
     * @return True if delete was successful
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    boolean deleteSavedList(long savedListId) throws InvalidRequestException;

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

    /**
     * Updates a candidate context note associated with the given saved list.
     * @param savedListId Id of saved list
     * @param request Request containing the candidate id and the context note
     *                text
     */
    void updateCandidateContextNote(
            long savedListId, UpdateCandidateContextNoteRequest request);

    /**
     * Update a candidate's shareable docs according to the given request.
     * @param id Id of candidate
     * @param request Request specific the docs to be shared.
     * @return Updated candidate object
     * @throws UnauthorisedActionException if there was a problem changing the sharing permissions
     * of the docs to be shared.
     */
    Candidate updateShareableDocs(long id, UpdateCandidateShareableDocsRequest request)
        throws UnauthorisedActionException;

    void updateCandidateShareableDocs(long candidateId,
            long savedListId, CandidateAttachment cv, CandidateAttachment doc);
}
