
alter table candidate add column potential_duplicate boolean default false not null;
alter table saved_search add column potential_duplicate boolean;
