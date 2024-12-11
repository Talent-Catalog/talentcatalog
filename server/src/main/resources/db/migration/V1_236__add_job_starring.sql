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

-- Job starring
create table user_job
(
    user_id bigint not null references users,
    tc_job_id bigint not null references salesforce_job_opp,
    primary key (user_id, tc_job_id)
);

-- Note that the primary key can serve as the index for user_id.
-- See https://stackoverflow.com/questions/3048154/indexes-and-multi-column-primary-keys
create index user_job_id_idx on user_job(tc_job_id);


--Accepting candidates
alter table salesforce_job_opp add column accepting boolean default false not null;

