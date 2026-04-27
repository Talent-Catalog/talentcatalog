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

import {ChatPost, CreateChatRequest, JobChat, JobChatType, JobChatUserInfo, Post} from "./chat";
import {User} from "./user";

describe('Job Chat and Post interface', () => {
  it('should create a Post instance', () => {
    const post: Post = {
      linkPreviews: [],
      id: 1,
      content: 'Sample content'
    };

    expect(post).toBeTruthy();
    expect(post.id).toBe(1);
    expect(post.linkPreviews).toEqual([]);
    expect(post.content).toBe('Sample content');
  });

  it('should create a JobChat instance', () => {
    const jobChat: JobChat = {
      id: 1,
      type: JobChatType.CandidateProspect,
      name: 'Sample Job Chat'
    };

    expect(jobChat).toBeTruthy();
    expect(jobChat.id).toBe(1);
    expect(jobChat.name).toBe('Sample Job Chat');
  });

  it('should create a JobChatUserInfo instance', () => {
    const jobChatUserInfo: JobChatUserInfo = {
      numberUnreadChats: 5,
      lastReadPostId: 10,
      lastPostId: 20
    };

    expect(jobChatUserInfo).toBeTruthy();
    expect(jobChatUserInfo.numberUnreadChats).toBe(5);
    expect(jobChatUserInfo.lastReadPostId).toBe(10);
    expect(jobChatUserInfo.lastPostId).toBe(20);
  });

  it('should create a ChatPost instance', () => {
    const createdBy: User = { id: 1, username: 'user1' } as User;
    const updatedBy: User = { id: 2, username: 'user2' } as User;
    const jobChat: JobChat = { id: 1, type: JobChatType.CandidateProspect, name: 'Sample Job Chat' };

    const chatPost: ChatPost = {
      id: 1,
      content: 'Sample chat content',
      createdBy: createdBy,
      createdDate: new Date(),
      updatedBy: updatedBy,
      updatedDate: new Date(),
      jobChat: jobChat,
      reactions: []
    };

    expect(chatPost).toBeTruthy();
    expect(chatPost.id).toBe(1);
    expect(chatPost.content).toBe('Sample chat content');
    expect(chatPost.createdBy.username).toBe('user1');
    expect(chatPost.updatedBy.id).toBe(2);
    expect(chatPost.jobChat.name).toBe('Sample Job Chat');
  });

  it('should create a CreateChatRequest instance', () => {
    const createChatRequest: CreateChatRequest = {
      type: JobChatType.CandidateProspect,
      jobId: 1,
      sourcePartnerId: 2,
      candidateId: 3
    };

    expect(createChatRequest).toBeTruthy();
    expect(createChatRequest.type).toBe(JobChatType.CandidateProspect);
    expect(createChatRequest.jobId).toBe(1);
    expect(createChatRequest.sourcePartnerId).toBe(2);
    expect(createChatRequest.candidateId).toBe(3);
  });
});
