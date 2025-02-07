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

package org.tctalent.server.model.db;

/**
 * Different kinds of job chats.
 * <p>
 * NB: if creating a new JobChatType we must add and define new translations for its details.
 * See TC Wiki for how-to.
 *</p>
 * @author John Cameron
 */
public enum JobChatType {
    /**
     * Participants: source and destination partners associated with job - and with prospects for
     * the job in a particular source location.
     */
    JobCreatorSourcePartner,

    /**
     * Participants: All source partners, job destination partner
     */
    JobCreatorAllSourcePartners,

    /**
     * Participants: Source partner and candidate in source location who is a prospect for the job,
     * but who has not yet reached the stage (after CV Review) where they will be communicating
     * directly with the destination partner.
     * <p/>
     * Can also be used when there is no job yet associated - just between source partner and
     * candidate.
     */
    CandidateProspect,

    /**
     * Participants: Source partner, destination partner and candidate in source location whom the
     * employer has expressed interest in (ie post CV Review)
     */
    CandidateRecruiting,

    /**
     * Participants: All source partners, job destination partner, all candidates who have passed a
     * certain stage (eg job offer)
     */
    AllJobCandidates
}
