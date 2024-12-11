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

create table system_language
(
	id                      bigserial not null primary key,
	language                text not null,
	label                   text not null,
    status                  text not null default 'active',
	created_by              bigint references users,
	created_date            timestamptz,
	updated_by              bigint references users,
	updated_date            timestamptz
);

create table translation
(
	id                      bigserial not null primary key,
	object_id               bigint not null,
	object_type             text not null,
	language                text not null,
	value                   text not null,
	created_by              bigint references users,
	created_date            timestamptz,
	updated_by              bigint references users,
	updated_date            timestamptz
);

insert into system_language (language, label, created_date, created_by, status) values ('en', 'English', now(), 1, 'active');
insert into system_language (language, label, created_date, created_by, status) values ('ar','عربى', now(), 1, 'active');
