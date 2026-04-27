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

import java.util.List;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Reaction;
import org.tctalent.server.request.chat.reaction.AddReactionRequest;

public interface ReactionService {

    /**
     * Adds a new reaction with the emoji in the given request, or calls modify if that emoji
     * was already associated with a reaction.
     * @param chatPostId id of the parent chat post
     * @param request {@link AddReactionRequest} containing the emoji.
     * @return list of reactions belonging to given chat post
     * @throws NoSuchObjectException if post or reaction (if modify called) not found
     * @throws InvalidRequestException if not authorised to delete (if delete method called)
     * @throws EntityExistsException if reaction already exists
     */
    List<Reaction> addReaction(long chatPostId, AddReactionRequest request)
            throws NoSuchObjectException, InvalidRequestException, EntityExistsException;

    /**
     * Modifies the reaction associated with the ID provided:
     * delete the reaction altogether if the only associated user;
     * if an associated user but not the only one, remove them and save the reaction;
     * if not an associated user, add them and save.
     * @param id ID of reaction
     * @return list of reactions belonging to given chat post
     * @throws NoSuchObjectException if the there is no Reaction record with the given ID
     * @throws InvalidRequestException if not authorised to delete (if delete method called)
     */
    List<Reaction> modifyReaction(long id) throws NoSuchObjectException;

}
