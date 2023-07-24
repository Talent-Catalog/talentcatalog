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

-- For old jobs that did not have a publishedDate, set the published date to the created date
-- and publishedBy to createdBy
-- The publishedDate field was introduced 2022-10-31
update salesforce_job_opp set published_date = created_date
    where published_date is null and created_date <= '2022-10-31'::date;

update salesforce_job_opp set published_by = created_by
    where published_by is null and created_by is not null and created_date <= '2022-10-31'::date;
