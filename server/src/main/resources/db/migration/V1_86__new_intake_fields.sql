
alter table candidate_citizenship add column passport_exp date;
alter table candidate_exam add column year integer;

alter table candidate drop column host_entry_year;
alter table candidate add column host_entry_year integer;
