-- Syrian
update candidate set nationality_id = 9396 where nationality_id = 6863;
delete from nationality where id = 6863;
delete from translation where object_type = 'nationality' and object_id = 6863;

-- Iraqi
update candidate set nationality_id = 9317 where nationality_id = 265;
delete from nationality where id = 265;
delete from translation where object_type = 'nationality' and object_id = 265;

-- Palestinian
update candidate set nationality_id = 9362 where nationality_id = 7342;
delete from nationality where id = 7342;
delete from translation where object_type = 'nationality' and object_id = 7342;

-- Somali
update candidate set nationality_id = 9386 where nationality_id = 267;
delete from nationality where id = 267;
delete from translation where object_type = 'nationality' and object_id = 267;

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

-- Sudanese
update candidate set nationality_id = 9391 where nationality_id = 353;
delete from nationality where id = 353;
delete from translation where object_type = 'nationality' and object_id = 353;
