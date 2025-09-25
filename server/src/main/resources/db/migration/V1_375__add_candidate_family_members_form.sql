/*
 * Copyright (c) 2025 Talent Catalog.
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

insert into candidate_form (name, description)
values ('FamilyDocForm',
        'Enter details of your eligible relocating family members.');


insert into task (name, display_name, task_type, description, days_to_complete,
                  created_by, created_date, candidate_form_id)
values ('familyDocFormTask','Enter details of your eligible relocating family members',
        'FormTask', 'Please enter details of your eligible relocating family members',
        7, (select id from users where username = 'SystemAdmin'),
        now(), (select id from candidate_form where name = 'FamilyDocForm'));

