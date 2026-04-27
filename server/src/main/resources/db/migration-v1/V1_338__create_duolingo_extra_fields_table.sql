/*
 * Copyright (c) 2025 Talent Catalog.
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

-- create the duolingo_extra_fields table
create table duolingo_extra_fields
(
    id bigserial not null primary key,
    certificate_url varchar(255),
    interview_url varchar(255),
    verification_date varchar(255),
    percent_score int not null,
    scale int not null,
    literacy_subscore int not null,
    conversation_subscore int not null,
    comprehension_subscore int not null,
    production_subscore int not null,
    candidate_exam_id bigint not null references candidate_exam(id)
);

