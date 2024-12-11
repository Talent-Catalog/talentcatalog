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

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types, help_link,
                  created_by, created_date)
values ('ID Card','UploadTask', null,
        7, 'Identity', 'idCard', null, null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types, help_link,
                  created_by, created_date)
values ('UNHCR/UNRWA Registration Card','UploadTask', 'Copy of your UNHCR or UNRWA Registration Certificate or Barcode (if applicable). If you are not registered with the UNHCR or UNRWA, please click "abandon task" and add a comment explaining the reason why you are not registered.',
        7, 'Registration', 'unhcrUnrwaRegCard', null, null,
        (select id from users where username = 'SystemAdmin'), now());
