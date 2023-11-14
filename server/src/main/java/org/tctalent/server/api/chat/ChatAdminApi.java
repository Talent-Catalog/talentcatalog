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

package org.tctalent.server.api.chat;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.api.admin.ITableApi;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.request.chat.CreateChatRequest;
import org.tctalent.server.request.chat.SearchChatRequest;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.JobService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * This is the api where new chats can be created and updated.
 *
 * @author John Cameron
 */
@RestController()
@RequestMapping("/api/admin/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatAdminApi implements
    ITableApi<SearchChatRequest, CreateChatRequest, CreateChatRequest> {

    private final CandidateOpportunityService candidateOpportunityService;
    private final JobChatService chatService;
    private final JobService jobService;
    private final PartnerService partnerService;

    @Override
    @PostMapping
    public @NonNull Map<String, Object> create(CreateChatRequest request) throws EntityExistsException {
        final Long jobId = request.getJobId();
        SalesforceJobOpp jobOpp = jobId == null ? null : jobService.getJob(jobId);
        final Long sourcePartnerId = request.getSourcePartnerId();
        PartnerImpl sourcePartner = sourcePartnerId == null ? null :
            (PartnerImpl) partnerService.getPartner(sourcePartnerId);
        final Long candidateOppId = request.getCandidateOppId();
        CandidateOpportunity candidateOpp = candidateOppId == null ? null : candidateOpportunityService.getCandidateOpportunity(candidateOppId);

        JobChat chat = chatService.createJobChat(request.getType(),
            jobOpp, sourcePartner, candidateOpp);
        return chatDto().build(chat);
    }

    @Override
    @NonNull
    public List<Map<String, Object>> list() {
        List<JobChat> chats = chatService.listJobChats();
        return chatDto().buildList(chats);
    }


    @NonNull
    @PostMapping("get-or-create")
    public Map<String, Object> getOrCreate(@RequestBody CreateChatRequest request) {
        final Long jobId = request.getJobId();
        SalesforceJobOpp jobOpp = jobId == null ? null : jobService.getJob(jobId);
        final Long sourcePartnerId = request.getSourcePartnerId();
        PartnerImpl sourcePartner = sourcePartnerId == null ? null :
            (PartnerImpl) partnerService.getPartner(sourcePartnerId);
        final Long candidateOppId = request.getCandidateOppId();
        CandidateOpportunity candidateOpp = candidateOppId == null ? null : candidateOpportunityService.getCandidateOpportunity(candidateOppId);

        JobChat jobChat = chatService.getOrCreateJobChat(request.getType(),
            jobOpp, sourcePartner, candidateOpp);
        return chatDto().build(jobChat);
    }

    private DtoBuilder chatDto() {
        return new DtoBuilder()
            .add("id")
        ;
    }
}
