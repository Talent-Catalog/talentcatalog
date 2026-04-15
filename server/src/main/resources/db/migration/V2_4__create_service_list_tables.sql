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
