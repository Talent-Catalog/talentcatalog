update users set id = -4 where id = 4;

alter table candidate alter column candidate_number drop not null;
alter table candidate add constraint uq_user_id unique (user_id);

alter table candidate_language alter column written_level_id drop not null;
alter table candidate_language alter column spoken_level_id drop not null;