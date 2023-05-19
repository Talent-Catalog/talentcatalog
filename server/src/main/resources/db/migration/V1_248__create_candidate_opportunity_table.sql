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

create table candidate_opportunity
(
    id                             bigserial             not null primary key,

    candidate_id                   bigint references candidate,
    closing_comments_for_candidate text,
    employer_feedback              text,
    job_opp_id                     bigint references salesforce_job_opp,
    stage                          text,

--     The following are potentially common with job opportunity
    closing_comments               text,
    closed                         boolean default false not null,
    last_modified_date             timestamptz,
    name                           text,
    next_step                      text,
    next_step_due_date             timestamptz,
    sfId                           text,
    stage_order                    int

);
