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

alter table candidate rename column nationality_id to nationalityold_id;
alter table candidate rename constraint candidate_nationality_id_fkey to candidate_nationalityold_id_fkey;
alter table candidate add column nationality_id bigint references country;
update candidate as c set nationality_id = j.country_id from country_nationality_join as j where j.nationality_id = nationalityold_id;

alter table candidate_citizenship drop constraint candidate_citizenship_nationality_id_fkey;
update candidate_citizenship as cc set nationality_id = j.country_id from country_nationality_join as j where j.nationality_id = cc.nationality_id;
alter table candidate_citizenship add constraint candidate_citizenship_nationality_id_fkey foreign key (nationality_id) REFERENCES country;
