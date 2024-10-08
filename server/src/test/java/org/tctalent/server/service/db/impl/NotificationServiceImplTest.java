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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collections;
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
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Role;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.model.db.User;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.service.db.ChatPostService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.PartnerService;
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
    PartnerService partnerService;

    @Mock
    UserService userService;

    NotificationServiceImpl notificationService;
    
    //System admin user
    User systemAdminUser;
    final static long SYSTEM_ADMIN_USER_ID = 999;
    
    @BeforeEach
    void setUp() {
        notificationService =
            new NotificationServiceImpl(
                null, candidateOpportunityRepository, chatPostService,
                null, jobChatService, null,
                partnerService, userService);

        //Set up a system admin user
        systemAdminUser = createNonCandidateUser(SYSTEM_ADMIN_USER_ID, Role.systemadmin);
        given(userService.getSystemAdminUser()).willReturn(systemAdminUser);
        
    }

    @Test
    void computeCandidateProspectUserNotifications() {

        //These are the chats that the jobChatService will return
        List<JobChat> chats = new ArrayList<>();
        given(jobChatService.findByIds(any())).willReturn(chats);

        //Create chat to test
        PartnerImpl sourcePartner = createPartner(10, createNonCandidateUser(4, Role.partneradmin));
        Candidate candidateAssociatedWithChat = createCandidate(1, sourcePartner);
        SalesforceJobOpp job = createJob(candidateAssociatedWithChat);
        JobChat chat = createChat(
            100, JobChatType.CandidateProspect, candidateAssociatedWithChat, job, null);
        chats.add(chat);

        //Create chat posts on chat and make chatPostService return it
        List<ChatPost> chatPosts = new ArrayList<>();
        
        //Random poster
        ChatPost chatPost = createChatPost(chat,
            createNonCandidateUser(3, Role.partneradmin) );
        chatPosts.add(chatPost);

        //Auto post (by system admin)
        ChatPost chatAutoPost = createChatPost(chat, systemAdminUser); 
        chatPosts.add(chatAutoPost);
        given(chatPostService.listChatPosts(anyLong())).willReturn(chatPosts);

        //All cases are in review stage
        mockAllCasesToStage(CandidateOpportunityStage.cvReview);
        Map<Long, Set<JobChat>> notifications = notificationService.computeUserNotifications();
        
        //The notifications for the chats should be user 1 (the candidate chat owner) and user 3
        //(the poster) and user 4 (the source partner contact) 
        assertNotNull(notifications);
        assertEquals(3, notifications.size());

        //Notify candidate associated with chat (past review stage)
        //User 1 has one chat - chat 100
        Set<JobChat> user1Chats = notifications.get(1L);
        assertEquals(1, user1Chats.size());
        assertEquals(100, user1Chats.toArray(new JobChat[1])[0].getId());

        //Notify random poster
        //User 3 has one chat - chat 100
        Set<JobChat> user3Chats = notifications.get(3L);
        assertEquals(1, user3Chats.size());
        assertEquals(100, user3Chats.toArray(new JobChat[1])[0].getId());

        //Notify candidate's source partner's contact
        //User 4 has one chat - chat 100
        Set<JobChat> user4Chats = notifications.get(4L);
        assertEquals(1, user4Chats.size());
        assertEquals(100, user4Chats.toArray(new JobChat[1])[0].getId());

        //System Admin is never notified
        Set<JobChat> systemAdminUserChats = notifications.get(SYSTEM_ADMIN_USER_ID);
        assertNull(systemAdminUserChats);
    }
    
    @Test
    void computeCandidateRecruitingUserNotifications() {

        //These are the chats that the jobChatService will return
        List<JobChat> chats = new ArrayList<>();
        given(jobChatService.findByIds(any())).willReturn(chats);

        //Create chat to test
        PartnerImpl sourcePartner = createPartner(10, createNonCandidateUser(4, Role.partneradmin));
        Candidate candidateAssociatedWithChat = createCandidate(1, sourcePartner);
        SalesforceJobOpp job = createJob(candidateAssociatedWithChat);
        JobChat chat = createChat(
            100, JobChatType.CandidateRecruiting, candidateAssociatedWithChat, job, null);
        chats.add(chat);

        //Create chat posts on chat and make chatPostService return it
        List<ChatPost> chatPosts = new ArrayList<>();
        
        //Random poster
        ChatPost chatPost = createChatPost(chat,
            createNonCandidateUser(3, Role.partneradmin) );
        chatPosts.add(chatPost);

        //Auto post (by system admin)
        ChatPost chatAutoPost = createChatPost(chat, systemAdminUser); 
        chatPosts.add(chatAutoPost);
        given(chatPostService.listChatPosts(anyLong())).willReturn(chatPosts);

        //All cases are in review stage
        mockAllCasesToStage(CandidateOpportunityStage.cvReview);
        Map<Long, Set<JobChat>> notifications = notificationService.computeUserNotifications();
        
        //The notifications for the chats should be user 1 (the candidate chat owner) and user 3
        //(the poster) and user 4 (the source partner contact) 
        assertNotNull(notifications);
        assertEquals(3, notifications.size());

        //Notify candidate associated with chat (past review stage)
        //User 1 has one chat - chat 100
        Set<JobChat> user1Chats = notifications.get(1L);
        assertEquals(1, user1Chats.size());
        assertEquals(100, user1Chats.toArray(new JobChat[1])[0].getId());

        //Notify random poster
        //User 3 has one chat - chat 100
        Set<JobChat> user3Chats = notifications.get(3L);
        assertEquals(1, user3Chats.size());
        assertEquals(100, user3Chats.toArray(new JobChat[1])[0].getId());

        //Notify candidate's source partner's contact
        //User 4 has one chat - chat 100
        Set<JobChat> user4Chats = notifications.get(4L);
        assertEquals(1, user4Chats.size());
        assertEquals(100, user4Chats.toArray(new JobChat[1])[0].getId());

        //System Admin is never notified
        Set<JobChat> systemAdminUserChats = notifications.get(SYSTEM_ADMIN_USER_ID);
        assertNull(systemAdminUserChats);

        
        //All cases are in prospect stage
        mockAllCasesToStage(CandidateOpportunityStage.prospect);
        notifications = notificationService.computeUserNotifications();
        
        assertNotNull(notifications);
        assertEquals(2, notifications.size());

        //Do NOT notify candidate associated with chat because case is not at review stage
        //User 1 has one chat - chat 100
        user1Chats = notifications.get(1L);
        assertNull(user1Chats);

        //Notify random poster
        //User 3 has one chat - chat 100
        user3Chats = notifications.get(3L);
        assertEquals(1, user3Chats.size());
        assertEquals(100, user3Chats.toArray(new JobChat[1])[0].getId());

        //Notify candidate's source partner's contact
        //User 4 has one chat - chat 100
        user4Chats = notifications.get(4L);
        assertEquals(1, user4Chats.size());
        assertEquals(100, user4Chats.toArray(new JobChat[1])[0].getId());

        //System Admin is never notified
        systemAdminUserChats = notifications.get(SYSTEM_ADMIN_USER_ID);
        assertNull(systemAdminUserChats);
    }
    
    @Test
    void computeAllJobCandidatesUserNotifications() {

        //These are the chats that the jobChatService will return
        List<JobChat> chats = new ArrayList<>();
        given(jobChatService.findByIds(any())).willReturn(chats);

        //Create chat to test
        PartnerImpl sourcePartner = createPartner(10, createNonCandidateUser(4, Role.partneradmin));
        Candidate candidateAssociatedWithChat = createCandidate(1, sourcePartner);
        SalesforceJobOpp job = createJob(candidateAssociatedWithChat);
        JobChat chat = createChat(
            100, JobChatType.AllJobCandidates, candidateAssociatedWithChat, job, null);
        chats.add(chat);

        //Create chat posts on chat and make chatPostService return it
        List<ChatPost> chatPosts = new ArrayList<>();
        
        //Random poster
        ChatPost chatPost = createChatPost(chat,
            createNonCandidateUser(3, Role.partneradmin) );
        chatPosts.add(chatPost);

        //Auto post (by system admin)
        ChatPost chatAutoPost = createChatPost(chat, systemAdminUser); 
        chatPosts.add(chatAutoPost);
        given(chatPostService.listChatPosts(anyLong())).willReturn(chatPosts);

        //All cases are in visa preparation stage
        setJobCasesStage(job, CandidateOpportunityStage.visaPreparation);
        Map<Long, Set<JobChat>> notifications = notificationService.computeUserNotifications();
        
        //The notifications for the chats should be user 1 (the candidate chat owner) and user 3
        //(the poster) and user 4 (the source partner contact) 
        assertNotNull(notifications);
        assertEquals(3, notifications.size());

        //Notify candidate associated with chat 
        //User 1 has one chat - chat 100
        Set<JobChat> user1Chats = notifications.get(1L);
        assertEquals(1, user1Chats.size());
        assertEquals(100, user1Chats.toArray(new JobChat[1])[0].getId());

        //Notify random poster
        //User 3 has one chat - chat 100
        Set<JobChat> user3Chats = notifications.get(3L);
        assertEquals(1, user3Chats.size());
        assertEquals(100, user3Chats.toArray(new JobChat[1])[0].getId());

        //Notify candidate's source partner's contact
        //User 4 has one chat - chat 100
        Set<JobChat> user4Chats = notifications.get(4L);
        assertEquals(1, user4Chats.size());
        assertEquals(100, user4Chats.toArray(new JobChat[1])[0].getId());

        //System Admin is never notified
        Set<JobChat> systemAdminUserChats = notifications.get(SYSTEM_ADMIN_USER_ID);
        assertNull(systemAdminUserChats);

        
        //All cases are in prospect stage
        setJobCasesStage(job, CandidateOpportunityStage.prospect);
        notifications = notificationService.computeUserNotifications();
        
        assertNotNull(notifications);
        assertEquals(2, notifications.size());

        //Do NOT notify candidate associated with chat because case is not past acceptance stage
        //User 1 has one chat - chat 100
        user1Chats = notifications.get(1L);
        assertNull(user1Chats);

        //Notify random poster
        //User 3 has one chat - chat 100
        user3Chats = notifications.get(3L);
        assertEquals(1, user3Chats.size());
        assertEquals(100, user3Chats.toArray(new JobChat[1])[0].getId());

        //Notify candidate's source partner's contact
        //User 4 has one chat - chat 100
        user4Chats = notifications.get(4L);
        assertEquals(1, user4Chats.size());
        assertEquals(100, user4Chats.toArray(new JobChat[1])[0].getId());

        //System Admin is never notified
        systemAdminUserChats = notifications.get(SYSTEM_ADMIN_USER_ID);
        assertNull(systemAdminUserChats);
    }


    @Test
    void computeJobCreatorSourcePartnerUserNotifications() {

        //These are the chats that the jobChatService will return
        List<JobChat> chats = new ArrayList<>();
        given(jobChatService.findByIds(any())).willReturn(chats);

        //Create chat to test
        PartnerImpl sourcePartner = createPartner(10, createNonCandidateUser(4, Role.partneradmin));
        PartnerImpl destinationPartner = createPartner(1000, createNonCandidateUser(50, Role.partneradmin));
        SalesforceJobOpp job = createJob(destinationPartner);
        JobChat chat = createChat(
            100, JobChatType.JobCreatorSourcePartner, null, job, sourcePartner);
        chats.add(chat);

        //Create chat posts on chat and make chatPostService return it
        List<ChatPost> chatPosts = new ArrayList<>();

        //Random poster
        ChatPost chatPost = createChatPost(chat,
            createNonCandidateUser(3, Role.partneradmin) );
        chatPosts.add(chatPost);

        //Auto post (by system admin)
        ChatPost chatAutoPost = createChatPost(chat, systemAdminUser);
        chatPosts.add(chatAutoPost);
        given(chatPostService.listChatPosts(anyLong())).willReturn(chatPosts);

        Map<Long, Set<JobChat>> notifications = notificationService.computeUserNotifications();

        //The notifications for the chats should be source partner contact (4), 
        // destination partner contact (50), the poster (3). 
        assertNotNull(notifications);
        assertEquals(3, notifications.size());

        Set<JobChat> userChatsA = notifications.get(4L);
        assertEquals(1, userChatsA.size());
        assertEquals(100, userChatsA.toArray(new JobChat[1])[0].getId());

        Set<JobChat> userChatsB = notifications.get(50L);
        assertEquals(1, userChatsB.size());
        assertEquals(100, userChatsB.toArray(new JobChat[1])[0].getId());

        Set<JobChat> userChatsC = notifications.get(3L);
        assertEquals(1, userChatsC.size());
        assertEquals(100, userChatsC.toArray(new JobChat[1])[0].getId());
        
        //System Admin is never notified
        Set<JobChat> systemAdminUserChats = notifications.get(SYSTEM_ADMIN_USER_ID);
        assertNull(systemAdminUserChats);
    }


    @Test
    void computeJobCreatorAllSourcePartnersUserNotifications() {

        //These are the chats that the jobChatService will return
        List<JobChat> chats = new ArrayList<>();
        given(jobChatService.findByIds(any())).willReturn(chats);

        //Mock source partners returned by partnerService 
        List<PartnerImpl> sourcePartners = new ArrayList<>();
        given(partnerService.listSourcePartners()).willReturn(sourcePartners);
        sourcePartners.add(createPartner(10, createNonCandidateUser(4, Role.partneradmin)));
        sourcePartners.add(createPartner(20, createNonCandidateUser(8, Role.partneradmin)));

        //Create chat to test
        PartnerImpl destinationPartner = createPartner(1000, createNonCandidateUser(50, Role.partneradmin));
        SalesforceJobOpp job = createJob(destinationPartner);
        JobChat chat = createChat(
            100, JobChatType.JobCreatorAllSourcePartners, null, job, null);
        chats.add(chat);

        //Create chat posts on chat and make chatPostService return it
        List<ChatPost> chatPosts = new ArrayList<>();

        //Random poster
        ChatPost chatPost = createChatPost(chat,
            createNonCandidateUser(3, Role.partneradmin) );
        chatPosts.add(chatPost);

        //Auto post (by system admin)
        ChatPost chatAutoPost = createChatPost(chat, systemAdminUser);
        chatPosts.add(chatAutoPost);
        given(chatPostService.listChatPosts(anyLong())).willReturn(chatPosts);

        Map<Long, Set<JobChat>> notifications = notificationService.computeUserNotifications();

        //The notifications for the chats should be to the contacts for the two source 
        // partners (4,8), destination partner contact (50), the poster (3). 
        assertNotNull(notifications);
        assertEquals(4, notifications.size());

        Set<JobChat> userChatsA = notifications.get(4L);
        assertEquals(1, userChatsA.size());
        assertEquals(100, userChatsA.toArray(new JobChat[1])[0].getId());

        Set<JobChat> userChatsA2 = notifications.get(8L);
        assertEquals(1, userChatsA2.size());
        assertEquals(100, userChatsA2.toArray(new JobChat[1])[0].getId());

        Set<JobChat> userChatsB = notifications.get(50L);
        assertEquals(1, userChatsB.size());
        assertEquals(100, userChatsB.toArray(new JobChat[1])[0].getId());

        Set<JobChat> userChatsC = notifications.get(3L);
        assertEquals(1, userChatsC.size());
        assertEquals(100, userChatsC.toArray(new JobChat[1])[0].getId());

        //System Admin is never notified
        Set<JobChat> systemAdminUserChats = notifications.get(SYSTEM_ADMIN_USER_ID);
        assertNull(systemAdminUserChats);

    }
    
    private void setJobCasesStage(SalesforceJobOpp job, CandidateOpportunityStage stage) {
        for (CandidateOpportunity opp : job.getCandidateOpportunities()) {
            opp.setStage(stage);
        }
    }

    private SalesforceJobOpp createJob(PartnerImpl destinationPartner) {
        SalesforceJobOpp job = new SalesforceJobOpp();
        job.setJobCreator(destinationPartner);
        job.setContactUser(destinationPartner.getDefaultContact());
        return job;
    }

    private SalesforceJobOpp createJob(Candidate candidate) {
        SalesforceJobOpp job = new SalesforceJobOpp();
        CandidateOpportunity caseOpp = new CandidateOpportunity();
        caseOpp.setJobOpp(job);
        caseOpp.setCandidate(candidate);
        job.setCandidateOpportunities(Collections.singleton(caseOpp));
        return job;
    }

    private void mockAllCasesToStage(CandidateOpportunityStage stage) {
        given(candidateOpportunityRepository.findByCandidateIdAndJobId(any(), any()))
            .willReturn(createCase(stage));
    }

    private PartnerImpl createPartner(long id, User nonCandidateUser) {
        PartnerImpl partner = new PartnerImpl();
        partner.setId(id);
        partner.setDefaultContact(nonCandidateUser);
        return partner;
    }

    private ChatPost createChatPost(JobChat chat, User postingUser) {
        ChatPost chatPost = new ChatPost();
        chatPost.setJobChat(chat);
        chatPost.setCreatedBy(postingUser);
        return chatPost;
    }

    private JobChat createChat(
        long id, JobChatType jobChatType, Candidate candidate, SalesforceJobOpp job, PartnerImpl sourcePartner) {
        JobChat chat = new JobChat();
        chat.setId(id);
        chat.setCandidate(candidate);
        chat.setJobOpp(job);
        chat.setSourcePartner(sourcePartner);
        chat.setType(jobChatType);
        return  chat;
    }

    private Candidate createCandidate(long userId, PartnerImpl sourcePartner) {
        User user = new User();
        user.setRole(Role.user);
        user.setPartner(sourcePartner);
        user.setId(userId);
        Candidate candidate = new Candidate();
        candidate.setUser(user);
        return candidate;
    }

    private CandidateOpportunity createCase(CandidateOpportunityStage stage) {
        CandidateOpportunity opp = new CandidateOpportunity();
        opp.setStage(stage);
        return opp;
    }

    private User createNonCandidateUser(long userId, Role role) {
        User user = new User();
        user.setId(userId);
        user.setRole(role);
        return user;
    }
}
