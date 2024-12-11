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

alter table candidate add column avail_immediate text;
alter table candidate add column avail_immediate_reason text;
alter table candidate add column avail_immediate_notes text;

alter table candidate add column family_move text;
alter table candidate add column family_move_notes text;
alter table candidate add column family_health_concern text;
alter table candidate add column family_health_concern_notes text;
