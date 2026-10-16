create table if not exists matching (
    id                   bigserial primary key,
    matching_description text,
    updated_date         timestamptz not null
);
