create table if not exists counterparty (
    id               bigserial primary key,
    type             varchar(255) not null,
    partner_id       bigint references partner (id),
    employer_id      bigint references employer (id),
    service_provider varchar(255),
    name             varchar(255),
    created_date     timestamptz,
    created_by       bigint references users (id),
    updated_date     timestamptz,
    updated_by       bigint references users (id)
);

create unique index if not exists cp_type_partner_uq_idx
    on counterparty (type, partner_id)
    where partner_id is not null;

create unique index if not exists cp_type_employer_uq_idx
    on counterparty (type, employer_id)
    where employer_id is not null;

create unique index if not exists cp_type_service_provider_uq_idx
    on counterparty (type, service_provider)
    where service_provider is not null;

create unique index if not exists cp_type_name_uq_idx
    on counterparty (type, lower(name))
    where name is not null;

create table if not exists agreement (
    id              bigserial primary key,
    candidate_id    bigint       not null references candidate (id),
    counterparty_id bigint       not null references counterparty (id),
    terms_info_id   varchar(255) not null,
    start_date      timestamptz  not null,
    end_date        timestamptz,
    created_date    timestamptz,
    created_by      bigint references users (id),
    updated_date    timestamptz,
    updated_by      bigint references users (id)
);

create index if not exists agreement_candidate_start_idx
    on agreement (candidate_id, start_date desc);

create unique index if not exists agreement_active_candidate_counterparty_uq_idx
    on agreement (candidate_id, counterparty_id)
    where end_date is null;
