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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.ChatPost;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.UserService;

@ExtendWith(SpringExtension.class)
class NotificationServiceImplTest {
    @Mock
    CandidateOpportunityRepository candidateOpportunityRepository;

    @Mock
    ChatPostService chatPostService;

    @Mock
    JobChatService jobChatService;

    @Mock
    UserService userService;

    NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService =
            new NotificationServiceImpl(
                null, candidateOpportunityRepository, chatPostService,
                null, jobChatService, null,
                null, userService);

        //Set up a system admin user
        User systemAdminUser = createNonCandidateUser(999, Role.systemadmin);
        given(userService.getSystemAdminUser()).willReturn(systemAdminUser);

        //This means that all candidate opportunities will look like are past the review stage
        CandidateOpportunity candidateOpportunity = new CandidateOpportunity();
        candidateOpportunity.setStage(CandidateOpportunityStage.cvReview);
        given(candidateOpportunityRepository
            .findByCandidateIdAndJobId(any(), any())).willReturn(candidateOpportunity);

    }

    @Test
    void computeUserNotifications() {

        //These are the chats that the jobChatService will return
        List<JobChat> chats = new ArrayList<>();
        given(jobChatService.findByIds(any())).willReturn(chats);

        //Test chat for candidate
        Candidate candidateAssociatedWithChat = createCandidate(1);
        SalesforceJobOpp job = new SalesforceJobOpp();
        JobChat candidateProspectChat = createChat(
            1, JobChatType.CandidateProspect, candidateAssociatedWithChat, job);
        chats.add(candidateProspectChat);

        //Create chat post on chat and make chatPostService return it
        List<ChatPost> chatPosts = new ArrayList<>();
        ChatPost chatPost = createChatPost(candidateProspectChat,
            createNonCandidateUser(3, Role.partneradmin) );
        chatPosts.add(chatPost);
        given(chatPostService.listChatPosts(anyLong())).willReturn(chatPosts);

        //The notifications for the chats should be user 1, the candidate chat owner and user 3
        //who posted.
        Map<Long, Set<JobChat>> notifications = notificationService.computeUserNotifications();
        assertNotNull(notifications);
        assertEquals(2, notifications.size());

        //User 1 has one chat
        final Set<JobChat> user1Chats = notifications.get(1L);

        //User 3 has one chat
        final Set<JobChat> user3Chats = notifications.get(3L);

        //TODO JC What about candidate's partner?
    }

    private ChatPost createChatPost(JobChat chat, User postingUser) {
        ChatPost chatPost = new ChatPost();
        chatPost.setJobChat(chat);
        chatPost.setCreatedBy(postingUser);
        return chatPost;
    }

    private JobChat createChat(
        long id, JobChatType jobChatType, Candidate candidate, SalesforceJobOpp job) {
        JobChat chat = new JobChat();
        chat.setId(id);
        chat.setCandidate(candidate);
        chat.setJobOpp(job);
        chat.setType(jobChatType);
        return  chat;
    }

    private Candidate createCandidate(long userId) {
        User user = new User();
        user.setRole(Role.user);
        user.setId(userId);
        Candidate candidate = new Candidate();
        candidate.setUser(user);
        return candidate;
    }

    private User createNonCandidateUser(long userId, Role role) {
        User user = new User();
        user.setId(userId);
        user.setRole(role);
        return user;
    }
}
