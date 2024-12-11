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

create table job_chat
(
    id               bigserial   not null primary key,
    candidate_opp_id bigint references candidate_opportunity,
    created_by       bigint      not null references users,
    created_date     timestamptz not null,
    job_id           bigint      references salesforce_job_opp,
    updated_by       bigint references users,
    updated_date     timestamptz

);

create table chat_post
(
    id           bigserial   not null primary key,
    content      text,
    job_chat_id  bigint      not null references job_chat,
    created_by   bigint      not null references users,
    created_date timestamptz not null,
    updated_by   bigint references users,
    updated_date timestamptz
);

-- To speed up look up of posts in a chat
create index job_chat_id_idx on chat_post (job_chat_id);
