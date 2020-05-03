drop table admin_note;

create table candidate_note
(
id                      bigserial not null primary key,
candidate_id            bigint not null references candidate,
note_type               text not null,
title                   text not null,
comment                 text,
created_by              bigint references users,
created_date            timestamp with time zone,
updated_by              bigint references users,
updated_date            timestamp with time zone
);


alter table candidate_education drop column date_completed;
alter table candidate_education add column year_completed integer;

