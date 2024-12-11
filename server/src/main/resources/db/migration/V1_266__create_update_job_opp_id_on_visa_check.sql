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

alter table candidate_visa_job_check add column job_opp_id bigint references salesforce_job_opp;

update candidate_visa_job_check set job_opp_id =
    (select id from salesforce_job_opp where candidate_visa_job_check.sf_job_link = CONCAT('https://talentbeyondboundaries.lightning.force.com/lightning/r/Opportunity/', sf_id, '/view'))
where job_opp_id is null;
