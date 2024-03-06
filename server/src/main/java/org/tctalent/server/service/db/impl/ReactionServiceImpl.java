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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Reaction;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.ReactionRepository;
import org.tctalent.server.request.chat.reaction.CreateReactionRequest;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.ReactionService;
import org.tctalent.server.service.db.UserService;

/**
 * Manage reactions
 * @author Sam Schlicht
 */

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {
    private final ReactionRepository reactionRepository;
    private final UserService userService;
    private final ChatPostService chatPostService;

    @Override
    public Reaction create(
            long chatPostId, CreateReactionRequest request)
            throws NoSuchObjectException {

        // If user selected emoji matching existing reaction, call update and return updated record.
        Optional<Reaction> matchingReaction =
            reactionRepository.findByEmojiAndChatPostId(request.getEmoji(), chatPostId);

        if (matchingReaction.isPresent()) {
            return updateReaction(matchingReaction.get().getId());
        }

        // Otherwise, create a new reaction for the given emoji.
        Reaction reaction = new Reaction();
        reaction.setUsers(Collections.singleton(userService.getLoggedInUser()));
        reaction.setEmoji(request.getEmoji());
        reaction.setChatPost(chatPostService.getChatPost(chatPostId));

        return reactionRepository.save(reaction);
    }

    @Override
    public boolean delete(long reactionId)
            throws InvalidRequestException {
        reactionRepository.deleteById(reactionId);
        return true;
    }

    @Override
    public @Nullable Reaction updateReaction(long id)
            throws NoSuchObjectException {
        final User loggedInUser = userService.getLoggedInUser();
        Reaction reaction = reactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Reaction.class, id));
        // Check if user was already associated with the reaction
        Set<User> users = reaction.getUsers();
        if(users.contains(loggedInUser)) {
            // Delete the reaction if they were the only associated user
            if(users.size() == 1) {
                delete(id);
                return null;
            } else {
                // Remove them if there are other users associated with it
                users.remove(loggedInUser);
                reaction.setUsers(users);
                return reactionRepository.save(reaction);
            }
        } else {
            // If they were not associated with it already, add them
            users.add(loggedInUser);
            return reactionRepository.save(reaction);
        }
    }

    @Override
    public List<Reaction> list(long chatPostId)
        throws NoSuchObjectException {
        List<Reaction> reactionList =
            reactionRepository.findBychatPostId(chatPostId)
                .orElseThrow(() -> new NoSuchObjectException(Reaction.class, chatPostId));
        return reactionList;
    }

}