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

create table candidate_shortlist_item
(
id                      bigserial not null primary key,
candidate_id            bigint not null references candidate,
saved_search_id         bigint not null references saved_search,
shortlist_status        text not null,
comment                 text,
created_by              bigint references users,
created_date            timestamptz,
updated_by              bigint references users,
updated_date            timestamptz
);


