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
alter table help_link add column focus text;
alter table help_link add column next_step_name text;
alter table help_link add column next_step_text text;
alter table help_link add column next_step_days integer;

alter table help_link add column created_by bigint references users;
alter table help_link add column created_date timestamptz;
alter table help_link add column updated_by bigint references users;
alter table help_link add column updated_date timestamptz;

