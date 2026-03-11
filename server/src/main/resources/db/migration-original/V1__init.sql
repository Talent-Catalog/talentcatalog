create table country
(
id                      bigserial not null primary key,
name                    text not null,
status                  text not null default 'active'
);


create table nationality
(
id                      bigserial not null primary key,
name                    text not null,
status                  text not null default 'active'

);


create table language
(
id                      bigserial not null primary key,
name                    text not null,
status                  text not null default 'active'

);

create table language_level
(
id                       bigserial not null primary key,
level                    text not null,
status                   text not null default 'active',
sort_order               integer not null

);

create table industry
(
  id                      bigserial not null primary key,
  name                    text not null,
  status                  text not null default 'active'
);


create table occupation
(
  id                      bigserial not null primary key,
  name                    text not null,
  status                  text not null default 'active'
);



create table education_level
(
id                      bigserial not null primary key,
name                    text not null,
sort_order              integer not null,
status                  text not null default 'active'

);

create table education_major
(
id                      bigserial not null primary key,
name                    text not null,
status                  text not null default 'active'

);

create table users
(
  id                      bigserial not null primary key,
  username                text,
  first_name              text,
  last_name               text,
  email                   text,
  role                    text not null,
  status                  text not null,
  password_enc            text,
  last_login              timestamptz,
  created_by              bigint references users,
  created_date            timestamptz,
  updated_by              bigint references users,
  updated_date            timestamptz
);

create table candidate
(
  id                      bigserial not null primary key,
  candidate_number        text not null,
  user_id                 bigint references users,
  gender                  text,
  dob                     date,
  email                   text,
  phone                   text,
  whatsapp                text,
  status                  text not null,
  country_id              bigint references country,
  city                    text,
  year_of_arrival         integer,
  nationality_id          bigint references nationality,
  un_registered           boolean,
  un_registration_number  text,
  additional_info         text,
  max_education_level_id     bigint references education_level,
  created_by              bigint not null references users,
  created_date            timestamptz not null,
  updated_by              bigint references users,
  updated_date            timestamptz
);


create table candidate_occupation
(
  id                      bigserial not null primary key,
  candidate_id            bigint not null references candidate,
  occupation_id           bigint not null references occupation,
  years_experience        integer not null,
  verified                boolean default false,
  top_candidate           boolean
);


create table candidate_education
(
  id                      bigserial not null primary key,
  candidate_id            bigint not null references candidate,
  education_type          text,
  country_id              bigint not null references country,
  length_of_course_years  integer,
  institution             text,
  course_name             text,
  date_completed          text
);

create table candidate_language
(
id                      bigserial not null primary key,
candidate_id            bigint not null references candidate,
language_id             bigint not null references language,
written_level_id        bigint not null references language_level,
spoken_level_id         bigint not null references language_level
);

create table candidate_job_experience
(
id                      bigserial not null primary key,
candidate_id            bigint not null references candidate,
candidate_occupation_id bigint not null references candidate_occupation,
company_name            text,
country_id              bigint references country,
role                    text,
start_date              text,
end_date                text,
full_time               boolean,
paid                    boolean,
description             text
);

create table candidate_certification
(
id                      bigserial not null primary key,
candidate_id            bigint not null references candidate,
name                    text,
institution             text,
date_completed          text
);

create table candidate_file
(
id                      bigserial not null primary key,
candidate_id            bigint not null references candidate,
type                    text,
name                    text,
url                     text,
file_name               text,
created_by              bigint references users,
created_date            timestamptz
);

create table admin_note
(
id                      bigserial not null primary key,
candidate_id            bigint not null references candidate,
comment                 text,
created_by              bigint references users,
created_date            timestamptz
);


-- Create system admin as boot strap user. Should have user id = 1.
insert into users (username, role, first_name, last_name, email, status)
values('SystemAdmin', 'systemadmin','System', 'Admin', 'tbbtalent@talentbeyondboundaries.org', 'active');

