/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.Reaction;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.ReactionRepository;
import org.tctalent.server.request.chat.reaction.AddReactionRequest;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.UserService;

@ExtendWith(MockitoExtension.class)
class ReactionServiceImplTest {

  private static final long CHAT_POST_ID = 25L;
  private static final long REACTION_ID = 10L;
  private static final String EMOJI = "👍";

  @Mock
  private ReactionRepository reactionRepository;

  @Mock
  private UserService userService;

  @Mock
  private ChatPostService chatPostService;

  @Mock
  private AddReactionRequest request;

  @Mock
  private User loggedInUser;

  @Mock
  private ChatPost chatPost;

  private ReactionServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new ReactionServiceImpl(
        reactionRepository,
        userService,
        chatPostService
    );
  }

  @Test
  void addReactionCreatesNewReactionWhenEmojiDoesNotExist()
      throws Exception {

    List<Reaction> expectedReactions = new ArrayList<>();

    when(request.getEmoji()).thenReturn(EMOJI);
    when(reactionRepository.findByEmojiAndChatPostId(
        EMOJI,
        CHAT_POST_ID
    )).thenReturn(Optional.empty());
    when(userService.getLoggedInUser()).thenReturn(loggedInUser);
    when(chatPostService.getChatPost(CHAT_POST_ID))
        .thenReturn(chatPost);
    when(reactionRepository.findBychatPostId(CHAT_POST_ID))
        .thenReturn(Optional.of(expectedReactions));

    List<Reaction> result =
        service.addReaction(CHAT_POST_ID, request);

    ArgumentCaptor<Reaction> reactionCaptor =
        ArgumentCaptor.forClass(Reaction.class);

    verify(reactionRepository).save(reactionCaptor.capture());

    Reaction savedReaction = reactionCaptor.getValue();

    assertAll(
        () -> assertSame(expectedReactions, result),
        () -> assertEquals(EMOJI, savedReaction.getEmoji()),
        () -> assertSame(chatPost, savedReaction.getChatPost()),
        () -> assertEquals(1, savedReaction.getUsers().size()),
        () -> assertTrue(
            savedReaction.getUsers().contains(loggedInUser)
        )
    );
  }

  @Test
  void addReactionModifiesExistingMatchingReaction()
      throws Exception {

    Reaction reaction = org.mockito.Mockito.mock(Reaction.class);
    when(reaction.getId()).thenReturn(REACTION_ID);

    Set<User> users = new HashSet<>();
    when(reaction.getUsers()).thenReturn(users);
    when(reaction.getChatPost()).thenReturn(chatPost);
    when(chatPost.getId()).thenReturn(CHAT_POST_ID);

    List<Reaction> expectedReactions = List.of(reaction);

    when(request.getEmoji()).thenReturn(EMOJI);
    when(reactionRepository.findByEmojiAndChatPostId(
        EMOJI,
        CHAT_POST_ID
    )).thenReturn(Optional.of(reaction));
    when(reactionRepository.findById(REACTION_ID))
        .thenReturn(Optional.of(reaction));
    when(userService.getLoggedInUser()).thenReturn(loggedInUser);
    when(reactionRepository.findBychatPostId(CHAT_POST_ID))
        .thenReturn(Optional.of(expectedReactions));

    List<Reaction> result =
        service.addReaction(CHAT_POST_ID, request);

    assertAll(
        () -> assertSame(expectedReactions, result),
        () -> assertTrue(users.contains(loggedInUser))
    );

    verify(reactionRepository).save(reaction);

    // modifyReaction lists the reactions once, and addReaction lists
    // them again before returning.
    verify(reactionRepository, times(2))
        .findBychatPostId(CHAT_POST_ID);
  }

  @Test
  void modifyReactionDeletesReactionWhenUserIsOnlyMember()
      throws Exception {

    Reaction reaction = org.mockito.Mockito.mock(Reaction.class);
    Set<User> users = new HashSet<>();
    users.add(loggedInUser);

    when(userService.getLoggedInUser()).thenReturn(loggedInUser);
    when(reactionRepository.findById(REACTION_ID))
        .thenReturn(Optional.of(reaction));
    when(reaction.getUsers()).thenReturn(users);
    when(reaction.getChatPost()).thenReturn(chatPost);
    when(chatPost.getId()).thenReturn(CHAT_POST_ID);

    List<Reaction> expectedReactions = List.of();
    when(reactionRepository.findBychatPostId(CHAT_POST_ID))
        .thenReturn(Optional.of(expectedReactions));

    List<Reaction> result =
        service.modifyReaction(REACTION_ID);

    assertSame(expectedReactions, result);

    verify(reactionRepository).deleteById(REACTION_ID);
    verify(reactionRepository, never()).save(reaction);
  }

  @Test
  void modifyReactionRemovesUserWhenOtherUsersRemain()
      throws Exception {

    Reaction reaction = org.mockito.Mockito.mock(Reaction.class);
    User otherUser = org.mockito.Mockito.mock(User.class);

    Set<User> users = new HashSet<>();
    users.add(loggedInUser);
    users.add(otherUser);

    when(userService.getLoggedInUser()).thenReturn(loggedInUser);
    when(reactionRepository.findById(REACTION_ID))
        .thenReturn(Optional.of(reaction));
    when(reaction.getUsers()).thenReturn(users);
    when(reaction.getChatPost()).thenReturn(chatPost);
    when(chatPost.getId()).thenReturn(CHAT_POST_ID);

    List<Reaction> expectedReactions = List.of(reaction);
    when(reactionRepository.findBychatPostId(CHAT_POST_ID))
        .thenReturn(Optional.of(expectedReactions));

    List<Reaction> result =
        service.modifyReaction(REACTION_ID);

    assertAll(
        () -> assertSame(expectedReactions, result),
        () -> assertFalse(users.contains(loggedInUser)),
        () -> assertTrue(users.contains(otherUser)),
        () -> assertEquals(1, users.size())
    );

    verify(reaction).setUsers(users);
    verify(reactionRepository).save(reaction);
    verify(reactionRepository, never()).deleteById(REACTION_ID);
  }

  @Test
  void modifyReactionAddsUserWhenNotAlreadyAssociated()
      throws Exception {

    Reaction reaction = org.mockito.Mockito.mock(Reaction.class);
    User otherUser = org.mockito.Mockito.mock(User.class);

    Set<User> users = new HashSet<>();
    users.add(otherUser);

    when(userService.getLoggedInUser()).thenReturn(loggedInUser);
    when(reactionRepository.findById(REACTION_ID))
        .thenReturn(Optional.of(reaction));
    when(reaction.getUsers()).thenReturn(users);
    when(reaction.getChatPost()).thenReturn(chatPost);
    when(chatPost.getId()).thenReturn(CHAT_POST_ID);

    List<Reaction> expectedReactions = List.of(reaction);
    when(reactionRepository.findBychatPostId(CHAT_POST_ID))
        .thenReturn(Optional.of(expectedReactions));

    List<Reaction> result =
        service.modifyReaction(REACTION_ID);

    assertAll(
        () -> assertSame(expectedReactions, result),
        () -> assertTrue(users.contains(loggedInUser)),
        () -> assertTrue(users.contains(otherUser)),
        () -> assertEquals(2, users.size())
    );

    verify(reactionRepository).save(reaction);
    verify(reactionRepository, never()).deleteById(REACTION_ID);
  }

  @Test
  void modifyReactionThrowsWhenReactionDoesNotExist() {
    when(userService.getLoggedInUser()).thenReturn(loggedInUser);
    when(reactionRepository.findById(REACTION_ID))
        .thenReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> service.modifyReaction(REACTION_ID)
    );

    verifyNoInteractions(chatPostService);
    verify(reactionRepository, never()).save(
        org.mockito.ArgumentMatchers.any()
    );
    verify(reactionRepository, never()).deleteById(REACTION_ID);
  }

  @Test
  void addReactionThrowsWhenReactionListCannotBeFound()
      throws Exception {

    when(request.getEmoji()).thenReturn(EMOJI);
    when(reactionRepository.findByEmojiAndChatPostId(
        EMOJI,
        CHAT_POST_ID
    )).thenReturn(Optional.empty());
    when(userService.getLoggedInUser()).thenReturn(loggedInUser);
    when(chatPostService.getChatPost(CHAT_POST_ID))
        .thenReturn(chatPost);
    when(reactionRepository.findBychatPostId(CHAT_POST_ID))
        .thenReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> service.addReaction(CHAT_POST_ID, request)
    );

    verify(reactionRepository).save(
        org.mockito.ArgumentMatchers.any(Reaction.class)
    );
  }
}