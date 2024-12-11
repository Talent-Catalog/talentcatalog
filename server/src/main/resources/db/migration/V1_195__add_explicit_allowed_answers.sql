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

alter table task add column explicit_allowed_answers text;

insert into task (name, display_name, task_type, description, days_to_complete, explicit_allowed_answers,
                  created_by, created_date)
values ('studiedEnglishUniversity','Did you study in English at University?', 'QuestionTask', 'Please select:',
        7, 'Yes,No',
        (select id from users where username = 'SystemAdmin'), now());
