create table terms_info
(
    id bigserial not null primary key,
    content text,
    created_date timestamptz,
    type text,
    version text
);

alter table candidate add accepted_privacy_policy_id bigint references terms_info;
alter table candidate add accepted_privacy_policy_date timestamptz;

