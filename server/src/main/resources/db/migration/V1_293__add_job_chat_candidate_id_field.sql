/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

alter table job_chat add column candidate_id bigint references candidate;

-- Indexes
create index job_chat_candidate_id_idx on job_chat(candidate_id);
create index job_chat_job_id_idx on job_chat(job_id);
create index job_chat_source_partner_id_idx on job_chat(source_partner_id);
create index job_chat_type_idx on job_chat(type);

-- Populate new field with candidate id taken from candidate associated with candidate opp
-- associated with existing candidate_opp_id field
update job_chat set candidate_id = opp.candidate_id from candidate_opportunity opp
where candidate_opp_id is not null and candidate_opp_id = opp.id;

