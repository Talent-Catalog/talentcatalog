
alter table saved_search add column exclusion_list_id bigint references tbbtalent.public.saved_list;
