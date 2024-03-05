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
     * Creates a reaction record from the data in the given request.
     * @param request Request containing details
     * @return Created record
     */
    @Override
    public @NotNull Map<String, Object> create(
            long chatPostId, @Valid CreateReactionRequest request)
            throws EntityExistsException {
        Reaction reaction = this.reactionService.create(chatPostId, request);
        return reactionDto().build(reaction);
    }

    /**
     * Delete the reaction with the given id.
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
     * Gets reaction record using the given ID.
     * @param id ID of reaction
     * @return Desired record
     * @throws NoSuchObjectException if if the there is no record with that id
     * TODO:maybe not needed
     */
    @Override
    public @NotNull Map<String, Object> get(long id)
            throws NoSuchObjectException {
        Reaction reaction = this.reactionService.get(id);
        return reactionDto().build(reaction);
    }

    @PutMapping("{id}/update-reaction")
    public @NotNull Map<String, Object> updateReaction(
        @PathVariable("id") long id)
            throws NoSuchObjectException {
        Reaction reaction = reactionService.updateReaction(id);
        return reactionDto().build(reaction);
    }

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
