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

alter table salesforce_job_opp add column recruitment_process text;
alter table salesforce_job_opp add column employer_cost_commitment text;
alter table salesforce_job_opp add column location text;
alter table salesforce_job_opp add column location_details text;
alter table salesforce_job_opp add column benefits text;
alter table salesforce_job_opp add column language_requirements text;
alter table salesforce_job_opp add column employment_experience text;
alter table salesforce_job_opp add column education_requirements text;
alter table salesforce_job_opp add column skill_requirements text;
