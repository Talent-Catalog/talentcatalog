create table if not exists service_assignment (
    id bigserial primary key,
    provider varchar(255) not null,                     -- e.g. "DUOLINGO", etc.
    service_code varchar(255) not null,                 -- e.g. "TEST_PROCTORED"
    resource_id bigint references service_resource(id) on delete set null,
    candidate_id bigint not null references candidate(id),
    actor_id bigint references users(id),
    status varchar(255) not null,                     -- e.g. ASSIGNED | REDEEMED | EXPIRED | REASSIGNED
    assigned_at timestamptz not null,
    created_at timestamptz not null default now()
);

create index if not exists sa_resource_idx on service_assignment(resource_id);
create index if not exists sa_candidate_idx on service_assignment(candidate_id);
create index if not exists sa_provider_service_candidate_status_time_idx
    on service_assignment(provider, service_code, candidate_id, status, assigned_at desc);

/*
 * Copyright (c) 2025 Talent Catalog.
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

-- At most one current assignment per resource (resource-backed services)
create unique index if not exists sa_assigned_per_resource_uq_idx
    on service_assignment(resource_id)
    where status = 'ASSIGNED' and resource_id is not null;
