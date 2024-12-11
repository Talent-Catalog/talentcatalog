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

-- List name and definition
create table saved_list
(
    id     bigserial not null primary key,
    status text      not null,
    name   text      not null,
    fixed  boolean default false not null,
    watcher_ids text,
    created_by   bigint not null references users,
    created_date timestamptz not null,
    updated_by   bigint references users,
    updated_date timestamptz

);

-- List sharing between users
create table user_saved_list
(
    user_id bigint not null references users,
    saved_list_id bigint not null references saved_list,
    primary key (user_id, saved_list_id)
);

-- Note that the primary key can serve as the index for user_id.
-- See https://stackoverflow.com/questions/3048154/indexes-and-multi-column-primary-keys
create index user_saved_list_id_idx on user_saved_list(saved_list_id);



-- List membership of candidates
create table candidate_saved_list
(
    candidate_id bigint not null references candidate,
    saved_list_id bigint not null references saved_list,
    primary key (candidate_id, saved_list_id)
);

-- Note that the primary key can serve as the index for candidate_id.
-- See https://stackoverflow.com/questions/3048154/indexes-and-multi-column-primary-keys
create index candidate_saved_list_id_idx on candidate_saved_list(saved_list_id);
