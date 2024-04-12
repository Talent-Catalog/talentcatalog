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

-- Speed up look ups of lowercase usernames filtering our deleted statuses
create index users_username_active_status_idx on users (lower(username))
    where status <> 'deleted';

-- Fast firstname and lastname searching
create index users_lower_first_name_idx on users (lower(first_name));
create index users_lower_last_name_idx on users (lower(last_name));

-- for efficient candidate filtering based on user_id, status and country_id
create index candidate_user_status_country_idx ON candidate(user_id, status, country_id);
