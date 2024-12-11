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

alter table education_level add column education_type text;

update education_level set education_type = 'Associate' where name = 'Associate Degree';
update education_level set education_type = 'Vocational' where name = 'Vocational Degree';
update education_level set education_type = 'Some University' where name = 'Bachelor''s Degree';
update education_level set education_type = 'Bachelor' where name = 'Bachelor''s Degree';
update education_level set education_type = 'Masters' where name = 'Master''s Degree';
update education_level set education_type = 'Doctoral' where name = 'Doctoral Degree';
