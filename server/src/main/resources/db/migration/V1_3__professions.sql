create table industry
(
  id                      bigserial not null primary key,
  name                    text
);

-- Add industries
insert into industry (name) values ('Accounting');
insert into industry (name) values ('Engineering');
insert into industry (name) values ('Information Technology');
insert into industry (name) values ('Legal');
insert into industry (name) values ('Medicine');
insert into industry (name) values ('Nursing');

create table profession
(
  id                      bigserial not null primary key,
  candidate_id            bigint not null,
  industry_id             bigint not null,
  years_experience        numeric
);

