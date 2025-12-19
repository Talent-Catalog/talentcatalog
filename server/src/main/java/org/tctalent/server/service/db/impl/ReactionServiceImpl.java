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

package org.tctalent.server.service.db.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Reaction;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.ReactionRepository;
import org.tctalent.server.request.chat.reaction.AddReactionRequest;
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
    public List<Reaction> addReaction(long chatPostId, AddReactionRequest request)
            throws NoSuchObjectException, InvalidRequestException, EntityExistsException {

        // If user selected emoji matching existing reaction, call modify and return modified record.
        Optional<Reaction> matchingReaction =
            reactionRepository.findByEmojiAndChatPostId(request.getEmoji(), chatPostId);

        if (matchingReaction.isPresent()) {
            modifyReaction(matchingReaction.get().getId());
        } else {
            // Otherwise, create a new reaction for the given emoji.
            Reaction reaction = new Reaction();
            reaction.setUsers(Collections.singleton(userService.getLoggedInUser()));
            reaction.setEmoji(request.getEmoji());
            reaction.setChatPost(chatPostService.getChatPost(chatPostId));

            reactionRepository.save(reaction);
        }
        return listReactions(chatPostId);
    }

    /**
     * Deletes the reaction with the given id.
     * @param id ID of record to be deleted
     * @throws InvalidRequestException if not authorized to delete this reaction
     */
    private void deleteReaction(long id)
            throws InvalidRequestException {
        reactionRepository.deleteById(id);
    }

    @Override
    public List<Reaction> modifyReaction(long id)
            throws NoSuchObjectException, InvalidRequestException {
        final User loggedInUser = userService.getLoggedInUser();
        final Reaction reaction = reactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchObjectException(Reaction.class, id));

        // Check if user already associated with the reaction
        Set<User> users = reaction.getUsers();
        if(users.contains(loggedInUser)) {
            // Delete the reaction if they were the only associated user
            if(users.size() == 1) {
                deleteReaction(id);
            } else {
                // Remove them if there are other users associated with it
                users.remove(loggedInUser);
                reaction.setUsers(users);
                reactionRepository.save(reaction);
            }
        } else {
            // If they were not associated with it already, add them
            users.add(loggedInUser);
            reactionRepository.save(reaction);
        }

        return listReactions(reaction.getChatPost().getId());
    }

    /**
     * Provides a list of the reactions associated with a given post.
     * @param chatPostId id of the chat post being queried
     * @return list of chat posts associated reactions
     * @throws NoSuchObjectException if chat post not found
     */
    private List<Reaction> listReactions(long chatPostId)
        throws NoSuchObjectException {
        List<Reaction> reactionList =
            reactionRepository.findBychatPostId(chatPostId)
                .orElseThrow(() -> new NoSuchObjectException(Reaction.class, chatPostId));
        return reactionList;
    }

}
