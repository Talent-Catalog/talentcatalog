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

package org.tctalent.server.api.chat;

 import jakarta.validation.Valid;
 import lombok.RequiredArgsConstructor;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.PostMapping;
 import org.springframework.web.bind.annotation.PutMapping;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;
 import org.tctalent.server.api.admin.IJoinedTableApi;
 import org.tctalent.server.exception.EntityExistsException;
 import org.tctalent.server.exception.InvalidRequestException;
 import org.tctalent.server.exception.NoSuchObjectException;
 import org.tctalent.server.model.db.ChatPost;
 import org.tctalent.server.request.chat.reaction.AddReactionRequest;
 import org.tctalent.server.service.db.ChatPostService;
 import org.tctalent.server.service.db.ReactionService;

@RestController
@RequestMapping("/api/admin/reaction")
@RequiredArgsConstructor
public class ReactionAdminApi
    implements IJoinedTableApi<AddReactionRequest, AddReactionRequest, AddReactionRequest> {

    private final ReactionService reactionService;
    private final ChatPostService chatPostService;

    /**
     * Adds a reaction record from the data in the given request or modifies an existing one if that
     * emoji is already associated with a reaction on same post. Modify may even delete, if user was
     * the last remaining user associated with the existing reaction.
     * <p/>
     * The updated post is published to all subscribers via {@link ChatPostService}
     *
     * @param request Request containing emoji
     * @param chatPostId ID of the post to add the reaction to
     * @throws EntityExistsException if reaction already exists
     * @throws NoSuchObjectException if post or reaction (if update called) not found
     * @throws InvalidRequestException if not authorised to delete (if delete method called)
     */
    @PostMapping("{chatPostId}/add-reaction")
    public void addReaction(
          @PathVariable("chatPostId") long chatPostId, @Valid @RequestBody AddReactionRequest request)
            throws NoSuchObjectException, InvalidRequestException, EntityExistsException {
      reactionService.addReaction(chatPostId, request);
      ChatPost post = chatPostService.getChatPost(chatPostId);
      chatPostService.publishChatPost(post);
    }

  /**
   * Modifies an existing user reaction, adding or removing a user (depending on whether they were
   * already associated with it) or calling delete if they were the last associated user.
   * <p/>
   * The updated post is published to all subscribers via {@link ChatPostService}
   *
   * @param reactionId id of the reaction to be modified
   * @param chatPostId ID of the post to modify the reaction on
   * @throws NoSuchObjectException if the there is no Reaction record with the given ID
   * @throws InvalidRequestException if not authorised to delete (if delete method called)
   */
    @PutMapping("{chatPostId}/modify-reaction/{reactionId}")
    public void modifyReaction(
        @PathVariable("chatPostId") long chatPostId, @PathVariable("reactionId") long reactionId)
            throws NoSuchObjectException, InvalidRequestException {
        reactionService.modifyReaction(reactionId);
        ChatPost post = chatPostService.getChatPost(chatPostId);
        chatPostService.publishChatPost(post);
    }

}
