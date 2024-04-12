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

-- Speed up look ups of lowercase usernames filtering out deleted statuses
create index users_username_active_status_idx on users (lower(username))
    where status <> 'deleted';

-- Filtered index for firstname and lastname searching
create index users_lower_first_name_idx on users (lower(first_name))
    where status != 'deleted';
create index users_lower_last_name_idx on users (lower(last_name))
    where status != 'deleted';

-- Filtered index for efficient search on user_id, status and country_id
create index candidate_user_status_country_active_idx ON candidate(user_id, status, country_id)
    where status <> 'deleted';

-- Filtered index for efficient search on candidate number, status and country id
create index candidate_number_status_country_active_idx ON candidate (candidate_number, status, country_id)
    where status != 'deleted';
