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

package org.tctalent.server.service.db;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.JobChat;
import org.tctalent.server.model.db.JobChatType;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.util.dto.DtoBuilder;

public interface JobChatService {

    /**
     * Return a DtoBuilder for JobChats
     * @return DtoBuilder that builds a DTO object
     */
    DtoBuilder getJobChatDtoBuilder();

    /**
     * Finds a job chat matching the given type and paramteres, creating one if needed.
     * <p/>
     * Parameters which are not needed for job type may be null.
     *
     * @param type          Type of Job Chat
     * @param job           Job associated with chat
     * @param sourcePartner Source partner associated with chat
     * @param candidate     Candidate associated with chat
     * @return Found or created job chat
     * @throws InvalidRequestException if required objects for this chat type are missing (null).
     */
    @NonNull JobChat getOrCreateJobChat(JobChatType type, @Nullable SalesforceJobOpp job,
        @Nullable PartnerImpl sourcePartner, @Nullable Candidate candidate)
        throws InvalidRequestException;

    /**
     * Creates a job chat which is associated with the Job Creator, but not any particular source
     * partner or candidate.
     * @param type Should be {@link JobChatType#JobCreatorAllSourcePartners} or
     * {@link JobChatType#AllJobCandidates}
     * @param job Job associated with chat
     * @return created JobChat
     * @throws InvalidRequestException if type is not allowed
     */
    @NonNull JobChat createJobCreatorChat(
        @NonNull JobChatType type, @NonNull SalesforceJobOpp job) throws InvalidRequestException;

    /**
     * Creates a {@link JobChatType#JobCreatorSourcePartner} type JobChat.
     * <p/>
     * This is associated with a particular Source Partner and Job (no association with any
     * particular candidate)
     * @param job Job associated with chat
     * @param sourcePartner Source partner asssociated with chat
     * @return created JobChat
     */
    @NonNull JobChat createJobCreatorSourcePartnerChat(
        @NonNull SalesforceJobOpp job, @NonNull PartnerImpl sourcePartner);

    /**
     * Creates a {@link JobChatType#CandidateProspect} type JobChat
     *
     * @param candidate Candidate associated with chat
     * @return created JobChat
     */
    @NonNull JobChat createCandidateProspectChat(@NonNull Candidate candidate)
        throws InvalidRequestException;

    /**
     * Creates a {@link JobChatType#CandidateRecruiting} type JobChat
     *
     * @param candidate Candidate associated with job chat
     * @param job Job associated with job chat
     * @return created JobChat
     */
    @NonNull JobChat createCandidateRecruitingChat(
        @NonNull Candidate candidate, @NonNull SalesforceJobOpp job)
        throws InvalidRequestException;

    /**
     * Return chats by their ids
     * @param ids The ids to be looked up. If an id does not correspond to any chat,
     *            it is ignored and no chat is returned.
     * @return Chats with ids matching the given ids.
     */
    List<JobChat> findByIds(@NonNull Collection<Long> ids);

    /**
     * Find chats which have posts where the date of the last post is greater than a given date.
     *
     * @param dateTime We want chats with posts after this date
     * @return Chats since the given date
     */
    List<Long> findChatsWithPostsSinceDate(OffsetDateTime dateTime);

    /**
     * Get the JobChat with the given id.
     * @param id Id of job chat to get
     * @return JobChat
     * @throws NoSuchObjectException if there is no JobChat with this id.
     */
    @NonNull
    JobChat getJobChat(long id) throws NoSuchObjectException;

    /**
     * Return all JobChats.
     * @return JobChats
     */
    @NonNull
    List<JobChat> listJobChats();

    /**
     * Gets the {@link JobChatType#CandidateProspect} type {@link JobChat} for candidate with given ID,
     * if there is one.
     * @param candidateId ID of candidate
     * @return null if there is no chat yet, or {@link JobChat} if there is
     */
    @Nullable
    JobChat getCandidateProspectChat(long candidateId);
}
