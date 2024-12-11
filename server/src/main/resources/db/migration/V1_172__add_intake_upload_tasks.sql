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
values ('Collaboration Agreement','UploadTask', 'Please sign and upload collaboration agreement. You can download the document by clicking on the View Task Help button.',
        7, 'TBB Forms', 'collaborationAgreement', null, 'https://www.talentbeyondboundaries.org/help/upload-collaboration-agreement',
        (select id from users where username = 'SystemAdmin'), now());

insert into task (name, task_type, description, days_to_complete, upload_subfolder_name,
                  upload_type, uploadable_file_types, help_link,
                  created_by, created_date)
values ('Information Release Form','UploadTask', 'Please fill out, sign and upload the Talent Beyond Boundaries information release form. You can download the document by clicking on the View Task Help button.',
        7, 'TBB Forms', 'infoReleaseForm', null, 'https://www.talentbeyondboundaries.org/help/upload-tbb-info-release-form',
        (select id from users where username = 'SystemAdmin'), now());
