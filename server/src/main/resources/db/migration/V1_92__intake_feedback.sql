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

alter table candidate add column partner_edu_level_notes text;
alter table candidate add column partner_occupation_notes text;
alter table candidate add column returned_home_reason_no text;
alter table candidate add column residence_status_notes text;
alter table candidate add column work_desired_notes text;

alter table candidate drop column work_legally;
alter table candidate drop column left_home_reason;
alter table candidate add column left_home_reasons text;
alter table candidate add column military_wanted text;
alter table candidate add column military_notes text;
alter table candidate add column military_start date;
alter table candidate add column military_end date;
alter table candidate add column int_recruit_other text;
alter table candidate add column avail_immediate_job_ops text;
alter table candidate add column unhcr_registered text;
alter table candidate add column unrwa_registered text;
alter table candidate_dependant add column name text;
alter table candidate_dependant add column registered text;

