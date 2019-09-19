-- Add location
alter table candidate add column country_id bigint;
alter table candidate add column city text;
alter table candidate add column year_of_arrival integer;

-- Add nationality
alter table candidate add column nationality_id bigint;
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

create table candidate_language
(
id                      bigserial not null primary key,
candidate_id            bigint not null,
language_id             bigint not null,
read_write              bigint not null,
speak                   bigint not null
);

create table work_experience
(
id                      bigserial not null primary key,
candidate_id            bigint not null,
company_name            text,
country_id              bigint,
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

create table nationality
(
id                      bigserial not null primary key,
name                    text
);

-- Add nationalities
insert into nationality (name) values ('Afghan');
insert into nationality (name) values ('Albanian');
insert into nationality (name) values ('Algerian');
insert into nationality (name) values ('Andorran');
insert into nationality (name) values ('Angolan');
insert into nationality (name) values ('Antiguan or Barbudan');

create table language
(
id                      bigserial not null primary key,
name                    text
);

-- Add languages
insert into language (name) values ('English');
insert into language (name) values ('Afar');
insert into language (name) values ('Afrikaans');
insert into language (name) values ('Akan');
insert into language (name) values ('Albanian');
insert into language (name) values ('Amharic');

create table language_level
(
id                       bigserial not null primary key,
level                    text
);

-- Add language levels
insert into language_level (level) values ('Elementary');
insert into language_level (level) values ('Intermediate');
insert into language_level (level) values ('Professional');
insert into language_level (level) values ('Fluent');