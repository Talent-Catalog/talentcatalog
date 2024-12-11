alter table language_level rename column level to name;
alter table language_level rename column sort_order to level;
alter table language_level alter column level SET NOT NULL;
alter table saved_search drop column english_min_spoken_level_id;
alter table saved_search drop column english_min_written_level_id;
alter table saved_search add column english_min_spoken_level integer;
alter table saved_search add column english_min_written_level integer;
alter table saved_search drop column other_min_spoken_level_id;
alter table saved_search drop column other_min_written_level_id;
alter table saved_search add column other_min_spoken_level integer;
alter table saved_search add column other_min_written_level integer;

