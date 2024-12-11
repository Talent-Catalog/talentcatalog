
create table job_chat
(
    id               bigserial   not null primary key,
    candidate_opp_id bigint references candidate_opportunity,
    created_by       bigint      not null references users,
    created_date     timestamptz not null,
    job_id           bigint      references salesforce_job_opp,
    updated_by       bigint references users,
    updated_date     timestamptz

);

create table chat_post
(
    id           bigserial   not null primary key,
    content      text,
    job_chat_id  bigint      not null references job_chat,
    created_by   bigint      not null references users,
    created_date timestamptz not null,
    updated_by   bigint references users,
    updated_date timestamptz
);

-- To speed up look up of posts in a chat
create index job_chat_id_idx on chat_post (job_chat_id);
