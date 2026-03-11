
alter table candidate add column work_abroad_loc bigint references country;
alter table candidate add column work_abroad_yrs integer;
