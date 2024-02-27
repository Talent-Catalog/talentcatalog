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

import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.ChatPostReaction;
import org.tctalent.server.request.chat.reaction.CreateChatPostReactionRequest;

public interface ChatPostReactionService {
    /**
     * Gets the chat post reaction record from the given id.
     * @param reactionId of chat post reaction
     * @return Desired record
     * TODO: @throws NoSuchObjectException if the there is no reaction record with that id ?
     */
    ChatPostReaction getChatPostReaction(long reactionId) throws NoSuchObjectException;

    // TODO: get reaction(s) by chat post ID

    /**
     * Creates a new chat post reaction from the data in the given request.
     * @param chatPostId id of the parent chat post
     * @param request Request containing visa job check details
     * @return Created record
     * TODO: @throws NoSuchObjectException?
     */
    ChatPostReaction createChatPostReaction(long chatPostId, CreateChatPostReactionRequest request)
            throws NoSuchObjectException;

    /**
     * Delete the chat post reaction with the given id.
     * @param reactionId ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    boolean deleteChatPostReaction(long reactionId)
            throws EntityReferencedException, InvalidRequestException;

    /**
     * Updates the chat post reaction associated with the ID provided.
     * @param reactionId ID of chat post reaction - If null this method does nothing
     * @param request {@link CreateChatPostReactionRequest}
     * TODO: @throws NoSuchObjectException if the there is no ChatPostReaction record with the given ID.
     */
    ChatPostReaction updateChatPostReaction(long reactionId, CreateChatPostReactionRequest request)
            throws NoSuchObjectException;
}
