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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatUser;
import org.tctalent.server.model.db.JobChatUserInfo;
import org.tctalent.server.model.db.JobChatUserKey;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.JobChatUserRepository;
import org.tctalent.server.service.db.ChatPostService;

@ExtendWith(MockitoExtension.class)
class JobChatUserServiceImplTest {

  private static final long CHAT_ID = 10L;
  private static final long USER_ID = 20L;

  private static final JobChatUserKey KEY =
      new JobChatUserKey(CHAT_ID, USER_ID);

  @Mock
  private JobChatUserRepository jobChatUserRepository;

  @Mock
  private ChatPostService chatPostService;

  @Mock
  private JobChat chat;

  @Mock
  private User user;

  @Mock
  private ChatPost lastPost;

  @Mock
  private ChatPost lastReadPost;

  private JobChatUserServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new JobChatUserServiceImpl(
        jobChatUserRepository,
        chatPostService
    );

    when(chat.getId()).thenReturn(CHAT_ID);
    when(user.getId()).thenReturn(USER_ID);
  }

  @Test
  void getJobChatUserInfoReturnsNullIdsWhenChatHasNoPostsOrReadRecord() {
    when(jobChatUserRepository.findById(KEY))
        .thenReturn(Optional.empty());
    when(chatPostService.getLastChatPost(CHAT_ID))
        .thenReturn(null);

    JobChatUserInfo result =
        service.getJobChatUserInfo(chat, user);

    assertAll(
        () -> assertNull(result.getLastPostId()),
        () -> assertNull(result.getLastReadPostId()),
        () -> assertNull(result.getNumberUnreadChats())
    );
  }

  @Test
  void getJobChatUserInfoReturnsLastPostButNoLastReadPost() {
    JobChatUser jobChatUser = new JobChatUser();
    jobChatUser.setLastReadPost(null);

    when(jobChatUserRepository.findById(KEY))
        .thenReturn(Optional.of(jobChatUser));
    when(chatPostService.getLastChatPost(CHAT_ID))
        .thenReturn(lastPost);
    when(lastPost.getId()).thenReturn(100L);

    JobChatUserInfo result =
        service.getJobChatUserInfo(chat, user);

    assertAll(
        () -> assertEquals(100L, (long) result.getLastPostId()),
        () -> assertNull(result.getLastReadPostId())
    );
  }

  @Test
  void getJobChatUserInfoReturnsLastPostAndLastReadPost() {
    JobChatUser jobChatUser = new JobChatUser();
    jobChatUser.setLastReadPost(lastReadPost);

    when(jobChatUserRepository.findById(KEY))
        .thenReturn(Optional.of(jobChatUser));
    when(chatPostService.getLastChatPost(CHAT_ID))
        .thenReturn(lastPost);
    when(lastPost.getId()).thenReturn(100L);
    when(lastReadPost.getId()).thenReturn(90L);

    JobChatUserInfo result =
        service.getJobChatUserInfo(chat, user);

    assertAll(
        () -> assertEquals(100L, (long) result.getLastPostId()),
        () -> assertEquals(90L, (long) result.getLastReadPostId())
    );
  }

  @Test
  void isChatReadByUserReturnsTrueWhenChatHasNoPosts() {
    when(jobChatUserRepository.findById(KEY))
        .thenReturn(Optional.empty());
    when(chatPostService.getLastChatPost(CHAT_ID))
        .thenReturn(null);

    boolean result = service.isChatReadByUser(chat, user);

    assertTrue(result);
  }

  @Test
  void isChatReadByUserReturnsFalseWhenChatHasNeverBeenRead() {
    when(jobChatUserRepository.findById(KEY))
        .thenReturn(Optional.empty());
    when(chatPostService.getLastChatPost(CHAT_ID))
        .thenReturn(lastPost);
    when(lastPost.getId()).thenReturn(100L);

    boolean result = service.isChatReadByUser(chat, user);

    assertFalse(result);
  }

  @Test
  void isChatReadByUserReturnsFalseWhenLastReadPostIsOlder() {
    JobChatUser jobChatUser = new JobChatUser();
    jobChatUser.setLastReadPost(lastReadPost);

    when(jobChatUserRepository.findById(KEY))
        .thenReturn(Optional.of(jobChatUser));
    when(chatPostService.getLastChatPost(CHAT_ID))
        .thenReturn(lastPost);
    when(lastPost.getId()).thenReturn(100L);
    when(lastReadPost.getId()).thenReturn(99L);

    boolean result = service.isChatReadByUser(chat, user);

    assertFalse(result);
  }

  @Test
  void isChatReadByUserReturnsTrueWhenLastPostHasBeenRead() {
    JobChatUser jobChatUser = new JobChatUser();
    jobChatUser.setLastReadPost(lastReadPost);

    when(jobChatUserRepository.findById(KEY))
        .thenReturn(Optional.of(jobChatUser));
    when(chatPostService.getLastChatPost(CHAT_ID))
        .thenReturn(lastPost);
    when(lastPost.getId()).thenReturn(100L);
    when(lastReadPost.getId()).thenReturn(100L);

    boolean result = service.isChatReadByUser(chat, user);

    assertTrue(result);
  }

  @Test
  void isChatReadByUserReturnsTrueWhenUserReadBeyondLastKnownPost() {
    JobChatUser jobChatUser = new JobChatUser();
    jobChatUser.setLastReadPost(lastReadPost);

    when(jobChatUserRepository.findById(KEY))
        .thenReturn(Optional.of(jobChatUser));
    when(chatPostService.getLastChatPost(CHAT_ID))
        .thenReturn(lastPost);
    when(lastPost.getId()).thenReturn(100L);
    when(lastReadPost.getId()).thenReturn(101L);

    boolean result = service.isChatReadByUser(chat, user);

    assertTrue(result);
  }

  @Test
  void markChatAsReadCreatesNewRecordWhenNoneExists() {
    when(jobChatUserRepository.findById(KEY))
        .thenReturn(Optional.empty());

    service.markChatAsRead(chat, user, lastPost);

    ArgumentCaptor<JobChatUser> captor =
        ArgumentCaptor.forClass(JobChatUser.class);

    verify(jobChatUserRepository).save(captor.capture());

    JobChatUser saved = captor.getValue();

    assertAll(
        () -> assertEquals(CHAT_ID, (long) saved.getId().getJobChatId()),
        () -> assertEquals(USER_ID, (long) saved.getId().getUserId()),
        () -> assertSame(chat, saved.getChat()),
        () -> assertSame(user, saved.getUser()),
        () -> assertSame(lastPost, saved.getLastReadPost())
    );
  }

  @Test
  void markChatAsReadUpdatesExistingRecord() {
    JobChatUser existing = new JobChatUser();
    existing.setId(KEY);
    existing.setChat(chat);
    existing.setUser(user);

    when(jobChatUserRepository.findById(KEY))
        .thenReturn(Optional.of(existing));

    service.markChatAsRead(chat, user, lastPost);

    assertSame(lastPost, existing.getLastReadPost());

    verify(jobChatUserRepository).save(existing);
  }

  @Test
  void markChatAsReadCanSetLastReadPostToNull() {
    JobChatUser existing = new JobChatUser();
    existing.setId(KEY);
    existing.setChat(chat);
    existing.setUser(user);
    existing.setLastReadPost(lastReadPost);

    when(jobChatUserRepository.findById(KEY))
        .thenReturn(Optional.of(existing));

    service.markChatAsRead(chat, user, null);

    assertNull(existing.getLastReadPost());

    verify(jobChatUserRepository).save(existing);
    verify(jobChatUserRepository, never())
        .deleteById(KEY);
  }
}