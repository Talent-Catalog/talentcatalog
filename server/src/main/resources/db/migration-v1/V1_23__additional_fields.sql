
alter table candidate_occupation add column updated_by bigint references users;
alter table candidate_occupation add column updated_date timestamptz;
alter table candidate_occupation add column created_by bigint references users;
alter table candidate_occupation add column created_date timestamptz;

create table audit_log
(
  id                      bigserial not null primary key,
  event_date              date not null,
  user_id                 bigint not null,
  type                    text not null,
  action                  text not null,
  object_ref              text not null,
  description             text not null
);
