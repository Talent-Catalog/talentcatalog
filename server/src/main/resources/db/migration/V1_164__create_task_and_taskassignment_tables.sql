/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

create table task
(
    id                    bigserial   not null primary key,
    admin                 boolean     not null default false,
    created_by            bigint      not null references users,
    created_date          timestamptz not null,
    days_to_complete      integer,
    description           text,
    help_link             text,
    name                  text        not null,
    optional              boolean     not null default false,
    task_type             text,
    updated_by            bigint references users,
    updated_date          timestamptz,
    upload_subfolder_name text,
    upload_type           text,
    uploadable_file_types text

);

create table task_list
(
    parent_task_id bigint not null references task,
    task_id        bigint not null references task,
    primary key (parent_task_id, task_id)
);

create table task_assignment
(
    id               bigserial   not null primary key,
    abandoned_date   timestamptz,
    activated_by     bigint      not null references users,
    activated_date   timestamptz not null,
    candidate_id     bigint      not null references candidate,
    candidate_notes  text,
    completed_date   timestamptz,
    deactivated_by   bigint references users,
    deactivated_date timestamptz,
    due_date         timestamptz,
    related_list_id  bigint references saved_list,
    status           text,
    task_id          bigint      not null references task
);

