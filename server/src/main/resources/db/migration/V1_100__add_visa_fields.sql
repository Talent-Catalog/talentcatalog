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
alter table candidate_visa add column heath_assessment text;
alter table candidate_visa add column heath_assessment_notes text;
alter table candidate_visa add column character_assessment text;
alter table candidate_visa add column character_assessment_notes text;
alter table candidate_visa add column security_risk text;
alter table candidate_visa add column security_risk_notes text;
alter table candidate_visa add column valid_travel_docs text;
alter table candidate_visa add column valid_travel_docs_notes text;
alter table candidate_visa add column overall_risk text;
alter table candidate_visa add column overall_risk_notes text;

create table candidate_role
(
    id                      bigserial not null primary key,
    candidate_visa          bigint not null references candidate_visa,
    interest                text,
    interestNotes           text,
    work_exp_yrs            integer,
    qualification_id        bigint references education_level,
    occupation_id           bigint references occupation,
    occupation_notes        text,
    salary_tsmit            text,
    regional                text,
    eligible_494            text,
    eligible_494_notes      text,
    eligible_186            text,
    eligible_186_notes      text,
    eligible_other          text,
    eligible_other_notes    text,
    put_forward             text,
    notes                   text
);

