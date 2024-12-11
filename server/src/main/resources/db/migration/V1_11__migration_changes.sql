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

/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

update users set id = -4 where id = 4;

alter table candidate alter column candidate_number drop not null;
alter table candidate add constraint uq_user_id unique (user_id);
alter table candidate drop column email;
alter table candidate add column migration_nationality text;

alter table candidate_language alter column written_level_id drop not null;
alter table candidate_language alter column spoken_level_id drop not null;
alter table candidate_language alter column language_id drop not null;
alter table candidate_language add column migration_language text;

alter table candidate_occupation add constraint uq_candidate_occupation unique (candidate_id, occupation_id);
alter table candidate_occupation add column migration_occupation text;
alter table candidate_occupation alter column occupation_id drop not null;

alter table candidate_job_experience alter column start_date type date USING start_date::date;
alter table candidate_job_experience alter column end_date type date USING end_date::date;

-- add unmapped values
insert into country (id, name, status) values (0, 'Unknown', 'inactive');
insert into nationality (id, name, status) values (0, 'Unknown', 'inactive');
insert into language (id, name, status) values (0, 'Unknown', 'inactive');
insert into language_level (id, name, status, level) values (0, 'Unknown', 'inactive', 0);
insert into industry (id, name, status) values (0, 'Unknown', 'inactive');
insert into occupation (id, name, status) values (0, 'Unknown', 'inactive');
insert into education_level (id, name, status, level) values (0, 'Unknown', 'inactive', 0);
insert into education_major (id, name, status) values (0, 'Unknown', 'inactive');


