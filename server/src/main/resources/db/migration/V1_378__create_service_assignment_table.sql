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

-- At most one current assignment per resource (resource-backed services)
create unique index if not exists sa_assigned_per_resource_uq_idx
    on service_assignment(resource_id)
    where status = 'ASSIGNED' and resource_id is not null;
