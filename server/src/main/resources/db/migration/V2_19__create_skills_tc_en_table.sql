create table if not exists skills_tc_en (
    id               bigserial primary key,
    name             text not null,
    created_date     timestamptz,
    created_by       bigint references users (id)
);

create index if not exists skills_tc_en_name_idx
    on skills_tc_en (name);
