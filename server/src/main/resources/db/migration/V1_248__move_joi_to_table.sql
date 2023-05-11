/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
alter table salesforce_job_opp drop column recruitment_process;
alter table salesforce_job_opp drop column employer_cost_commitment;
alter table salesforce_job_opp drop column location;
alter table salesforce_job_opp drop column location_details;
alter table salesforce_job_opp drop column benefits;
alter table salesforce_job_opp drop column language_requirements;
alter table salesforce_job_opp drop column employment_experience;
alter table salesforce_job_opp drop column education_requirements;
alter table salesforce_job_opp drop column skill_requirements;
alter table salesforce_job_opp drop column occupation_code;

create table job_opp_intake
(
    id                          bigserial not null primary key,
    job_opp_id                  bigint not null references salesforce_job_opp,
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