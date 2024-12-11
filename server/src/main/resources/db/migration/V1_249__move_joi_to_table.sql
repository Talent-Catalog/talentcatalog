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

create table job_opp_intake
(
    id                          bigserial not null primary key,
    recruitment_process         text,
    employer_cost_commitment    text,
    location                    text,
    location_details            text,
    salary_range                text,
    benefits                    text,
    language_requirements       text,
    employment_experience       text,
    education_requirements      text,
    skill_requirements          text,
    occupation_code             text,
    min_salary                  text
);
