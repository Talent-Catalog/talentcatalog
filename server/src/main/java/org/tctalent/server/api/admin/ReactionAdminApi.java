 /*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.api.admin;

 import java.util.List;
 import java.util.Map;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import lombok.RequiredArgsConstructor;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.PutMapping;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;
 import org.tctalent.server.exception.EntityExistsException;
 import org.tctalent.server.exception.EntityReferencedException;
 import org.tctalent.server.exception.InvalidRequestException;
 import org.tctalent.server.exception.NoSuchObjectException;
 import org.tctalent.server.model.db.Reaction;
 import org.tctalent.server.request.chat.reaction.CreateReactionRequest;
 import org.tctalent.server.service.db.ReactionService;
 import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/reaction")
@RequiredArgsConstructor
public class ReactionAdminApi
        implements IJoinedTableApi<
        CreateReactionRequest, CreateReactionRequest, CreateReactionRequest> {

    private final ReactionService reactionService;

    /**
     * Creates a reaction record from the data in the given request or calls update if that emoji
     * is already associated with a reaction on this post. Update may in turn even call delete, if
     * that user was the last remaining user associated with the existing reaction.
     * @param request Request containing details
     * @return Created or updated record, null if an existing record was deleted
     */
    @Override
    public @NotNull Map<String, Object> create(
            long chatPostId, @Valid CreateReactionRequest request)
            throws EntityExistsException {
        Reaction reaction = this.reactionService.create(chatPostId, request);
        return reactionDto().build(reaction);
    }

    /**
     * Deletes the reaction with the given id.
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this record.
     */
    @Override
    public boolean delete(long id)
            throws EntityReferencedException, InvalidRequestException {
        return reactionService.delete(id);
    }

  /**
   * Updates an existing user reaction, adding or removing a user (depending whether they were
   * already associated with it) or calling delete on it if they were the last associated user.
   * @param id of the reaction to be updated
   * @return updated reaction or null if it was deleted
   * @throws NoSuchObjectException if the reaction doesn't exist
   */
    @PutMapping("{id}/update-reaction")
    public @NotNull Map<String, Object> updateReaction(
        @PathVariable("id") long id)
            throws NoSuchObjectException {
        Reaction reaction = reactionService.updateReaction(id);
        return reactionDto().build(reaction);
    }

  /**
   * Returns a list containing the reactions associated with a given chat post.
   * @param chatPostId ID of parent record
   * @return list of chat post's reactions, empty if there are none
   * @throws NoSuchObjectException if the chat post doesn't exist
   */
    public List<Map<String, Object>> list(long chatPostId)
        throws NoSuchObjectException {
        List<Reaction> reactionList = reactionService.list(chatPostId);
        return reactionDto().buildList(reactionList);
    }

    private DtoBuilder reactionDto() {
        return new DtoBuilder()
                .add("id")
                .add("emoji")
                .add("users", userDto())
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
            .add("displayName")
            ;
    }

}
