/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

create table saved_search
(
  id                            bigserial not null primary key,
  status                        text not null,
  name                          text not null,
  keyword                       text,
  statuses                      text,
  gender                        text,
  occupation_ids                text,
  or_profile_keyword            text,
  verified_occupation_ids       text,
  verified_occupation_search_type text,
  nationality_ids               text,
  nationality_search_type       text,
  country_ids                   text,
  english_min_written_level_id  bigint references language_level,
  english_min_spoken_level_id   bigint references language_level,
  other_language_id             bigint references language,
  other_min_written_level_id    bigint references language_level,
  other_min_spoken_level_id     bigint references language_level,
  un_registered                 boolean,
  last_modified_from            date,
  last_modified_to              date,
  created_from                  date,
  created_to                    date,
  min_age                       integer,
  max_age                       integer,
  min_education_level_id        bigint references education_level,
  education_major_ids           text,
  created_by              bigint not null references users,
  created_date            timestamptz not null,
  updated_by              bigint references users,
  updated_date            timestamptz
);

create table search_join(
  id                            bigserial not null primary key,
  search_id                     bigint not null references saved_search,
  child_search_id               bigint not null references saved_search,
  search_type                   text not null
);


create table shortlist_candidate(
  id                            bigserial not null primary key,
  candidate_id                  bigint not null references candidate,
  search_id                     bigint not null references saved_search,
  shortlist_status              text not null ,
  comment                       text,
  created_by                    bigint not null references users,
  created_date                  timestamp not null,
  updated_by                    bigint references users,
  updated_date                  timestamp

);


