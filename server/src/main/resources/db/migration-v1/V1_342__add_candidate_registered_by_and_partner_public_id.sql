alter table candidate add registered_by bigint references partner;
create index candidate_registered_by_idx on candidate(registered_by);

alter table partner add public_id varchar(22);
create index partner_public_id_idx on partner(public_id);



