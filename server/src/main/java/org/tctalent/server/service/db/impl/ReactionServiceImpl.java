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
import org.tctalent.server.exception.EntityReferencedException;
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
    public Reaction get(long reactionId)
            throws NoSuchObjectException {

        return reactionRepository.findById(reactionId)
                .orElseThrow(() -> new NoSuchObjectException(Reaction.class, reactionId));
    }

    @Override
    public Reaction create(
            long chatPostId, CreateReactionRequest request)
            throws NoSuchObjectException {

        // If user selected emoji matching existing reaction, divert to update()
        Optional<Reaction> matchingReaction =
            reactionRepository.findByEmojiAndChatPostId(request.getEmoji(), chatPostId);

        if (matchingReaction.isPresent()) {
            return updateReaction(matchingReaction.get().getId());
        }

        // Otherwise, create a new reaction for the given emoji
        Reaction reaction = new Reaction();
        reaction.setUsers(Collections.singleton(userService.getLoggedInUser()));
        reaction.setEmoji(request.getEmoji());
        reaction.setChatPost(chatPostService.getChatPost(chatPostId));

        return reactionRepository.save(reaction);
    }

    @Override
    public boolean delete(long reactionId)
            throws EntityReferencedException, InvalidRequestException {
        reactionRepository.deleteById(reactionId);
        return true;
    }

    @Override
    public @Nullable Reaction updateReaction(long id)
            throws NoSuchObjectException {
        final User loggedInUser = userService.getLoggedInUser();
        Reaction reaction = reactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Reaction.class, id));
        Set<User> users = reaction.getUsers();
        if(users.contains(loggedInUser)) {
            if(users.size() == 1) {
                delete(id);
                return null;
            } else {
                users.remove(loggedInUser);
                reaction.setUsers(users);
                return reactionRepository.save(reaction);
            }
        } else {
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
    };

}
