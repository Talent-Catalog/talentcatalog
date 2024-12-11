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

alter table candidate add column partner_registered text;
alter table candidate add column partner_candidate_id bigint references candidate;
alter table candidate add column partner_edu_level_id bigint references education_level;
alter table candidate add column partner_occupation_id bigint references occupation;
alter table candidate add column partner_english text;
alter table candidate add column partner_english_level_id bigint references language_level;
alter table candidate add column partner_ielts text;
alter table candidate add column partner_ielts_score text;
alter table candidate add column partner_citizenship_id bigint references nationality;
