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

create table root_request
(
    id                   bigserial not null primary key,
    ip_address           text,
    partner_abbreviation text,
    query_string         text,
    request_url          text,
    timestamp            timestamp,
    utm_campaign         text,
    utm_content          text,
    utm_medium           text,
    utm_source           text,
    utm_term             text
);

-- To speed up look ups by ip address
create index ip_address_idx on root_request(ip_address);
