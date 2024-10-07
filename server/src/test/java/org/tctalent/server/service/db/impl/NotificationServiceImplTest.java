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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
    }

    @Test
    void computeUserNotifications() {
        List<JobChat> chats = new ArrayList<>();

        User user;
        Candidate candidate;
        JobChat chat;
        SalesforceJobOpp job;

        User systemAdminUser = createNonCandidateUser(2, Role.systemadmin);

        candidate = createCandidate(1);
        job = new SalesforceJobOpp();

        chat = new JobChat();
        chat.setId(1L);
        chat.setCandidate(candidate);
        chat.setJobOpp(job);
        chat.setType(JobChatType.CandidateProspect);

        chats.add(chat);


        CandidateOpportunity candidateOpportunity = new CandidateOpportunity();
        candidateOpportunity.setStage(CandidateOpportunityStage.cvReview);

        given(jobChatService.findByIds(any())).willReturn(chats);
        given(userService.getSystemAdminUser()).willReturn(systemAdminUser);
        given(candidateOpportunityRepository
            .findByCandidateIdAndJobId(any(), any())).willReturn(candidateOpportunity);

        Map<Long, Set<JobChat>> notifications = notificationService.computeUserNotifications();
        assertNotNull(notifications);
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
