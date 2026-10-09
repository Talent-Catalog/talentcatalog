create table if not exists matching (
    id                   bigint generated always as identity primary key,
    matching_description text,
    updated_date         timestamptz not null
);
