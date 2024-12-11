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

alter table candidate add column crime_convict text;
alter table candidate add column crime_convict_notes text;
alter table candidate add column conflict text;
alter table candidate add column conflict_notes text;
alter table candidate add column residence_status text;
alter table candidate add column work_abroad text;
alter table candidate add column host_entry_legally text;
alter table candidate add column left_home_reason text;
alter table candidate add column left_home_other text;
alter table candidate add column return_home_future text;
alter table candidate add column return_home_when text;
alter table candidate add column resettle_third text;
alter table candidate add column resettle_third_status text;
alter table candidate add column host_challenges text;
alter table candidate add column marital_status text;
