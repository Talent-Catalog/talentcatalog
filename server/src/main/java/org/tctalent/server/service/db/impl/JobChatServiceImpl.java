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

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.repository.db.JobChatRepository;
import org.tctalent.server.service.db.JobChatService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.util.dto.DtoBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobChatServiceImpl implements JobChatService {

    private final UserService userService;
    private final JobChatRepository jobChatRepository;

    private @NonNull JobChat createJobChat(JobChatType type, @Nullable SalesforceJobOpp job,
        @Nullable PartnerImpl sourcePartner, @Nullable Candidate candidate) {
        JobChat chat = new JobChat();
        chat.setCreatedBy(userService.getLoggedInUser());
        chat.setCreatedDate(OffsetDateTime.now());
        if (type != null) {
            chat.setType(type);
        }
        if (job != null) {
            chat.setJobOpp(job);
        }
        if (sourcePartner != null) {
            chat.setSourcePartner(sourcePartner);
        }
        if (candidate != null) {
            chat.setCandidate(candidate);
        }

        return jobChatRepository.save(chat);
    }

    @Override
    public DtoBuilder getJobChatDtoBuilder() {
        return new DtoBuilder()
            .add("id")
            .add("type")
            ;
    }

    @Override
    public @NonNull JobChat createJobCreatorChat(
        @NonNull JobChatType type, @NonNull SalesforceJobOpp job)
        throws InvalidRequestException {
        if (type != JobChatType.AllJobCandidates &&
            type != JobChatType.JobCreatorAllSourcePartners) {
            throw new InvalidRequestException("Unsupported type: " + type);
        }
        return getOrCreateJobChat(type, job, null, null);
    }

    @Override
    public @NonNull JobChat createJobCreatorSourcePartnerChat(
        @NonNull SalesforceJobOpp job, @NonNull PartnerImpl sourcePartner) {
        return getOrCreateJobChat(
            JobChatType.JobCreatorSourcePartner, job, sourcePartner, null);
    }

    @Override
    public @NonNull JobChat createCandidateProspectChat(@NonNull Candidate candidate) {
        return getOrCreateJobChat(JobChatType.CandidateProspect, null, null, candidate);
    }

    @NonNull
    @Override
    public JobChat createCandidateRecruitingChat(@NonNull Candidate candidate,
        @NonNull SalesforceJobOpp job) throws InvalidRequestException {
        return getOrCreateJobChat(JobChatType.CandidateRecruiting, job, null, candidate);
    }

    @Override
    public List<JobChat> findByIds(@NonNull Collection<Long> ids) {
        return jobChatRepository.findByIds(ids);
    }

    @Override
    @NonNull
    public JobChat getOrCreateJobChat(JobChatType type, @Nullable SalesforceJobOpp job,
        @Nullable PartnerImpl sourcePartner, @Nullable Candidate candidate)
        throws InvalidRequestException {
        if (type == null) {
            throw new InvalidRequestException("Missing JobChatType");
        }

        JobChat jobChat =
        switch (type) {
            case JobCreatorAllSourcePartners, AllJobCandidates -> {
                if (job == null) {
                    throw new InvalidRequestException("Missing Job");
                }
                yield(jobChatRepository.findByTypeAndJob(type, job.getId()));
            }

            case JobCreatorSourcePartner -> {
                if (job == null) {
                    throw new InvalidRequestException("Missing Job");
                }
                if (sourcePartner == null) {
                    throw new InvalidRequestException("Missing source partner");
                }
                yield(jobChatRepository.findByTypeAndJobAndPartner(type, job.getId(), sourcePartner.getId()));
            }

            case CandidateProspect -> {
                if (candidate == null) {
                    throw new InvalidRequestException("Missing candidate");
                }
                yield(jobChatRepository.findByTypeAndCandidate(type, candidate.getId()));
            }

            case CandidateRecruiting -> {
                if (candidate == null) {
                    throw new InvalidRequestException("Missing candidate");
                }
                if (job == null) {
                    throw new InvalidRequestException("Missing Job");
                }
                yield(jobChatRepository.findByTypeAndCandidateAndJob(type, candidate.getId(), job.getId()));
            }
        };

        if (jobChat == null) {
            jobChat = createJobChat(type, job, sourcePartner, candidate);
        }
        return jobChat;
    }

    @Override
    @NonNull
    public JobChat getJobChat(long id) throws NoSuchObjectException {
        return jobChatRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(JobChat.class, id));
    }

    @Override
    @NonNull
    public List<JobChat> listJobChats() {
        final List<JobChat> chats = jobChatRepository.findAll(Sort.by(Direction.ASC, "id"));
        return chats;
    }

    @Override
    public List<Long> findChatsWithPostsSinceDate(OffsetDateTime dateTime) {
        return jobChatRepository.findChatsWithPostsSinceDate(dateTime);
    }

    @Override
    @Nullable
    public JobChat getCandidateProspectChat(long candidateId) {
        return jobChatRepository.findByTypeAndCandidate(
            JobChatType.CandidateProspect, candidateId
        );
    }

}
