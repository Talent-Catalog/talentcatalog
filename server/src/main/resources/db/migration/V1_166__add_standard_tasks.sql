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
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Degree certificate','UploadTask', 'Bachelors degree certificate',
        7, 'Qualification', 'degree', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Degree transcript','UploadTask', 'Transcript with grades for all years',
        7, 'Qualification', 'degreeTranscript', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Degree transcript translation','UploadTask', 'Translation of degree transcript',
        7, 'Qualification', 'degreeTranscriptTrans', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Letter studied in English','UploadTask',
        'Letter from your university stating, in English, that you studied in English',
        7, 'Qualification', 'studiedInEnglish', null,
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types,
                  created_by, created_date)
values ('Passport','UploadTask',
        'Must have 6 months validity after arrival to the UK. Birth date must be present and correct',
        14, 'Identity', 'passport', null,
        (select id from users where username = 'SystemAdmin'), now());
