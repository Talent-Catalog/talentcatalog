/*
 * Copyright (c) 2026 Talent Catalog.
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

-- Service list: links a saved list to a candidate assistance service, declaring its role
-- and the admin actions permitted on its candidates.
create table if not exists service_list (
    id            bigserial primary key,
    saved_list_id bigint      not null unique references saved_list (id),
    provider      varchar(255) not null,   -- e.g. 'LINKEDIN'
    service_code  varchar(255) not null,   -- e.g. 'PREMIUM_MEMBERSHIP'
    list_role     varchar(255) not null,   -- e.g. 'ASSIGNMENT_FAILURE'
    created_at    timestamptz  not null default now()
);

create index if not exists sl_provider_service_role_idx
    on service_list (provider, service_code, list_role);

-- Permitted actions for each service list (one row per action)
create table if not exists service_list_permitted_actions (
    service_list_id bigint      not null references service_list (id) on delete cascade,
    action          varchar(255) not null,
    primary key (service_list_id, action)
);

-- Users designated as admins for a service list
create table if not exists service_list_admins (
    service_list_id bigint not null references service_list (id) on delete cascade,
    user_id         bigint not null references users (id) on delete cascade,
    primary key (service_list_id, user_id)
);

-- Register existing LinkedIn service lists so that ServiceListSetupService does not
-- create duplicates on first deploy. These lists were previously managed via hardcoded IDs.
insert into service_list (saved_list_id, provider, service_code, list_role)
values (12623, 'LINKEDIN', 'PREMIUM_MEMBERSHIP', 'USER_ISSUE_REPORT'),
       (12625, 'LINKEDIN', 'PREMIUM_MEMBERSHIP', 'ASSIGNMENT_FAILURE');

insert into service_list (saved_list_id, provider, service_code, list_role)
select id, 'LINKEDIN', 'PREMIUM_MEMBERSHIP', 'SERVICE_ELIGIBILITY'
from saved_list
where id in (12608, 12609, 12610, 12611, 12612, 12613, 12614, 12615,
             12616, 12617, 12618, 12619, 12620, 12621, 12622);

-- Permitted actions for the existing LinkedIn lists
insert into service_list_permitted_actions (service_list_id, action)
select sl.id, 'DISABLE_ASSIGNED_RESOURCE'
from service_list sl
where sl.saved_list_id = 12623;

insert into service_list_permitted_actions (service_list_id, action)
select sl.id, 'ASSIGN_NEW_RESOURCE'
from service_list sl
where sl.saved_list_id in (12623, 12625);
