alter table candidate add registered_by bigint references partner;

create index candidate_registered_by_idx on candidate(registered_by);


