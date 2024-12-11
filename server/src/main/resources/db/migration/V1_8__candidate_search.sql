
alter table saved_search drop column min_education_level_id;
alter table saved_search add column min_education_level integer;

alter table candidate add column registered_date timestamptz;
alter table candidate add column registered_by bigint references users;

alter table education_level rename column sort_order to level;
alter table education_level alter column level SET NOT NULL;

