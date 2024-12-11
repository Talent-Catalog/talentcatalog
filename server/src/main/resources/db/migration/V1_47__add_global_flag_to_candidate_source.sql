
alter table saved_list add column global boolean default false not null;
alter table saved_search add column global boolean default false not null;

update saved_search set global = true where saved_search.fixed = true;

