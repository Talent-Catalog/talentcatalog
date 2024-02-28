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

package org.tctalent.server.service.db.impl;

import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.ChatPostReaction;
import org.tctalent.server.repository.db.ChatPostReactionRepository;
import org.tctalent.server.repository.db.ChatPostRepository;
import org.tctalent.server.request.chat.reaction.CreateChatPostReactionRequest;
import org.tctalent.server.service.db.ChatPostReactionService;

/**
 * Manage chat post reactions
 * @author Sam Schlicht
 */

@Service
@RequiredArgsConstructor
public class ChatPostReactionImpl implements ChatPostReactionService {
    private final ChatPostReactionRepository chatPostReactionRepository;
    private final ChatPostRepository chatPostRepository;

    @Override
    public ChatPostReaction getChatPostReaction(long reactionId)
            throws NoSuchObjectException {

        return chatPostReactionRepository.findById(reactionId)
                .orElseThrow(() -> new NoSuchObjectException(ChatPostReaction.class, reactionId));
    }

    @Override
    public ChatPostReaction createChatPostReaction(
            long chatPostId, CreateChatPostReactionRequest request)
            throws NoSuchObjectException {

        ChatPost chatPost = chatPostRepository.findById(chatPostId)
                .orElseThrow(() -> new NoSuchObjectException(ChatPost.class, chatPostId));

        ChatPostReaction reaction = new ChatPostReaction();
        reaction.setChatPost(chatPost);
        reaction.setUserIds(request.getUserIds());
        reaction.setEmoji(request.getEmoji());

        return chatPostReactionRepository.save(reaction);
    }

    @Override
    public boolean deleteChatPostReaction(long reactionId)
            throws EntityReferencedException, InvalidRequestException {
        chatPostReactionRepository.deleteById(reactionId);
        return true;
    }

    @Override
    public @Nullable ChatPostReaction updateChatPostReaction(
            long reactionId, CreateChatPostReactionRequest request)
        throws NoSuchObjectException {
        ChatPostReaction reaction = chatPostReactionRepository.findById(reactionId)
                .orElseThrow(() -> new NoSuchObjectException(ChatPostReaction.class, reactionId));

        // If user clicks on reaction to retract the only upvote, delete it
        if(null == request.getUserIds()) {
            deleteChatPostReaction(reaction.getId());
            return null;
        } else {
            reaction.setUserIds(request.getUserIds());
            chatPostReactionRepository.save(reaction);
            return reaction;
        }
    }

}
