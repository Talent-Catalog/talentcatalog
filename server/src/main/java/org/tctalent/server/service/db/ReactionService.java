/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

import java.util.List;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Reaction;
import org.tctalent.server.request.chat.reaction.CreateReactionRequest;

public interface ReactionService {

    /**
     * Creates a new reaction from the emoji in the given request, or calls update if that emoji
     * was already associated with a reaction.
     * @param chatPostId id of the parent chat post
     * @param request {@link CreateReactionRequest} containing the emoji.
     * @return the created or updated record, or null if a record was deleted
     * @throws NoSuchObjectException if no chat post exists with the given id
     */
    Reaction create(long chatPostId, CreateReactionRequest request)
            throws NoSuchObjectException;

    /**
     * Deletes the reaction with the given id.
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    boolean delete(long id) throws InvalidRequestException;

    /**
     * Updates the reaction associated with the ID provided:
     * delete the reaction altogether if the only associated user;
     * if an associated user but not the only one, remove them and save the reaction;
     * if not an associated user, add them and save.
     * @param id ID of reaction
     * @return reaction if updated, null if deleted
     * @throws NoSuchObjectException if the there is no Reaction record with the given ID.
     */
    Reaction updateReaction(long id) throws NoSuchObjectException;

    /**
     * Provides a list of the reactions associated with a given post.
     * @param chatPostId id of the chat post being queried
     * @return list
     * @throws NoSuchObjectException if chat post not found
     */
    List<Reaction> list(long chatPostId) throws NoSuchObjectException;
}
