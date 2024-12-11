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

create table salesforce_job_opp
(
    id                        text not null primary key,
    closed                    boolean default false not null,
    country                   text,
    employer                  text,
    last_update               timestamptz,
    name                      text,
    stage                     text,
    stage_order               int
);

create table job
(
    id                        bigserial not null primary key,
    submission_due_date       timestamptz,
    sf_job_opp_id             text references salesforce_job_opp,
    submission_list_id        bigint references saved_list
);

-- To speed up look ups of job by submission list
create index job_submission_list_id_idx on job(submission_list_id);
