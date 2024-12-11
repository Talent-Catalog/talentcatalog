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

alter table candidate add column sflink text;
alter table candidate add column folderlink text;

update candidate set sflink=
 (select location from candidate_attachment where candidate_id = candidate.id
  and type = 'link' and lower(name) like '%salesforce%' fetch first row only)
where sflink is null;

update candidate set folderlink=
 (select location from candidate_attachment where candidate_id = candidate.id
  and type = 'link' and lower(name) like '%folder%' fetch first row only)
where folderlink is null;
