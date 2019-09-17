-- Add location
alter table candidate add column country text;
alter table candidate add column city text;
alter table candidate add column year_of_arrival text;

-- Add nationality
alter table candidate add column nationality text;
alter table candidate add column registered_with_un text;
alter table candidate add column registration_id text;

alter table candidate add column education_level text;

-- Add additional info
alter table candidate add column additional_info text;

CREATE type education_type AS ENUM ('Masters','University','Schooling');

create table education
(
  id                      bigserial not null primary key,
  candidate_id            bigint not null,
  education_type          education_type,
  country_id              text,
  length_of_course_years  text,
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