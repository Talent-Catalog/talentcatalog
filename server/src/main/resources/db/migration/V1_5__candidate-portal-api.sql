-- Add location
alter table candidate add column country_id bigint;
alter table candidate add column city text;
alter table candidate add column year_of_arrival integer;

-- Add nationality
alter table candidate add column nationality text;
alter table candidate add column registered_with_un boolean;
alter table candidate add column registration_id text;

alter table candidate add column education_level text;

-- Add additional info
alter table candidate add column additional_info text;

create table education
(
  id                      bigserial not null primary key,
  candidate_id            bigint not null,
  education_type          text,
  country_id              bigint not null,
  length_of_course_years  integer,
  institution             text,
  course_name             text,
  date_completed          text
);

create table language
(
id                      bigserial not null primary key,
candidate_id            bigint not null,
name                    text,
read_write              text,
speak                   text
);

create table work_experience
(
id                      bigserial not null primary key,
candidate_id            bigint not null,
company_name            text,
country_id              text,
role                    text,
start_date              text,
end_date                text,
full_time               text,
paid                    text,
description             text
);

create table certification
(
id                      bigserial not null primary key,
candidate_id            bigint not null,
name                    text,
institution             text,
date_completed          text
);

create table country
(
id                      bigserial not null primary key,
name                    text
);

-- Add countries
insert into country (name) values ('Afghanistan');
insert into country (name) values ('Albania');
insert into country (name) values ('Algeria');
insert into country (name) values ('Andorra');
insert into country (name) values ('Angola');
insert into country (name) values ('Antigua and Barbuda');