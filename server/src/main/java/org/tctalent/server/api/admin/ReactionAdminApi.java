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

package org.tctalent.server.api.admin;

 import java.util.List;
 import java.util.Map;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import lombok.RequiredArgsConstructor;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.PutMapping;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;
 import org.tctalent.server.exception.EntityExistsException;
 import org.tctalent.server.exception.InvalidRequestException;
 import org.tctalent.server.exception.NoSuchObjectException;
 import org.tctalent.server.model.db.Reaction;
 import org.tctalent.server.request.chat.reaction.CreateReactionRequest;
 import org.tctalent.server.service.db.ReactionService;
 import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/reaction")
@RequiredArgsConstructor
public class ReactionAdminApi {

    private final ReactionService reactionService;

    /**
     * Creates a reaction record from the data in the given request or calls update if that emoji
     * is already associated with a reaction on this post. Update may in turn even call delete, if
     * that user was the last remaining user associated with the existing reaction.
     * @param request Request containing details
     * @return updated reactions list
     * @throws EntityExistsException if reaction already exists
     * @throws NoSuchObjectException if post or reaction (if update called) not found
     * @throws InvalidRequestException if not authorised to delete (if delete method called)
     */
    @PostMapping("{id}")
    public @NotNull List<Map<String, Object>> createReaction(
          @PathVariable("id") long chatPostId, @Valid @RequestBody CreateReactionRequest request)
            throws NoSuchObjectException, InvalidRequestException, EntityExistsException {
        List<Reaction> reactions = this.reactionService.createReaction(chatPostId, request);
        return reactionDto().buildList(reactions);
    }

  /**
   * Updates an existing user reaction, adding or removing a user (depending on whether they were
   * already associated with it) or calling delete if they were the last associated user.
   * @param id of the reaction to be updated
   * @return updated reactions list
   * @throws NoSuchObjectException if the there is no Reaction record with the given ID
   * @throws InvalidRequestException if not authorised to delete (if delete method called)
   */
    @PutMapping("{id}/update-reaction")
    public @NotNull List<Map<String, Object>> updateReaction(
        @PathVariable("id") long id)
            throws NoSuchObjectException, InvalidRequestException {
        List<Reaction> reactions = reactionService.updateReaction(id);
        return reactionDto().buildList(reactions);
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
            .add("id")
            .add("displayName")
            ;
    }

}
