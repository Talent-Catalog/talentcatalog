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

import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.ChatPostReaction;
import org.tctalent.server.request.chat.reaction.CreateChatPostReactionRequest;
import org.tctalent.server.service.db.ChatPostReactionService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/chat-post-reaction")
@RequiredArgsConstructor
public class ChatPostReactionAdminApi
        implements IJoinedTableApi<
        CreateChatPostReactionRequest, CreateChatPostReactionRequest, CreateChatPostReactionRequest> {

    private final ChatPostReactionService chatPostReactionService;

    /**
     * Creates a new chat post reaction record from the data in the given request.
     * @param request Request containing details
     * @return Created record
     */
    @Override
    public @NotNull Map<String, Object> create(
            long chatPostId, @Valid CreateChatPostReactionRequest request)
            throws EntityExistsException {
        ChatPostReaction reaction =
                this.chatPostReactionService.createChatPostReaction(chatPostId, request);
        return chatPostReactionDto().build(reaction);
    }

    /**
     * Delete the chat post reaction with the given id.
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this record.
     */
    @Override
    public boolean delete(long id)
            throws EntityReferencedException, InvalidRequestException {
        return chatPostReactionService.deleteChatPostReaction(id);
    }

    /**
     * Gets chat post reaction record using the given ID.
     * @param id ID of chat post reaction
     * @return Desired record
     * @throws NoSuchObjectException if if the there is no record with that id
     * TODO: may not need this one
     */
    @Override
    public @NotNull Map<String, Object> get(long id)
            throws NoSuchObjectException {
        ChatPostReaction chatPostReaction =
                this.chatPostReactionService.getChatPostReaction(id);
        return chatPostReactionDto().build(chatPostReaction);
    }

    //TODO: doc
    @Override
    public @NotNull Map<String, Object> update(long id, CreateChatPostReactionRequest request)
            throws NoSuchObjectException {
        ChatPostReaction reaction = chatPostReactionService.updateChatPostReaction(id, request);
        return chatPostReactionDto().build(reaction);
    }

    private DtoBuilder chatPostReactionDto() {
        return new DtoBuilder()
                .add("id")
                .add("emoji")
                .add("userIds")
                ;
    }

}
