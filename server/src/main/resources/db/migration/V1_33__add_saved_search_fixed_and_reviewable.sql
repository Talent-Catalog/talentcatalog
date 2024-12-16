
alter table saved_search add column fixed boolean default false not null;
alter table saved_search add column reviewable boolean default false not null;

update saved_search set fixed = true where saved_search.fixed = false;
update saved_search set reviewable = true where saved_search.reviewable = false;
