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

package org.tctalent.server.api.chat;

 import java.util.List;
 import java.util.Map;
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import lombok.RequiredArgsConstructor;
 import org.springframework.messaging.handler.annotation.DestinationVariable;
 import org.springframework.messaging.handler.annotation.MessageMapping;
 import org.springframework.messaging.handler.annotation.SendTo;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.tctalent.server.api.admin.IJoinedTableApi;
 import org.tctalent.server.exception.EntityExistsException;
 import org.tctalent.server.exception.InvalidRequestException;
 import org.tctalent.server.exception.NoSuchObjectException;
 import org.tctalent.server.model.db.Reaction;
 import org.tctalent.server.request.chat.reaction.AddReactionRequest;
 import org.tctalent.server.service.db.ChatPostService;
 import org.tctalent.server.service.db.ReactionService;
 import org.tctalent.server.util.dto.DtoBuilder;

 /**
  * This controller handles websocket connections.
  * <p/>
  * Websocket urls with the /app prefix are directed to websocket controllers such as this one.
  * This is configured in {@link org.tctalent.server.configuration.WebSocketConfig}.
  * <p/>
  * So, for example, {@link #addReaction} below would be executed in response to the url ending in
  * /app/reaction/{chatPostId}/add-reaction/.
  * <p/>
  * This code is modelled on {@link org.tctalent.server.api.chat.ChatPublishApi} from the same package,
  * which is in turn modelled on
  * <a href="https://spring.io/guides/gs/messaging-stomp-websocket/">this example from Spring.io</a>
  * and <a href="https://stackoverflow.com/questions/27047310/path-variables-in-spring-websockets-sendto-mapping/27055764#27055764">
  *     this Stackoverflow post</a>
  *
  */
@Controller
@RequestMapping("/api/admin/reaction")
@RequiredArgsConstructor
public class ReactionPublishApi
    implements IJoinedTableApi<AddReactionRequest, AddReactionRequest, AddReactionRequest> {

    private final ReactionService reactionService;

    /**
     * Adds a reaction record from the data in the given request or modifies an existing one if that
     * emoji is already associated with a reaction on same post. Modify may even delete, if user was
     * the last remaining user associated with the existing reaction.
     * <p/>
     * Multicasts the updated reactions to all clients subscribed to the post.
     *
     * @param request Request containing emoji
     * @param postId the ID of the chat post where the reaction was made
     * @return updated reactions list for the parent chat post, sent to all subscribers
     * @throws EntityExistsException if reaction already exists
     * @throws NoSuchObjectException if post or reaction (if update called) not found
     * @throws InvalidRequestException if not authorised to delete (if delete method called)
     */
    @MessageMapping("/reaction/{postId}/add")
    @SendTo(ChatPostService.REACTION_PUBLISH_ROOT + "/{postId}")
    public @NotNull List<Map<String, Object>> addReaction(
        @DestinationVariable Long postId, @Valid AddReactionRequest request)
            throws NoSuchObjectException, InvalidRequestException, EntityExistsException {
        List<Reaction> reactions = this.reactionService.addReaction(postId, request);
        return reactionDto().buildList(reactions);
    }

  /**
   * Modifies an existing user reaction, adding or removing a user (depending on whether they were
   * already associated with it) or calling delete if they were the last associated user.
   * @param reactionId of the reaction to be modified
   * @param postId the ID of the chat post where the reaction was modified
   * @return modified reactions list for the parent chat post
   * @throws NoSuchObjectException if the there is no Reaction record with the given ID
   * @throws InvalidRequestException if not authorised to delete (if delete method called)
   */
    @MessageMapping("/reaction/{postId}/modify/{reactionId}")
    @SendTo(ChatPostService.REACTION_PUBLISH_ROOT + "/{postId}")
    public @NotNull List<Map<String, Object>> modifyReaction(
        @DestinationVariable Long postId, @DestinationVariable Long reactionId)
            throws NoSuchObjectException, InvalidRequestException {
        List<Reaction> reactions = reactionService.modifyReaction(reactionId);
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
