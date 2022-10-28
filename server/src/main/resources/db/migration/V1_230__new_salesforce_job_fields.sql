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

-- Tc_job_id should be unique
alter table salesforce_job_opp add constraint uq_tc_job_id unique (tc_job_id);

-- Make additional index based on tc_job_id
create index salesforce_job_opp_tc_job_id_idx on salesforce_job_opp(tc_job_id);

-- New fields
alter table salesforce_job_opp add column contact_email text;
alter table salesforce_job_opp add column contact_user_id bigint references users;
alter table salesforce_job_opp add column job_summary text;
alter table salesforce_job_opp add column recruiter_partner_id bigint references partner;
alter table salesforce_job_opp add column suggested_list_id bigint references saved_list;

-- Support multiple salesforce_job_opp suggestedSearches
create table job_suggested_saved_search
(
    tc_job_id bigint references salesforce_job_opp(tc_job_id),
    saved_search_id bigint references saved_search,
    primary key (tc_job_id, saved_search_id)
);
