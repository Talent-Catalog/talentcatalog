create table if not exists service_resource (
    id bigserial primary key,
    provider varchar(255) not null,                    -- e.g. "DUOLINGO", etc.
    service_code varchar(255) not null,                -- e.g. "TEST_PROCTORED"
    resource_code varchar(255),                        -- e.g. coupon ID; nullable if not applicable
    status varchar(255) not null,                      -- e.g. AVAILABLE | RESERVED | SENT | REDEEMED | EXPIRED | DISABLED
    expires_at timestamptz,
    sent_at timestamptz,
    created_at timestamptz not null default now()
);

-- prevent duplicate concrete tokens per provider
create unique index if not exists sr_provider_resource_uq_idx
    on service_resource(provider, resource_code)
    where resource_code is not null;

-- fast allocation: for provider+service_code+status(e.g. 'AVAILABLE') then order by id
create index if not exists sr_provider_sc_status_idx
    on service_resource(provider, service_code, status);

-- for general lookups by provider+service_code
create index if not exists sr_provider_service_idx
    on service_resource(provider, service_code, id);



