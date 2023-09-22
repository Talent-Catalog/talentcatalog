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

alter table candidate_visa_job_check add column relevant_work_exp text;
alter table candidate_visa_job_check add column age_requirement text;
alter table candidate_visa_job_check add column preferred_pathways text;
alter table candidate_visa_job_check add column ineligible_pathways text;
alter table candidate_visa_job_check add column eligible_pathways text;
alter table candidate_visa_job_check add column occupation_category text;
alter table candidate_visa_job_check add column occupation_sub_category text;
